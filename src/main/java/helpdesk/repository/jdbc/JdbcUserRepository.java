package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.User;
import helpdesk.model.UserRole;
import helpdesk.repository.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public User save(User user) {
        String sql = """
            INSERT INTO users(name, email, role, department_id, active)
            VALUES (?, ?, ?::user_role, ?, ?)
            RETURNING id, created_at
        """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole().name());

            if (user.getDepartmentId() == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, user.getDepartmentId());
            }

            ps.setBoolean(5, user.isActive());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя по id", e);
        }
    }

    // Реализация Самостоятельного задания №1
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя по email", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> result = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapUser(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения списка пользователей", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setRole(UserRole.valueOf(rs.getString("role")));

        long deptId = rs.getLong("department_id");
        user.setDepartmentId(rs.wasNull() ? null : deptId);

        user.setActive(rs.getBoolean("active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}