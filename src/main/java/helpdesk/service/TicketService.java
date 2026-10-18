package helpdesk.service;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.Ticket;
import helpdesk.model.TicketComment;
import helpdesk.model.TicketStatus;
import helpdesk.repository.CommentRepository;
import helpdesk.repository.TicketRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    public Ticket createTicket(Ticket ticket) {
        if (ticket.getTitle() == null || ticket.getTitle().trim().length() < 3) {
            throw new IllegalArgumentException("Заголовок заявки должен содержать минимум 3 символа");
        }
        ticket.setStatus(TicketStatus.NEW);
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> getTicketById(long id) {
        return ticketRepository.findById(id);
    }

    public void assignTicket(long ticketId, long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка с ID " + ticketId + " не найдена"));

        ticket.setAssigneeId(agentId);
        ticketRepository.update(ticket);
    }

    public TicketComment addComment(long ticketId, long authorId, String text, boolean internal) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Текст комментария не может быть пустым");
        }
        TicketComment comment = new TicketComment(ticketId, authorId, text, internal);
        return commentRepository.add(comment);
    }

    public List<TicketComment> getTicketComments(long ticketId) {
        return commentRepository.findByTicketId(ticketId);
    }

    public void changeStatus(long ticketId, TicketStatus newStatus, long changedByUserId) {
        String selectSql = "SELECT status FROM tickets WHERE id = ? FOR UPDATE";
        
        String updateSql = """
            UPDATE tickets
            SET status = ?::ticket_status,
                updated_at = CURRENT_TIMESTAMP,
                closed_at = CASE 
                    WHEN ?::ticket_status IN ('CLOSED', 'CANCELLED') THEN CURRENT_TIMESTAMP 
                    ELSE closed_at 
                END
            WHERE id = ?
        """;

        String historySql = """
            INSERT INTO ticket_status_history(ticket_id, old_status, new_status, changed_by_id)
            VALUES (?, ?::ticket_status, ?::ticket_status, ?)
        """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                TicketStatus currentStatus;
                try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                    ps.setLong(1, ticketId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Заявка с ID " + ticketId + " не найдена");
                        }
                        currentStatus = TicketStatus.valueOf(rs.getString("status"));
                    }
                }

                validateTransition(currentStatus, newStatus);

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setString(1, newStatus.name());
                    ps.setString(2, newStatus.name());
                    ps.setLong(3, ticketId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(historySql)) {
                    ps.setLong(1, ticketId);
                    ps.setString(2, currentStatus.name());
                    ps.setString(3, newStatus.name());
                    ps.setLong(4, changedByUserId);
                    ps.executeUpdate();
                }

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Транзакция смены статуса отменена: " + e.getMessage(), e);
            } finally {
                // Возвращаем исходный режим
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка соединения с БД при смене статуса", e);
        }
    }
    private void validateTransition(TicketStatus current, TicketStatus target) {
        if (current == target) {
            throw new IllegalStateException("Заявка уже находится в статусе " + current);
        }

        boolean allowed = switch (current) {
            case NEW -> target == TicketStatus.IN_PROGRESS || target == TicketStatus.CANCELLED;
            case IN_PROGRESS -> target == TicketStatus.RESOLVED || target == TicketStatus.CANCELLED;
            case RESOLVED -> target == TicketStatus.CLOSED || target == TicketStatus.IN_PROGRESS;
            case CLOSED, CANCELLED -> false; // Конечные статусы, перевод запрещен
        };

        if (!allowed) {
            throw new IllegalStateException(
                    String.format("Недопустимый переход статуса: %s -> %s", current, target)
            );
        }
    }
}