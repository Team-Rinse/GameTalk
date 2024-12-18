import javax.swing.*;
import java.awt.*;
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

        // "MOVE!" 버튼 초기화
        moveButton = new JButton("MOVE!");
        moveButton.setFont(new Font("Dunggeunmo", Font.PLAIN, 13));
        moveButton.setVisible(false);
        moveButton.addActionListener(e -> ClientSocket.sendMoveCommand(chatId));

        // 기존 buttonPanel에서 버튼 제거
        JPanel buttonPanel = (JPanel) getContentPane().getComponent(1);
        buttonPanel.remove(moveButton); // 제거
        setVisible(true);
    }

    // 준비 상태 업데이트
    public void updateReadyStatus(Map<String, Boolean> readyMap) {
        for (Map.Entry<String, Boolean> entry : readyMap.entrySet()) {
            String playerName = entry.getKey();
            boolean ready = entry.getValue();
            JLabel label = super.playerLabels.get(playerName);
            if (label != null) {
                // 준비 상태에 따라 색상 변경
                label.setForeground(ready ? Color.GREEN : Color.RED);
            }
        }
    }

    public void showCountDown(int count) {
        JLabel opacityBackground = new JLabel("");
        opacityBackground.setOpaque(true); // 배경색을 보이게 설정
        opacityBackground.setBackground(new Color(0, 0, 0, 128));
        opacityBackground.setBounds(0, 0, 400, 300);
        add(opacityBackground);

        JLabel countdownLabel = new JLabel("Racing Game");
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setFont(new Font("Dunggeunmo", Font.BOLD, 30));
        countdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countdownLabel.setBounds(0, 130, 400, 40);
        add(countdownLabel);

        setTitle("Racing Game - " + count + "초 후 시작!");
        setBackground(Color.black);
        repaint();
    }

    @Override
    protected void onReadyButtonClicked() {
        ClientSocket.sendRaceReadySignal(chatId);
        super.readyButton.setEnabled(false);
    }

    @Override
    protected void startGameUI(String chatId) {
        super.readyButton.setVisible(false);
        remove(super.readyPanel);

        gamePanel = new GamePanel();
        add(gamePanel);
        setTitle("Racing Game - Start!");
        int count = 1;
        int initialX = 20; // 초기 X 좌표
        int y = 20;
        for (Map.Entry<String, JLabel> entry : super.playerLabels.entrySet()) {
            String name = entry.getKey();
            JLabel character = new JLabel();
            ImageIcon charIcon = new ImageIcon(RacingGameFrame.class.getResource("/character/mon" + count + ".png"));
            Image charImg = charIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            character.setIcon(new ImageIcon(charImg));
            character.setBounds(initialX, y, 50, 50); // 초기 X 좌표 설정
            gamePanel.add(character);

            playerCharacters.put(name, character);
            count++;
            y += 70;
        }

        // "MOVE!" 버튼을 GamePanel에 추가
        moveButton.setBounds(150, 220, 100, 30); // 적절한 위치와 크기로 설정
        gamePanel.add(moveButton);

        moveButton.setVisible(true);
        moveButton.setEnabled(true);

        revalidate();
        repaint();
    }

    public void updatePlayerPosition(String playerName, int position) {
        JLabel character = playerCharacters.get(playerName);
        if (character != null) {
            int initialX = 20;
            double scaleFactor = 2.8;

            int newX = initialX + (int) (position * scaleFactor);
            character.setLocation(newX, character.getY());

            // 도착 지점 도달 시 처리
            if (position >= 100) {
                // 도착한 플레이어에게만 버튼 비활성화 (다른 플레이어는 계속 이동 가능)
                if (ClientSocket.name.equals(playerName)) {
                    moveButton.setEnabled(false);
                }
            }

            gamePanel.revalidate();
            gamePanel.repaint();
        }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
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

            g.setColor(Color.YELLOW);
            g.drawLine(finishLineX, 0, finishLineX, getHeight());
        }
    }
}
