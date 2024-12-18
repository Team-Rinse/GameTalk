import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;

public abstract class BaseGameFrame extends JFrame {
    protected HashMap<String, JLabel> playerLabels = new HashMap<>();
    protected JPanel readyPanel;
    protected JPanel buttonPanel;
    protected JPanel playerGridPanel;
    protected JButton readyButton;
    protected String chatId;
    public BaseGameFrame(String title, String chatId) {
        this.chatId = chatId;
        setTitle(title);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // 공통 UI 패널 설정
        readyPanel = new JPanel(new BorderLayout());
        readyPanel.setBackground(Color.WHITE);

        // 타이틀 라벨
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dunggeunmo", Font.PLAIN, 40));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        readyPanel.add(titleLabel, BorderLayout.NORTH);

        playerGridPanel = createPlayerGrid();
        readyPanel.add(playerGridPanel, BorderLayout.CENTER);

        // Ready 버튼
        buttonPanel = new JPanel();
        readyButton = new JButton("Ready");
        readyButton.setFont(new Font("Dunggeunmo", Font.PLAIN, 13));
        readyButton.addActionListener(e -> onReadyButtonClicked());

        buttonPanel.add(readyButton);

        add(readyPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getChatId() {
        return this.chatId;
    }

    private JPanel createPlayerGrid() {
        JPanel playerGridPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        playerGridPanel.setBackground(Color.WHITE);

        for (String playerName : ClientSocket.currentUsers) {
            JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            playerPanel.setBackground(Color.WHITE);

            JLabel isReady = new JLabel("● ");
            isReady.setForeground(Color.RED);
            isReady.setFont(new Font("Dunggeunmo", Font.PLAIN, 25));

            JLabel playerLabel = new JLabel(playerName);
            playerLabel.setFont(new Font("Dunggeunmo", Font.PLAIN, 15));

            playerLabels.put(playerName, isReady);

            playerPanel.add(isReady);
            playerPanel.add(playerLabel);
            playerGridPanel.add(playerPanel);
        }
        return playerGridPanel;
    }

    protected abstract void onReadyButtonClicked();

    protected abstract void startGameUI(String chatId);

    public void updateReadyStatus(HashMap<String, Boolean> readyMap) {
        for (var entry : readyMap.entrySet()) {
            String playerName = entry.getKey();
            boolean ready = entry.getValue();
            JLabel label = playerLabels.get(playerName);
            if (label != null) {
                label.setForeground(ready ? Color.GREEN : Color.RED);
            }
        }
    }
}
