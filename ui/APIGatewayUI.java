package ui;

import config.RoleConfig;
import gateway.APIGateway;
import gateway.Request;
import gateway.Response;
import tracking.RequestTracker;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Desktop dashboard UI for interactive API Gateway simulation.
 */
public class APIGatewayUI extends JFrame {
    private static final String[] SERVICES = {"user", "payment", "order", "notification", "analytics"};
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final APIGateway apiGateway;
    private final JTextField usernameField = new JTextField();
    private final JComboBox<String> roleBox = new JComboBox<>();
    private final JComboBox<String> serviceBox = new JComboBox<>(SERVICES);
    private final JTextField dataField = new JTextField();
    private final JLabel dataFieldLabel = new JLabel("Payload");

    private final JLabel statusPill = new JLabel("IDLE", SwingConstants.CENTER);
    private final JLabel totalRequestsValue = new JLabel("0");
    private final JLabel currentUserRequestsValue = new JLabel("0");
    private final JLabel trackerLabel = new JLabel("Ready to send requests", SwingConstants.LEFT);

    private final JTextArea outputArea = new JTextArea();
    private final JTextArea usageArea = new JTextArea();
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"#", "Time", "User", "Role", "Service", "Status", "Message"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private int requestSequence = 0;
    private PrintStream originalSystemOut;

    public APIGatewayUI(APIGateway apiGateway) {
        this.apiGateway = apiGateway;
        initializeLookAndFeel();
        setupWindow();
        buildUi();
        installConsoleMirror();
        registerListeners();
        updateDataFieldState();
        refreshUsageSummary();
    }

    private void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep default look and feel when system look and feel is unavailable.
        }
    }

    private void setupWindow() {
        setTitle("API Gateway Control Studio");
        setSize(1080, 700);
        setMinimumSize(new Dimension(940, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        setContentPane(new GradientPanel());
        setLayout(new BorderLayout(14, 14));

        JPanel contentWrapper = new JPanel(new BorderLayout(14, 14));
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        contentWrapper.add(createHeaderPanel(), BorderLayout.NORTH);
        contentWrapper.add(createMainSplitPanel(), BorderLayout.CENTER);

        add(contentWrapper, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setOpaque(true);
        panel.setBackground(new Color(245, 235, 220));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(197, 178, 150), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("API Gateway Control Studio");
        title.setFont(new Font("Georgia", Font.BOLD, 29));
        title.setForeground(new Color(54, 34, 28));

        JLabel subtitle = new JLabel("Role-aware request testing with live gateway insights");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(77, 61, 56));

        textWrap.add(title);
        textWrap.add(Box.createVerticalStrut(4));
        textWrap.add(subtitle);

        JPanel metricsPanel = new JPanel();
        metricsPanel.setOpaque(false);
        metricsPanel.setLayout(new BoxLayout(metricsPanel, BoxLayout.X_AXIS));

        metricsPanel.add(createMetricCard("TOTAL", totalRequestsValue));
        metricsPanel.add(Box.createHorizontalStrut(10));
        metricsPanel.add(createMetricCard("CURRENT USER", currentUserRequestsValue));
        metricsPanel.add(Box.createHorizontalStrut(10));

        styleStatusPill("IDLE", new Color(74, 98, 110), new Color(224, 234, 240));
        statusPill.setPreferredSize(new Dimension(118, 30));
        statusPill.setOpaque(true);
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 12));
        metricsPanel.add(statusPill);

        panel.add(textWrap, BorderLayout.WEST);
        panel.add(metricsPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setOpaque(true);
        card.setBackground(new Color(255, 248, 236));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(216, 200, 172), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(124, 98, 83));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        valueLabel.setForeground(new Color(50, 41, 37));
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 20));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JSplitPane createMainSplitPanel() {
        JPanel controlsPanel = createControlsPanel();
        JTabbedPane insightsTabs = createInsightsTabs();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlsPanel, insightsTabs);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setResizeWeight(0.34);
        splitPane.setDividerLocation(350);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel createControlsPanel() {
        JPanel wrap = new JPanel(new BorderLayout(10, 10));
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(350, 100));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setOpaque(true);
        formCard.setBackground(new Color(249, 242, 230));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(201, 183, 158), 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        fillRoles();
        styleInputs();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        int row = 0;
        addField(formCard, gbc, row++, "Username", usernameField);
        addField(formCard, gbc, row++, "Role", roleBox);
        addField(formCard, gbc, row++, "Service", serviceBox);
        addField(formCard, gbc, row++, dataFieldLabel, dataField);

        JPanel buttonRow = new JPanel(new GridBagLayout());
        buttonRow.setOpaque(false);

        JButton sendButton = createButton("Send Request", new Color(45, 117, 89), new Color(245, 253, 249));
        sendButton.addActionListener(e -> handleSubmit());

        JButton clearButton = createButton("Clear Form", new Color(122, 85, 52), new Color(255, 246, 238));
        clearButton.addActionListener(e -> clearForm());

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.weightx = 1;
        buttonConstraints.insets = new Insets(0, 0, 0, 8);
        buttonRow.add(sendButton, buttonConstraints);

        buttonConstraints.insets = new Insets(0, 0, 0, 0);
        buttonRow.add(clearButton, buttonConstraints);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 4, 6, 4);
        formCard.add(buttonRow, gbc);

        JLabel noteLabel = new JLabel("Analytics requests ignore payload automatically.");
        noteLabel.setForeground(new Color(97, 81, 69));
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        gbc.gridy = row + 1;
        gbc.insets = new Insets(6, 4, 0, 4);
        formCard.add(noteLabel, gbc);

        JPanel statusCard = new JPanel(new BorderLayout());
        statusCard.setOpaque(true);
        statusCard.setBackground(new Color(252, 247, 238));
        statusCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(208, 194, 171), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        trackerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        trackerLabel.setForeground(new Color(57, 59, 66));
        statusCard.add(trackerLabel, BorderLayout.CENTER);

        wrap.add(formCard, BorderLayout.CENTER);
        wrap.add(statusCard, BorderLayout.SOUTH);
        return wrap;
    }

    private JTabbedPane createInsightsTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("Activity", createActivityTab());
        tabs.addTab("History", createHistoryTab());
        tabs.addTab("Usage", createUsageTab());
        return tabs;
    }

    private JPanel createActivityTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(true);
        panel.setBackground(new Color(250, 246, 238));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        outputArea.setEditable(false);
        outputArea.setLineWrap(false);
        outputArea.setWrapStyleWord(false);
        outputArea.setBackground(new Color(27, 33, 40));
        outputArea.setForeground(new Color(222, 233, 244));
        outputArea.setCaretColor(new Color(222, 233, 244));
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setText("Gateway started. Waiting for requests.\n");

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createLineBorder(new Color(63, 73, 87), 1, true));

        JButton clearLogsButton = createButton("Clear Logs", new Color(74, 78, 90), new Color(242, 244, 248));
        clearLogsButton.addActionListener(e -> outputArea.setText("Gateway started. Waiting for requests.\n"));

        panel.add(outputScroll, BorderLayout.CENTER);
        panel.add(clearLogsButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(true);
        panel.setBackground(new Color(250, 246, 238));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTable historyTable = new JTable(historyModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(23);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(232, 220, 203));
        historyTable.getTableHeader().setForeground(new Color(54, 45, 39));

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(189, 172, 148), 1, true));

        JButton clearHistoryButton = createButton("Clear History", new Color(123, 82, 56), new Color(255, 246, 238));
        clearHistoryButton.addActionListener(e -> {
            historyModel.setRowCount(0);
            requestSequence = 0;
        });

        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(clearHistoryButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createUsageTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(true);
        panel.setBackground(new Color(250, 246, 238));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        usageArea.setEditable(false);
        usageArea.setLineWrap(false);
        usageArea.setBackground(new Color(36, 38, 44));
        usageArea.setForeground(new Color(218, 230, 220));
        usageArea.setFont(new Font("Consolas", Font.PLAIN, 13));

        JScrollPane usageScroll = new JScrollPane(usageArea);
        usageScroll.setBorder(BorderFactory.createLineBorder(new Color(76, 78, 82), 1, true));

        JButton refreshButton = createButton("Refresh Usage", new Color(61, 103, 86), new Color(246, 253, 250));
        refreshButton.addActionListener(e -> refreshUsageSummary());

        panel.add(usageScroll, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private void fillRoles() {
        List<String> roles = new ArrayList<>();
        for (String role : RoleConfig.getAvailableRoles().split(",")) {
            String cleaned = role.trim().toLowerCase();
            if (!cleaned.isEmpty()) {
                roles.add(cleaned);
            }
        }

        if (roles.isEmpty()) {
            roles.add("admin");
            roles.add("user");
            roles.add("guest");
        }

        roles.sort(Comparator.naturalOrder());
        for (String role : roles) {
            roleBox.addItem(role);
        }
    }

    private void styleInputs() {
        styleTextField(usernameField);
        styleTextField(dataField);
        styleCombo(roleBox);
        styleCombo(serviceBox);

        dataField.addActionListener(e -> handleSubmit());
    }

    private void registerListeners() {
        serviceBox.addActionListener(e -> updateDataFieldState());
    }

    private void updateDataFieldState() {
        String service = (String) serviceBox.getSelectedItem();
        boolean analyticsSelected = "analytics".equalsIgnoreCase(service);

        dataFieldLabel.setText(getDataFieldLabel(service));
        dataField.setEnabled(!analyticsSelected);
        dataField.setToolTipText(analyticsSelected ? "Payload is ignored for analytics." : "Enter details for the selected service.");

        if (analyticsSelected) {
            dataField.setText("");
        }
    }

    private String getDataFieldLabel(String service) {
        if (service == null) {
            return "Payload";
        }

        return switch (service.toLowerCase()) {
            case "user" -> "Profile Details";
            case "payment" -> "Payment Details";
            case "order" -> "Order Details";
            case "notification" -> "Message";
            case "analytics" -> "Analytics Input";
            default -> "Payload";
        };
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        JLabel fieldLabel = new JLabel(label);
        addField(panel, gbc, row, fieldLabel, component);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, JLabel fieldLabel, java.awt.Component component) {
        fieldLabel.setForeground(new Color(67, 52, 39));
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(255, 251, 245));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(186, 167, 144), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(255, 251, 245));
    }

    private void handleSubmit() {
        String username = usernameField.getText().trim();
        String role = (String) roleBox.getSelectedItem();
        String service = (String) serviceBox.getSelectedItem();
        String data = dataField.getText().trim();

        if (username.isBlank()) {
            styleStatusPill("INPUT", new Color(138, 70, 38), new Color(250, 229, 211));
            appendLine("[INPUT] Name cannot be empty.");
            return;
        }

        if (role == null || !RoleConfig.isValidRole(role)) {
            styleStatusPill("ROLE", new Color(138, 70, 38), new Color(250, 229, 211));
            appendLine("[AUTH] Invalid role selected.");
            return;
        }

        if (service == null || service.isBlank()) {
            styleStatusPill("INPUT", new Color(138, 70, 38), new Color(250, 229, 211));
            appendLine("[INPUT] Service is required.");
            return;
        }

        if ("analytics".equalsIgnoreCase(service)) {
            data = "";
        }

        Request request = new Request(username, role, service, data);
        Response response = apiGateway.handleRequest(request);
        int statusCode = response.getStatusCode();

        requestSequence++;
        String time = LocalDateTime.now().format(TIME_FORMAT);
        historyModel.addRow(new Object[]{
                requestSequence,
                time,
                username,
                role,
                service,
                statusCode,
                response.getMessage()
        });

        appendLine("[" + time + "] [REQUEST] user=" + username + " role=" + role + " service=" + service);
        appendLine("[" + time + "] [RESPONSE] Status=" + statusCode + " Message=" + response.getMessage());
        appendLine("----------------------------------------------------------------");

        updateTrackerMetrics(username);
        refreshUsageSummary();
        updateStatusFromCode(statusCode);
    }

    private void updateStatusFromCode(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            styleStatusPill("SUCCESS", new Color(24, 101, 63), new Color(221, 245, 231));
            return;
        }

        if (statusCode == 429 || statusCode == 403 || statusCode == 400) {
            styleStatusPill("BLOCKED", new Color(138, 70, 38), new Color(250, 229, 211));
            return;
        }

        styleStatusPill("ERROR", new Color(128, 41, 41), new Color(252, 222, 222));
    }

    private void styleStatusPill(String text, Color foreground, Color background) {
        statusPill.setText(text);
        statusPill.setForeground(foreground);
        statusPill.setBackground(background);
        statusPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foreground, 1, true),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
    }

    private void updateTrackerMetrics(String username) {
        RequestTracker tracker = RequestTracker.getInstance();
        totalRequestsValue.setText(String.valueOf(tracker.getTotalRequests()));
        currentUserRequestsValue.setText(String.valueOf(tracker.getUserRequestCount(username)));
        trackerLabel.setText("Total requests: " + tracker.getTotalRequests() + " | " + username + ": " + tracker.getUserRequestCount(username));
    }

    private void refreshUsageSummary() {
        usageArea.setText(RequestTracker.getInstance().getUsageSummary());
        usageArea.setCaretPosition(0);
    }

    private void clearForm() {
        usernameField.setText("");
        if (roleBox.getItemCount() > 0) {
            roleBox.setSelectedIndex(0);
        }
        serviceBox.setSelectedItem("user");
        dataField.setText("");
        usernameField.requestFocusInWindow();
        styleStatusPill("IDLE", new Color(74, 98, 110), new Color(224, 234, 240));
    }

    private void appendLine(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void installConsoleMirror() {
        if (originalSystemOut != null) {
            return;
        }

        originalSystemOut = System.out;
        OutputStream mirrorStream = new UiTextAreaOutputStream();
        OutputStream teeStream = new TeeOutputStream(originalSystemOut, mirrorStream);
        System.setOut(new PrintStream(teeStream, true, StandardCharsets.UTF_8));
    }

    private class UiTextAreaOutputStream extends OutputStream {
        private final StringBuilder lineBuffer = new StringBuilder();

        @Override
        public void write(int b) {
            if (b == '\r') {
                return;
            }

            if (b == '\n') {
                flushLine();
                return;
            }

            lineBuffer.append((char) b);
        }

        @Override
        public void flush() {
            // PrintStream can flush frequently; do not emit partial lines here.
        }

        @Override
        public void close() {
            flushLine();
        }

        private void flushLine() {
            if (lineBuffer.length() == 0) {
                return;
            }

            String line = lineBuffer.toString();
            lineBuffer.setLength(0);
            if (SwingUtilities.isEventDispatchThread()) {
                appendLine(line);
            } else {
                SwingUtilities.invokeLater(() -> appendLine(line));
            }
        }
    }

    private static class TeeOutputStream extends OutputStream {
        private final OutputStream primary;
        private final OutputStream secondary;

        private TeeOutputStream(OutputStream primary, OutputStream secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @Override
        public void write(int b) throws IOException {
            primary.write(b);
            secondary.write(b);
        }

        @Override
        public void flush() throws IOException {
            primary.flush();
            secondary.flush();
        }
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint paint = new GradientPaint(
                    0, 0, new Color(238, 220, 197),
                    getWidth(), getHeight(), new Color(208, 233, 221)
            );
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
