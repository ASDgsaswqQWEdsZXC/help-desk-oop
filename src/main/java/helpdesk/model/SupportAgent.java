/*package helpdesk.model;

import java.util.ArrayList;
import java.util.List;

public class SupportAgent extends User {
    private List<Ticket> assignedTickets = new ArrayList<>();

    public SupportAgent(long id, String name, String email) {
        super(id, name, email);
    }

    public void assignTicket(Ticket ticket) {
        ticket.startProcessing();
        assignedTickets.add(ticket);
    }

    public List<Ticket> getAssignedTickets() {
        return assignedTickets;
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " обрабатывает заявку");
    }
}*/