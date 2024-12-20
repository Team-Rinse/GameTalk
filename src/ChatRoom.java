import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChatRoom extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel messagePanel;  // 채팅 메시지를 담는 패널
    private JScrollPane scrollPane;
    private JTextField messageInput;
    private JButton sendButton;
    private String chatId;
    private List<String> participants;

    public ChatRoom(String chatId, List<String> participants) {
        this.chatId = chatId;
        this.participants = participants;

        String users = "";
        for(String s : participants) {
            if (!s.equals(ClientSocket.name)) { // 본인 이름 제외
                if (users.length() > 0) {
                    users += ", "; // 쉼표 추가
                }
                users += s;
            }
        }
        setTitle(users);
        setSize(380, 635);
        setBackground(new Color(0xCCDAE7));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 상단 바 영역
        JPanel topBar = new JPanel();
        topBar.setLayout(null);
        topBar.setPreferredSize(new Dimension(380, 60));
        topBar.setBackground(new Color(0xCCDAE7));
        panel.add(topBar, BorderLayout.NORTH);

        // 채팅방 이름
        JLabel chatRoomTitle = new JLabel(users);
        chatRoomTitle.setFont(new Font("Kakao", Font.BOLD, 18));
        chatRoomTitle.setBounds(10, 10, 80, 40);
        topBar.add(chatRoomTitle);

        // 참여자 수 표시 (예: "4")
        JLabel participantCount = new JLabel("4");
        participantCount.setFont(new Font("Kakao", Font.PLAIN, 13));
        participantCount.setForeground(Color.DARK_GRAY);
        participantCount.setBounds(60, 10, 20, 40);
        topBar.add(participantCount);

        JButton exitButton = new JButton("EXIT");
        exitButton.setBounds(230, 20, 60, 20);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 서버에 LEAVE_CHAT 요청 보내기
                    ClientSocket.getDataOutputStream().writeUTF("LEAVE_CHAT");
                    ClientSocket.getDataOutputStream().writeUTF(chatId); // 채팅방 ID 전송
                    ClientSocket.getDataOutputStream().flush();

                    // ChatPanel에서 채팅방 삭제
                    SwingUtilities.invokeLater(() -> {
                        if (ClientSocket.chatPanel != null) {
                            ClientSocket.chatPanel.removeChatEntry(chatId);
                        }
                    });

                    // 채팅방 닫기
                    dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        topBar.add(exitButton);

        // 중앙 메시지 영역 (스크롤 가능)
        messagePanel = new JPanel();
        messagePanel.setBackground(new Color(0xCCDAE7)); // 바탕과 같은 색
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 하단 입력 영역
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(307, 120));
        bottomPanel.setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // 여백 추가
        buttonPanel.setOpaque(false);

        messageInput = new JTextField();
        messageInput.setFont(new Font("Kakao", Font.PLAIN, 14));
        messageInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageInput.setBackground(Color.WHITE);
        messageInput.setForeground(Color.GRAY);
        messageInput.setText("메시지 입력");
        messageInput.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if(messageInput.getText().equals("메시지 입력")) {
                    messageInput.setText("");
                    messageInput.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if(messageInput.getText().trim().isEmpty()) {
                    messageInput.setText("메시지 입력");
                    messageInput.setForeground(Color.GRAY);
                }
            }
        });
        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(messageInput, BorderLayout.CENTER);

        // 이모티콘 버튼 추가
        ImageIcon emojiIcon = new ImageIcon(getClass().getResource("/emoji/emo.png"));
        Image emojiImg = emojiIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        JButton emojiButton = new JButton(new ImageIcon(emojiImg));
        emojiButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        emojiButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        emojiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEmojiPopup(emojiButton);
            }
        });
        buttonPanel.add(emojiButton);

        JButton gameButton = new JButton(new ImageIcon(getClass().getResource("/icon/game.png")));
        gameButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        gameButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog(ChatRoom.this, "게임 선택", true);
                dialog.setLayout(new GridLayout(1, 2, 10, 10));
                dialog.setSize(220, 100);
                dialog.setLocationRelativeTo(ChatRoom.this);

                JButton racing = new JButton("<html>RACING<br>GAME</html>");
                racing.setFont(new Font("Dunggeunmo", Font.PLAIN, 14));
                racing.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ClientSocket.createRacingGameFrame(chatId);
                        dialog.dispose();
                    }
                });
                dialog.add(racing);

                JButton drawing = new JButton("<html>DRAWING<br>GAME</html>");
                drawing.setFont(new Font("Dunggeunmo", Font.PLAIN, 14));
                drawing.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ClientSocket.createDrawingGameFrame(chatId);
                        dialog.dispose();
                    }
                });
                dialog.add(drawing);

                dialog.setVisible(true);
            }
        });
        buttonPanel.add(gameButton, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        sendButton = new JButton("전송");
        sendButton.setPreferredSize(new Dimension(100, 40));
        sendButton.setFont(new Font("Kakao", Font.PLAIN, 14));
        sendButton.setEnabled(true);
        sendButton.setBackground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
    }

    // ChatRoom 클래스 내에 추가
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && !message.equals("메시지 입력")) {
            String myMsg = message;
            addMyMessage(myMsg);
            messageInput.setText("");
            ClientSocket.sendTextMessage(chatId, myMsg);
        }
    }

    // 내 메시지(오른쪽 정렬, 노란 말풍선)
    void addMyMessage(String text) {
        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        msgPanel.setOpaque(false);

        JLabel messageLabel = createMessageBubble(text, new Color(0xFFEB3B));
        msgPanel.add(messageLabel);

        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.revalidate();
        messagePanel.repaint();

        // 스크롤을 맨 아래로 이동
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    // 다른 사람 메시지(왼쪽 정렬, 흰색 말풍선, 프로필 이미지)
    void addOtherMessage(String senderName, String text) {
        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        msgPanel.setOpaque(false);

        // 프로필 이미지 가져오기
        byte[] profileData = ClientSocket.userProfiles.get(senderName);
        Image profileImage;
        if (profileData != null) {
            profileImage = Toolkit.getDefaultToolkit().createImage(profileData);
            profileImage = profileImage.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        } else {
            ImageIcon profileIcon = new ImageIcon(getClass().getResource("/icon/profile.png"));
            profileImage = profileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        }

        JLabel profilePic = new JLabel(new ImageIcon(profileImage));
        profilePic.setBorder(new EmptyBorder(0, 0, 0, 10)); // 오른쪽 여백

        // 텍스트 패널
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setMaximumSize(new Dimension(220, Integer.MAX_VALUE)); // 폭 제한 설정

        JLabel nameLabel = new JLabel(senderName);
        nameLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
        textPanel.add(nameLabel);

        JLabel messageLabel = createMessageBubble(text, Color.WHITE);
        textPanel.add(messageLabel);

        // 프로필 사진과 텍스트 패널을 왼쪽에 배치
        msgPanel.add(profilePic);
        msgPanel.add(textPanel);

        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10)); // 간격 추가
        messagePanel.revalidate();
        messagePanel.repaint();

        // 스크롤을 맨 아래로 이동
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void showEmojiPopup(JButton emojiButton) {
        JDialog emojiDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(emojiButton), "이모티콘 선택", true);
        emojiDialog.setSize(300, 380);
        emojiDialog.setResizable(false);
        emojiDialog.setUndecorated(true);

        // 이모티콘 선택 창의 위치를 emojiButton 근처로 설정
        Point buttonLocation = emojiButton.getLocationOnScreen();
        int dialogX = buttonLocation.x + emojiButton.getWidth() / 2 - 150; // 중앙 정렬
        int dialogY = buttonLocation.y - 380; // 버튼 위에 표시
        emojiDialog.setLocation(dialogX, dialogY);

        emojiDialog.getContentPane().setBackground(new Color(245, 245, 245));
        emojiDialog.setLayout(null);

        JLabel label = new JLabel("이모티콘 선택", SwingConstants.CENTER);
        label.setFont(new Font("Kakao", Font.PLAIN, 15));
        label.setBounds(0, 10, 300, 30);
        emojiDialog.add(label);

        JPanel scrollPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 한 줄에 2개씩, 간격 10
        scrollPanel.setBackground(new Color(245, 245, 245));

        OptionPanel optionPanel = ClientSocket.optionPanel; // OptionPanel 참조
        if (optionPanel == null) return;

        for (Map.Entry<String, Integer> entry : optionPanel.purchasedEmojis.entrySet()) {
            String emojiName = entry.getKey();
            int count = entry.getValue();

            if (count <= 0) continue; // 재고 없는 경우 제외

            // 이모티콘 이미지 버튼 생성
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/emoji/" + emojiName + ".png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaledImage);

            JButton emojiButtonItem = new JButton(resizedIcon);
            emojiButtonItem.setPreferredSize(new Dimension(70, 70));
            emojiButtonItem.setBorderPainted(false);
            emojiButtonItem.setContentAreaFilled(false);
            emojiButtonItem.setFocusPainted(false);
            emojiButtonItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // 이모티콘 클릭 시 전송 및 재고 감소
            emojiButtonItem.addActionListener(e -> {
                try {
                    // 이모티콘 전송 후 재고 감소
                    optionPanel.addPurchasedEmoji(emojiName, -1);
                    byte[] imageBytes = loadImageAsByteArray("/emoji/" + emojiName + ".png");
                    addImageMessage("나", imageBytes);
                    ClientSocket.sendImageMessage(chatId, imageBytes);

                    // 이모티콘 선택 창 닫기 후 새로 열기 (갱신)
                    emojiDialog.dispose();
                    showEmojiPopup(emojiButton);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            // 이모티콘 아래에 개수 표시
            JLabel countLabel = new JLabel("x" + count, SwingConstants.CENTER);
            countLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
            countLabel.setForeground(Color.GRAY);

            // 이모티콘과 개수를 패널에 추가 (수직 정렬)
            JPanel emojiItemPanel = new JPanel();
            emojiItemPanel.setLayout(new BoxLayout(emojiItemPanel, BoxLayout.Y_AXIS));
            emojiItemPanel.setOpaque(false);

            emojiItemPanel.add(emojiButtonItem);
            emojiItemPanel.add(Box.createVerticalStrut(5)); // 이모티콘과 수량 사이 간격
            emojiItemPanel.add(countLabel);

            scrollPanel.add(emojiItemPanel);
        }

        JScrollPane scrollPane = new JScrollPane(scrollPanel);
        scrollPane.setBounds(10, 50, 280, 260);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null); // 테두리 제거
        emojiDialog.add(scrollPane);

        JButton closeButton = new JButton("닫기");
        closeButton.setFont(new Font("Kakao", Font.PLAIN, 12));
        closeButton.setBounds(100, 330, 100, 30);
        closeButton.addActionListener(e -> emojiDialog.dispose());
        emojiDialog.add(closeButton);

        emojiDialog.setVisible(true);
    }

    private byte[] loadImageAsByteArray(String imagePath) throws IOException {
        // 클래스 리소스에서 이미지를 읽음
        InputStream is = getClass().getResourceAsStream(imagePath);
        if (is == null) {
            throw new IOException("Image file not found: " + imagePath);
        }

        // 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    // 이미지 메시지
    void addImageMessage(String senderName, byte[] imageData) {
        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);

        try {
            // byte[]를 BufferedImage로 변환
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage bufferedImage = ImageIO.read(bais);
            if (bufferedImage == null) {
                throw new IOException("이미지 데이터를 읽을 수 없습니다.");
            }

            ImageIcon imgIcon = new ImageIcon(bufferedImage);
            Image img = imgIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(img);

            JLabel imageLabel = new JLabel(scaledIcon);
            imageLabel.setOpaque(false);
            imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            if (senderName.equals("나")) {
                // 내 메시지: 오른쪽 정렬, 노란색 배경
                imageLabel.setBackground(new Color(255, 240, 140)); // 노란색
                msgPanel.add(imageLabel, BorderLayout.EAST);
            } else {
                // 상대방 메시지: 왼쪽 정렬, 흰색 배경 + 프로필 이미지
                JLabel profilePic = new JLabel();
                ImageIcon profileIcon = new ImageIcon(getClass().getResource("/icon/profile.png"));
                Image profileImage = profileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                profilePic.setIcon(new ImageIcon(profileImage));
                msgPanel.add(profilePic, BorderLayout.WEST);

                JPanel textPanel = new JPanel();
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                textPanel.setOpaque(false);

                JLabel nameLabel = new JLabel(senderName);
                nameLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
                textPanel.add(nameLabel);

                imageLabel.setBackground(Color.WHITE); // 흰색
                textPanel.add(imageLabel);

                msgPanel.add(textPanel, BorderLayout.CENTER);
            }

            // 메시지 패널 추가
            messagePanel.add(msgPanel);
            messagePanel.add(Box.createVerticalStrut(10)); // 간격 추가
            messagePanel.revalidate();
            messagePanel.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JLabel createMessageBubble(String text, Color bgColor) {
        JLabel label = new JLabel("<html><body style='max-width:200px; padding:5px;'>" + text + "</body></html>");
        label.setFont(new Font("Kakao", Font.PLAIN, 14));
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }

    public String getChatId() {
        return chatId;
    }

    // 시스템 메시지 추가
    public void addSystemMessage(String text) {
        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);

        JLabel messageLabel = new JLabel("<html><body style='width:auto; color:gray;'>" + text + "</body></html>");
        messageLabel.setFont(new Font("Kakao", Font.ITALIC, 12));
        msgPanel.add(messageLabel, BorderLayout.CENTER);

        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10)); // 메시지 간 간격
        messagePanel.revalidate();
        messagePanel.repaint();
    }

}