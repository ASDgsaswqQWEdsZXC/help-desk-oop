package helpdesk;

import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.model.User;
import helpdesk.repository.jdbc.JdbcCommentRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;
import helpdesk.repository.jdbc.JdbcUserRepository;
import helpdesk.service.TicketService;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ЗАПУСК HELPDESK ===\n");

        TicketService service = new TicketService(new JdbcTicketRepository(), new JdbcCommentRepository());
        JdbcUserRepository userRepository = new JdbcUserRepository();

        // [№1] Поиск пользователей по email
        User client = userRepository.findByEmail("anna@example.org").orElseThrow();
        User agent = userRepository.findByEmail("sergey.net@example.org").orElseThrow();
        System.out.println("1. Клиент и Агент успешно найдены по email.");

        // Создание базовой заявки
        Ticket ticket = new Ticket();
        ticket.setTitle("Сломался принтер");
        ticket.setDescription("Не включается принтер");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setCustomerId(client.getId());
        ticket.setCategoryId(1L);
        ticket = service.createTicket(ticket);
        System.out.println("2. Заявка создана. ID: " + ticket.getId());

        // [№3 + Транзакции] Безопасная смена статусов 
        service.changeStatus(ticket.getId(), TicketStatus.IN_PROGRESS, agent.getId());
        service.changeStatus(ticket.getId(), TicketStatus.RESOLVED, agent.getId());
        service.changeStatus(ticket.getId(), TicketStatus.CLOSED, agent.getId());
        System.out.println("3. Заявка прошла по циклу: IN_PROGRESS -> RESOLVED -> CLOSED");

        // [№8] Красивый вывод карточки
        System.out.println("\n4. Вывод карточки заявки:");
        Ticket finalTicket = service.getTicketById(ticket.getId()).orElseThrow();
        finalTicket.printCard(client.getName(), agent.getName(), "Оборудование");

        // [№4] Проверка защиты бизнес-правил перехода статусов
        System.out.println("\n5. Тест защиты: пробуем отменить (CANCELLED) закрытую заявку...");
        try {
            service.changeStatus(ticket.getId(), TicketStatus.CANCELLED, client.getId());
        } catch (RuntimeException e) {
            System.out.println("   [УСПЕХ] Защита сработала: " + e.getMessage());
        }
    }
}