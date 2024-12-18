import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class DrawingGameFrame extends BaseGameFrame {
    protected DrawingCanvas canvas;
    private boolean isDrawer = false;
    private int currentColor = Color.BLACK.getRGB();
    private String chatId;
    private Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.BLACK};

    private JLabel timerLabel;        // 남은 시간 표시
    private Timer gameTimer;          // 1분 타이머
    private int remainingTime = 60;   // 60초

    private JPanel bottomPanel;       // 하단 패널 (정답 입력창 or 지우개/전체 지우기)
    private JPanel colorPanel;
    private JPanel buttonPanel;
    private JPanel answerPanel;
    private JTextField answerField;   // 정답 입력 필드
    private JButton answerButton;     // 정답 전송 버튼
    private JButton eraserButton;     // 지우개 버튼
    private JButton clearButton;      // 전체 지우기 버튼

    private final Color backgroundColor = new Color(56, 153, 224);

    public DrawingGameFrame(String chatId) {
        super("Mini Catchmind", chatId);
        this.chatId = chatId;
        super.readyPanel.setBackground(backgroundColor);
        super.buttonPanel.setBackground(backgroundColor);
        super.playerGridPanel.setBackground(backgroundColor);
        setVisible(true);
    }

    @Override
    protected void onReadyButtonClicked() {
        ClientSocket.sendCatchmindReadySignal(chatId);
    }

    @Override
    protected void startGameUI(String chatId) {
        remove(super.readyPanel);
        remove(super.buttonPanel);
        super.readyButton.setVisible(false);

        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(backgroundColor);

        JLabel answerLabel = new JLabel("", SwingConstants.LEFT);
        answerLabel.setFont(new Font("Dunggeunmo", Font.PLAIN, 15));
        answerLabel.setForeground(Color.BLACK);
        topPanel.add(answerLabel, BorderLayout.WEST);

        timerLabel = new JLabel("00:60", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Dunggeunmo", Font.PLAIN, 15));
        timerLabel.setForeground(Color.BLACK);
        topPanel.add(timerLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 양 옆 패널
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(backgroundColor);
        leftPanel.setPreferredSize(new Dimension(20, 0));
        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(backgroundColor);
        rightPanel.setPreferredSize(new Dimension(20, 0));
        add(rightPanel, BorderLayout.EAST);

        // 캔버스 영역
        canvas = new DrawingCanvas();
        add(canvas, BorderLayout.CENTER);

        // 하단 패널 설정
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.setPreferredSize(new Dimension(0, 60)); // 고정 높이 설정

        // 색상 버튼 패널 초기화
        initializeColorPanel();

        // 버튼 패널 초기화 (지우개, 리셋 버튼)
        initializeButtonPanel();

        // 하단 패널에 colorPanel과 buttonPanel 추가는 모드에 따라 수행
        add(bottomPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
        startTimer();
    }

    // 색상 패널 초기화 메서드
    private void initializeColorPanel() {
        colorPanel = new JPanel(new GridLayout(1, colors.length, 0, 0));
        colorPanel.setBackground(backgroundColor);
        for (Color color : colors) {
            JButton button = new JButton();
            button.setBackground(color);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setPreferredSize(new Dimension(50, 30));
            colorPanel.add(button);

            button.addActionListener(e -> {
                if (isDrawer) {
                    currentColor = color.getRGB();
                    canvas.setPenColor(color);
                }
            });
        }
    }

    // 버튼 패널 초기화 메서드 (지우개, 리셋 버튼)
    private void initializeButtonPanel() {
        // 지우개 버튼
        ImageIcon eraserIcon = new ImageIcon(DrawingGameFrame.class.getResource("/icon/eraser.png"));
        Image eraserImg = eraserIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eraserButton = new JButton("");
        eraserButton.setIcon(new ImageIcon(eraserImg));
        eraserButton.setPreferredSize(new Dimension(30, 30));
        eraserButton.setMaximumSize(new Dimension(30, 30));
        eraserButton.setMinimumSize(new Dimension(30, 30));
        eraserButton.setBackground(backgroundColor);
        eraserButton.setForeground(Color.BLACK);
        eraserButton.setOpaque(true);
        eraserButton.setBorderPainted(false);
        eraserButton.addActionListener(e -> {
            if (isDrawer) {
                currentColor = Color.WHITE.getRGB();
                canvas.setPenColor(Color.WHITE);
            }
        });

        // 리셋 버튼
        clearButton = new JButton("리셋");
        clearButton.setFont(new Font("Dunggeunmo", Font.PLAIN, 12));
        clearButton.setBackground(backgroundColor);
        clearButton.setForeground(Color.BLACK);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);
        clearButton.setPreferredSize(new Dimension(80, 30));
        clearButton.setMaximumSize(new Dimension(80, 30));
        clearButton.setMinimumSize(new Dimension(80, 30));
        clearButton.addActionListener(e -> {
            if (isDrawer) { // 출제자만 리셋 버튼을 사용할 수 있도록 제한
                canvas.clearCanvas(); // 로컬에서도 캔버스 초기화
                ClientSocket.sendResetCanvasCommand(chatId);
            }
        });

        // 버튼 패널 (지우개, 리셋 버튼)
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(eraserButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0))); // 버튼 사이 간격
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createHorizontalGlue());
    }

    // 게임 타이머 시작
    private void startTimer() {
        gameTimer = new Timer(1000, e->{
            remainingTime--;
            timerLabel.setText("00:" + remainingTime);
            if(remainingTime <= 0) {
                gameTimer.stop();
                // 시간 만료 시 처리 로직 (서버에 알림, 게임 종료 등)
                // ClientSocket.sendTimeOver(chatId);
            }
        });
        gameTimer.start();
    }

    // 서버에서 DRAWER/VIEW_ONLY 정보 받으면 호출
    public void setMode(String mode) {
        SwingUtilities.invokeLater(() -> {
            bottomPanel.removeAll(); // 기존 컴포넌트 제거

            if (mode.equals("DRAWER")) {
                // DRAWER 모드 UI 구성
                isDrawer = true;
                canvas.enableDrawing(true);

                // 색상 패널 추가
                bottomPanel.add(colorPanel, BorderLayout.WEST);

                // 버튼 패널 (지우개, 리셋 버튼) 추가
                bottomPanel.add(buttonPanel, BorderLayout.EAST);
            } else {
                // VIEW_ONLY 모드 UI 구성
                isDrawer = false;
                canvas.enableDrawing(false);

                bottomPanel.add(createAnswerPanel(), BorderLayout.CENTER);
            }

            bottomPanel.revalidate();
            bottomPanel.repaint();
        });
    }

    // 정답 입력 패널 생성 메서드
    private JPanel createAnswerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0)); // 5px 간격
        panel.setBackground(backgroundColor);

        answerField = new JTextField();
        answerField.setFont(new Font("Dunggeunmo", Font.PLAIN, 14));
        answerField.setBackground(Color.WHITE);
        answerField.setBorder(BorderFactory.createCompoundBorder(
        answerField.getBorder(),
        BorderFactory.createEmptyBorder(0, 10, 0, 0))); // 왼쪽 여백 10px
        answerField.setPreferredSize(new Dimension(150, 30));
        answerField.setMaximumSize(new Dimension(150, 30));
        answerField.setMinimumSize(new Dimension(100, 30));

        answerButton = new JButton("제출");
        answerButton.setFont(new Font("Dunggeunmo", Font.PLAIN, 12));
        answerButton.setBackground(backgroundColor);
        answerButton.setForeground(Color.BLACK);
        answerButton.setOpaque(true);
        answerButton.setBorderPainted(false);
        answerButton.setPreferredSize(new Dimension(80, 30));
        answerButton.setMaximumSize(new Dimension(80, 30));
        answerButton.setMinimumSize(new Dimension(80, 30));
        answerButton.addActionListener(e -> {
            String answer = answerField.getText().trim();
            if (!answer.isEmpty()) {
                ClientSocket.sendAnswerAttempt(chatId, answer);
                answerField.setText("");
            }
        });

        panel.add(answerField, BorderLayout.CENTER);
        panel.add(answerButton, BorderLayout.EAST);

        return panel;
    }

    // 서버에서 DRAW_EVENT 받으면 다른 사람이 그린 선 표시
    public void drawLineFromServer(int x1, int y1, int x2, int y2, int rgb) {
        if (canvas != null) {
            canvas.drawLineFromServer(x1, y1, x2, y2, new Color(rgb));
        }
    }

    public void showAnswer(String answer) {
        JLabel answerLabel = new JLabel("정답: " + answer, SwingConstants.LEFT);
        answerLabel.setFont(new Font("Dunggeunmo", Font.PLAIN, 13));
        answerLabel.setForeground(Color.BLACK);
        add(answerLabel, BorderLayout.NORTH);
        revalidate();
        repaint();
        // 정답 맞추면 타이머 정지 등 추가 처리가 가능
        if(gameTimer != null) gameTimer.stop();
    }

    class DrawingCanvas extends JPanel {
        private Image bufferImage;
        private Graphics2D g2;
        private boolean canDraw = false;
        private int prevX, prevY;

        private Color penColor = Color.BLACK;

        public DrawingCanvas() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (!canDraw) return;
                    prevX = e.getX();
                    prevY = e.getY();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (!canDraw) return;
                    int x = e.getX();
                    int y = e.getY();
                    drawLine(prevX, prevY, x, y, penColor, true);
                    prevX = x;
                    prevY = y;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferImage == null) {
                bufferImage = createImage(getWidth(), getHeight());
                g2 = (Graphics2D) bufferImage.getGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g.drawImage(bufferImage, 0, 0, null);
        }

        public void drawLine(int x1, int y1, int x2, int y2, Color color, boolean sendToServer) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(x1, y1, x2, y2);
            repaint();

            if (sendToServer && isDrawer) {
                // 서버로 draw 이벤트 전송
                try {
                    ClientSocket.getDataOutputStream().writeUTF("DRAW_EVENT");
                    ClientSocket.getDataOutputStream().writeUTF(chatId);
                    ClientSocket.getDataOutputStream().writeInt(x1);
                    ClientSocket.getDataOutputStream().writeInt(y1);
                    ClientSocket.getDataOutputStream().writeInt(x2);
                    ClientSocket.getDataOutputStream().writeInt(y2);
                    ClientSocket.getDataOutputStream().writeInt(penColor.getRGB());
                    ClientSocket.getDataOutputStream().flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void drawLineFromServer(int x1, int y1, int x2, int y2, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(x1, y1, x2, y2);
            repaint();
        }

        public void enableDrawing(boolean enable) {
            this.canDraw = enable;
        }

        public void setPenColor(Color color) {
            this.penColor = color;
        }

        public void clearCanvas() {
            g2.setColor(Color.WHITE);
            g2.fillRect(0,0,getWidth(),getHeight());
            repaint();
        }
    }
}
