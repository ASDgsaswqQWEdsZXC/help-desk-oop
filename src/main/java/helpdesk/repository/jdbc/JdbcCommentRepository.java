package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.TicketComment;
import helpdesk.repository.CommentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCommentRepository implements CommentRepository {

    @Override
    public TicketComment add(TicketComment comment) {
        String sql = """
            INSERT INTO ticket_comments(ticket_id, author_id, text, internal)
            VALUES (?, ?, ?, ?)
            RETURNING id, created_at
        """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, comment.getTicketId());
            ps.setLong(2, comment.getAuthorId());
            ps.setString(3, comment.getText());
            ps.setBoolean(4, comment.isInternal());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    comment.setId(rs.getLong("id"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            return comment;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления комментария к заявке", e);
        }
    }

    @Override
    public List<TicketComment> findByTicketId(long ticketId) {
        String sql = """
            SELECT id, ticket_id, author_id, text, internal, created_at
            FROM ticket_comments
            WHERE ticket_id = ?
            ORDER BY created_at ASC
        """;

        List<TicketComment> comments = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TicketComment comment = new TicketComment();
                    comment.setId(rs.getLong("id"));
                    comment.setTicketId(rs.getLong("ticket_id"));
                    comment.setAuthorId(rs.getLong("author_id"));
                    comment.setText(rs.getString("text"));
                    comment.setInternal(rs.getBoolean("internal"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    comments.add(comment);
                }
            }
            return comments;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения комментариев заявки", e);
        }
    }
}