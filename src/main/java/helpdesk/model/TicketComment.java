package helpdesk.model;

import java.time.LocalDateTime;

public class TicketComment {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String text;
    private boolean internal;
    private LocalDateTime createdAt;

    public TicketComment() {}

    public TicketComment(Long ticketId, Long authorId, String text, boolean internal) {
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.text = text;
        this.internal = internal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isInternal() { return internal; }
    public void setInternal(boolean internal) { this.internal = internal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("TicketComment{id=%d, ticketId=%d, internal=%b, text='%s'}", id, ticketId, internal, text);
    }
}