import config.RoleConfig;
import gateway.APIGateway;
import gateway.Request;
import gateway.Response;
import tracking.RequestTracker;
import ui.APIGatewayUI;

import javax.swing.SwingUtilities;

import java.util.Scanner;

public class Main {
    private static final APIGateway apiGateway = new APIGateway();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            runConsole();
            return;
        }

        SwingUtilities.invokeLater(() -> new APIGatewayUI(apiGateway).setVisible(true));
    }

    private static void runConsole() {
        printWelcome();

        while (true) {
            String username = prompt("Enter your name");
            if (username.isBlank()) {
                System.out.println("[INPUT] Name cannot be empty. Try again.");
                continue;
            }

            String role = readRole();
            if (role == null) {
                continue;
            }

            int choice = readMenuChoice();
            if (choice == 6) {
                break;
            }

            String serviceType = mapChoiceToService(choice);
            if (serviceType == null) {
                System.out.println("[MENU] Invalid menu selection. Please choose again.");
                continue;
            }

            String requestData = promptForData(choice);
            Request request = new Request(username, role, serviceType, requestData);
            Response response = apiGateway.handleRequest(request);
            printResponse(response);
            printTrackerSummary(username);
        }

        System.out.println("\nThank you for using API Gateway Simulation. Goodbye!");
    }

    private static void printWelcome() {
        System.out.println("======================================");
        System.out.println("   API GATEWAY SIMULATION MENU");
        System.out.println("======================================");
        System.out.println("Supported roles: " + RoleConfig.getAvailableRoles());
    }

    private static String readRole() {
        String role = prompt("Enter your role (admin/user/guest)");
        if (!RoleConfig.isValidRole(role)) {
            System.out.println("[AUTH] Invalid role. Please use one of: " + RoleConfig.getAvailableRoles());
            return null;
        }
        return role.toLowerCase();
    }

    private static int readMenuChoice() {
        System.out.println();
        System.out.println("===== API GATEWAY MENU =====");
        System.out.println("1. User Service (View Profile)");
        System.out.println("2. Payment Service (Make Payment)");
        System.out.println("3. Order Service (Place Order)");
        System.out.println("4. Notification Service (Send Notification)");
        System.out.println("5. Analytics Service (Usage Stats)");
        System.out.println("6. Exit");
        String choiceInput = prompt("Select an option");
        try {
            return Integer.parseInt(choiceInput.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String mapChoiceToService(int choice) {
        return switch (choice) {
            case 1 -> "user";
            case 2 -> "payment";
            case 3 -> "order";
            case 4 -> "notification";
            case 5 -> "analytics";
            default -> null;
        };
    }

    private static String promptForData(int choice) {
        if (choice == 5) {
            return "";
        }

        String promptText = switch (choice) {
            case 1 -> "Enter profile request details";
            case 2 -> "Enter payment amount or transaction details";
            case 3 -> "Enter order description";
            case 4 -> "Enter notification message";
            default -> "Enter request details";
        };
        return prompt(promptText);
    }

    private static String prompt(String message) {
        System.out.print(message + ": ");
        return scanner.nextLine().trim();
    }

    private static void printResponse(Response response) {
        System.out.println();
        System.out.println("[RESPONSE] Status: " + response.getStatusCode() + " | Message: " + response.getMessage());
    }

    private static void printTrackerSummary(String username) {
        RequestTracker tracker = RequestTracker.getInstance();
        System.out.println("[TRACKER] Total requests: " + tracker.getTotalRequests() + " | " + username + ": " + tracker.getUserRequestCount(username));
    }
}
