import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        super.setSize(300, 300);
        this.chatId = chatId;

        // "MOVE!" 버튼 초기화
        moveButton = new JButton("MOVE!");
        moveButton.setFont(new Font("Dunggeunmo", Font.PLAIN, 13));
        moveButton.setVisible(false);
        moveButton.addActionListener(e -> ClientSocket.sendMoveCommand(chatId));

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
        // 기존 오버레이 및 카운트다운 라벨 제거
        getContentPane().removeAll();

        JLabel opacityBackground = new JLabel("");
        opacityBackground.setOpaque(true); // 배경색을 보이게 설정
        opacityBackground.setBackground(new Color(0, 0, 0, 128));
        opacityBackground.setBounds(0, 0, 300, 300);
        add(opacityBackground);

        JLabel countdownLabel = new JLabel(String.valueOf(count), SwingConstants.CENTER);
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setFont(new Font("Dunggeunmo", Font.BOLD, 50));
        countdownLabel.setBounds(0, 110, 300, 50);
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

        // 프레임 크기 조정: 이미 300x300으로 설정됨
        gamePanel = new GamePanel();
        add(gamePanel);
        setTitle("Racing Game - Start!");
        int count = 1;
        int initialX = 20; // 초기 X 좌표
        int y = 20;

        // 플레이어 이름을 정렬하여 일관된 순서로 캐릭터 할당
        List<String> sortedPlayerNames = new ArrayList<>(super.playerLabels.keySet());
        Collections.sort(sortedPlayerNames);

        for (String name : sortedPlayerNames) {
            JLabel character = new JLabel();
            ImageIcon charIcon = new ImageIcon(RacingGameFrame.class.getResource("/character/mon" + count + ".png"));
            Image charImg = charIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            character.setIcon(new ImageIcon(charImg));
            character.setBounds(initialX, y, 50, 50); // 초기 X 좌표 설정 (높이 50으로 수정)
            gamePanel.add(character);

            playerCharacters.put(name, character);
            count++;
            y += 70; // 각 캐릭터의 Y 좌표 간격 조정
        }

        // "MOVE!" 버튼을 GamePanel에 추가
        moveButton.setBounds(100, 220, 100, 30); // 프레임 너비 300에 맞게 중앙 하단에 배치
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
            double scaleFactor = 2.0;

            int newX = initialX + (int) (position * scaleFactor);
            if (newX > 230) {
                newX = 230;
            }
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
        // 승자 다이얼로그를 별도의 스레드에서 실행하여 GUI 응답성을 유지
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, winnerName + "님이 우승했습니다!");
            moveButton.setEnabled(false);
            closeWindow();
        });
    }

    class GamePanel extends JPanel {
        private Image backgroundImage;
        private Image flagImage;

        public GamePanel() {
            setLayout(null);
            try {
                backgroundImage = new ImageIcon(RacingGameFrame.class.getResource("/background/grass.jpg")).getImage();
                flagImage = new ImageIcon(RacingGameFrame.class.getResource("/icon/flag.png")).getImage();
                flagImage = flagImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 배경 이미지 그리기
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            // 도착 지점 영역 그리기
            int finishLineX = 230; // 프레임 너비 300에 맞게 도착선 X 좌표 설정
            g.setColor(Color.GRAY);
            g.fillRect(finishLineX, 0, 10, getHeight()); // 도착선 두께를 10으로 설정

            g.setColor(Color.YELLOW);
            g.drawLine(finishLineX, 0, finishLineX, getHeight());

            // 깃발 이미지를 우측 상단에 그리기
            if (flagImage != null) {
                int flagX = getWidth() - flagImage.getWidth(null) - 10; // 오른쪽 여백 10
                int flagY = 10; // 위쪽 여백 10
                g.drawImage(flagImage, flagX, flagY, this);
            }
        }
    }
}
