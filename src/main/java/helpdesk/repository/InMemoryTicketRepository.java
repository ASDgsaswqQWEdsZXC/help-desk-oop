/*package helpdesk.repository;

import helpdesk.model.Ticket;
import helpdesk.model.TicketStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryTicketRepository implements TicketRepository {
    private final List<Ticket> tickets = new ArrayList<>();

    @Override
    public void add(Ticket ticket) {
        tickets.add(ticket);
    }

    @Override
    public List<Ticket> findAll() {
        return new ArrayList<>(tickets);
    }

    @Override
    public Optional<Ticket> findById(long id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == status) {
                result.add(ticket);
            }
        }
        return result;
    }
}*/