import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class RacingGameFrame extends BaseGameFrame {
    private HashMap<String, JLabel> playerCharacters = new HashMap<>();
    private JPanel readyPanel;
    private GamePanel gamePanel;
    private JButton readyButton;
    private JButton moveButton;
    private String chatId;

    public RacingGameFrame(String chatId) {
        super("Racing Game", chatId);
        this.chatId = chatId;
        moveButton = new JButton("MOVE!");
        moveButton.setFont(new Font("Dunggeunmo", Font.PLAIN, 13));
        moveButton.setVisible(false);
        moveButton.addActionListener(e -> ClientSocket.sendMoveCommand(chatId));

        JPanel buttonPanel = (JPanel) getContentPane().getComponent(1);
        buttonPanel.add(moveButton);
        setVisible(true);
    }

    // 준비 상태 업데이트
    public void updateReadyStatus(Map<String, Boolean> readyMap) {
        for (Map.Entry<String, Boolean> entry : readyMap.entrySet()) {
            String playerName = entry.getKey();
            boolean ready = entry.getValue();
            JLabel label = super.playerLabels.get(playerName);
            if (label != null) {
                // ready하면 초록색
                label.setForeground(ready ? Color.GREEN : Color.RED);
            }
        }
    }

    public void showCountDown(int count) {
        JLabel opacityBackground = new JLabel("");
        opacityBackground.setBackground(new Color(0, 0, 0, 128));
        opacityBackground.setBounds(0, 0, 400, 300);
        add(opacityBackground);

        JLabel countdownLabel = new JLabel("Racing Game");

        setTitle("Racing Game - " + count + "초 후 시작!");
        setBackground(Color.black);
    }

    @Override
    protected void onReadyButtonClicked() {
        ClientSocket.sendRaceReadySignal(chatId);
        super.readyButton.setEnabled(false);
    }

    @Override
    protected void startGameUI(String chatId) {
        super.readyButton.setVisible(false);
        moveButton.setVisible(true);
        remove(super.readyPanel);

        gamePanel = new GamePanel();
        add(gamePanel);
        moveButton.setEnabled(true);
        setTitle("Racing Game - Start!");
        int count = 1;
        int x = 20;
        int y = 20;
        for (Map.Entry<String, JLabel> entry : super.playerLabels.entrySet()) {
            String name = entry.getKey();
            JLabel character = new JLabel();
            ImageIcon charIcon = new ImageIcon(RacingGameFrame.class.getResource("/character/mon" + count + ".png"));
            Image charImg = charIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            character.setIcon(new ImageIcon(charImg));
            character.setBounds(x, y, 50, 50);
            gamePanel.add(character);

            playerCharacters.put(name, character);
            count++;
            y += 70;
        }
        revalidate();
        repaint();
    }

    public void updatePlayerPosition(String playerName, int position) {
        JLabel character = playerCharacters.get(playerName);
        character.setLocation(character.getX()+3, character.getY());
        if(position == 100) return;
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    public void showRaceResult(String winnerName) {
        JOptionPane.showMessageDialog(this, winnerName + "님이 우승했습니다!");
        moveButton.setVisible(false);
        readyButton.setVisible(false);
    }

    class GamePanel extends JPanel {
        private Image backgroundImage;

        public GamePanel() {
            setLayout(null);
            try {
                backgroundImage = new ImageIcon(RacingGameFrame.class.getResource("/background/grass.jpg")).getImage();
            } catch (Exception e) {}
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            int finishLineX = getWidth() - 100; // 도착 지점의 X 좌표
            g.setColor(new Color(255, 200, 200)); // 연한 빨간색
            g.fillRect(finishLineX, 0, 100, getHeight());

            g.setColor(Color.yellow);
            g.drawLine(finishLineX, 0, finishLineX, getHeight());
        }
    }
}
