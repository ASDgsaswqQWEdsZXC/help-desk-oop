package helpdesk.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import helpdesk.exception.InvalidTicketStateException;

public class Ticket {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private Long customerId;
    private Long assigneeId;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    public Ticket() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    // №8
    public void printCard(String customerName, String assigneeName, String categoryName) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        System.out.println("┌──────────────────────────────────────────────────────────");
        System.out.printf("│ ЗАЯВКА #%d: %s%n", id != null ? id : 0, title);
        System.out.println("├──────────────────────────────────────────────────────────");
        System.out.printf("│ Статус:       %-15s Приоритет: %s%n", status, priority);
        System.out.printf("│ Категория:    %s%n", categoryName != null ? categoryName : "Не указана");
        System.out.printf("│ Клиент:       %s (ID: %d)%n", customerName != null ? customerName : "Неизвестен", customerId);
        System.out.printf("│ Исполнитель:  %s%n", assigneeName != null ? assigneeName : "Не назначен");
        System.out.printf("│ Создана:      %s%n", createdAt != null ? createdAt.format(fmt) : "-");
        System.out.printf("│ Изменена:     %s%n", updatedAt != null ? updatedAt.format(fmt) : "-");
        if (closedAt != null) {
            System.out.printf("│ Закрыта:      %s%n", closedAt.format(fmt));
        }
        System.out.printf("│ Описание:     %s%n", description);
        System.out.println("└──────────────────────────────────────────────────────────");
    }

    public void startProcessing() {
        if (status != TicketStatus.NEW) {
            throw new InvalidTicketStateException("В работу можно взять только новую заявку");
        }
        status = TicketStatus.IN_PROGRESS;
    }

    public void resolve() {
        if (status != TicketStatus.IN_PROGRESS) {
            throw new InvalidTicketStateException("Решить можно только заявку в работе");
        }
        status = TicketStatus.RESOLVED;
    }

    public void close() {
        if (status != TicketStatus.RESOLVED) {
            throw new InvalidTicketStateException("Закрыть можно только решенную заявку");
        }
        status = TicketStatus.CLOSED;
    }

    public void cancel() {
        if (status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Нельзя отменить уже закрытую заявку");
        }
        status = TicketStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return String.format("Ticket{id=%d, title='%s', status=%s, priority=%s}", id, title, status, priority);
    }
}