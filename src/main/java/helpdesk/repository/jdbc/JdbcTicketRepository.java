package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.repository.TicketRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTicketRepository implements TicketRepository {

    @Override
    public Ticket save(Ticket ticket) {
        String sql = """
            INSERT INTO tickets(
                title, description, status, priority,
                customer_id, assignee_id, category_id
            )
            VALUES (?, ?, ?::ticket_status, ?::ticket_priority, ?, ?, ?)
            RETURNING id, created_at, updated_at
        """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getStatus().name());
            ps.setString(4, ticket.getPriority().name());
            ps.setLong(5, ticket.getCustomerId());

            if (ticket.getAssigneeId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, ticket.getAssigneeId());
            }

            if (ticket.getCategoryId() == null) {
                ps.setNull(7, Types.BIGINT);
            } else {
                ps.setLong(7, ticket.getCategoryId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ticket.setId(rs.getLong("id"));
                    ticket.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    ticket.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                }
            }
            return ticket;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить заявку", e);
        }
    }

    @Override
    public Optional<Ticket> findById(long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTicket(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска заявки по id", e);
        }
    }

    @Override
    public List<Ticket> findAll() {
        String sql = "SELECT * FROM tickets ORDER BY id";
        List<Ticket> result = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapTicket(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения всех заявок", e);
        }
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        String sql = """
            SELECT * FROM tickets
            WHERE status = ?::ticket_status
            ORDER BY created_at DESC
        """;
        List<Ticket> result = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTicket(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка выборки заявок по статусу", e);
        }
    }

    @Override
    public void update(Ticket ticket) {
        // Самостоятельное задание №3: автоматическое обновление updated_at
        String sql = """
            UPDATE tickets
            SET title = ?,
                description = ?,
                status = ?::ticket_status,
                priority = ?::ticket_priority,
                assignee_id = ?,
                category_id = ?,
                updated_at = CURRENT_TIMESTAMP,
                closed_at = ?
            WHERE id = ?
        """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getStatus().name());
            ps.setString(4, ticket.getPriority().name());

            if (ticket.getAssigneeId() == null) {
                ps.setNull(5, Types.BIGINT);
            } else {
                ps.setLong(5, ticket.getAssigneeId());
            }

            if (ticket.getCategoryId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, ticket.getCategoryId());
            }

            if (ticket.getClosedAt() == null) {
                ps.setNull(7, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(ticket.getClosedAt()));
            }

            ps.setLong(8, ticket.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления заявки", e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM tickets WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления заявки", e);
        }
    }

    // Вспомогательный маппер: преобразует строку ResultSet в объект Ticket
    private Ticket mapTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setStatus(TicketStatus.valueOf(rs.getString("status")));
        ticket.setPriority(TicketPriority.valueOf(rs.getString("priority")));
        ticket.setCustomerId(rs.getLong("customer_id"));

        long assigneeId = rs.getLong("assignee_id");
        ticket.setAssigneeId(rs.wasNull() ? null : assigneeId);

        long categoryId = rs.getLong("category_id");
        ticket.setCategoryId(rs.wasNull() ? null : categoryId);

        ticket.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        ticket.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Timestamp closed = rs.getTimestamp("closed_at");
        ticket.setClosedAt(closed == null ? null : closed.toLocalDateTime());

        return ticket;
    }
}