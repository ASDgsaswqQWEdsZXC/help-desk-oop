package helpdesk.service;

import java.util.ArrayList;
import java.util.List;

public class AuditService {
    private final List<String> logs = new ArrayList<>();

    public void log(String action) {
        logs.add(action);
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }
}