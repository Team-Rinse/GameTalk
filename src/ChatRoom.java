import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

public class ChatRoom extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel messagePanel;  // 채팅 메시지를 담는 패널
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
                            ClientSocket.chatPanel.removeChatEntry(getTitle());
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

        JScrollPane scrollPane = new JScrollPane(messagePanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
        messageInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if(messageInput.getText().equals("메시지 입력")) {
                    messageInput.setText("");
                    messageInput.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if(messageInput.getText().trim().isEmpty()) {
                    messageInput.setText("메시지 입력");
                    messageInput.setForeground(Color.GRAY);
                }
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
                JDialog dialog = new JDialog();
                dialog.setLayout(null);
                dialog.setSize(200, 200);
                dialog.setVisible(true);

                JButton racing = new JButton("RACING\nGAME");
                racing.setFont(new Font("Dunggeunmo", Font.PLAIN, 20));
                racing.setBounds(0, 0, 100, 200);
                racing.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ClientSocket.createRacingGameFrame(chatId);
                    }
                });
                dialog.add(racing);

                JButton drawing = new JButton("DRAWING\nGAME");
                drawing.setFont(new Font("Dunggeunmo", Font.PLAIN, 20));
                drawing.setBounds(100, 0, 100, 200);
                drawing.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ClientSocket.createDrawingGameFrame(chatId);
                    }
                });
                dialog.add(drawing);
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
                if(!messageInput.getText().trim().isEmpty() && !messageInput.getText().equals("메시지 입력")) {
                    String myMsg = messageInput.getText();
                    addMyMessage(myMsg);
                    messageInput.setText("");
                    ClientSocket.sendTextMessage(myMsg);
                }
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);

        // 테스트용 메시지 추가
        addOtherMessage("참여자1", "빼이~");
        addOtherMessage("참여자2", "GOAT");
        addMyMessage("빼이ㅋㅋ");
    }

    // 내 메시지(오른쪽 정렬, 노란 말풍선)
    private void addMyMessage(String text) {
        JPanel msgPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                JLabel tempLabel = createMessageBubble(text, new Color(0xFFEB3B), SwingConstants.RIGHT);
                Dimension labelSize = tempLabel.getPreferredSize();
                return new Dimension(labelSize.width + 20, labelSize.height + 10); // 메시지 패딩 추가
            }
        };
        msgPanel.setOpaque(false);

        // 메시지 말풍선
        JLabel messageLabel = createMessageBubble(text, new Color(0xFFEB3B), SwingConstants.RIGHT);
        msgPanel.add(messageLabel, BorderLayout.EAST);

        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10)); // 메시지 간 간격
        messagePanel.revalidate();
        messagePanel.repaint();
    }

    // 다른 사람 메시지(왼쪽 정렬, 흰색 말풍선, 프로필 이미지)
    private void addOtherMessage(String senderName, String text) {
        JPanel msgPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                JLabel tempLabel = createMessageBubble(text, Color.WHITE, SwingConstants.LEFT);
                Dimension labelSize = tempLabel.getPreferredSize();
                return new Dimension(labelSize.width + 60, labelSize.height + 10); // 메시지와 프로필 이미지 패딩 추가
            }
        };
        msgPanel.setOpaque(false);

        // 프로필 이미지
        JLabel profilePic = new JLabel();
        ImageIcon profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
        Image profileImage = profileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        profilePic.setIcon(new ImageIcon(profileImage));
        msgPanel.add(profilePic, BorderLayout.WEST);

        // 이름 + 메시지
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(senderName);
        nameLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
        textPanel.add(nameLabel);

        // 메시지 말풍선
        JLabel messageLabel = createMessageBubble(text, Color.WHITE, SwingConstants.LEFT);
        textPanel.add(messageLabel);

        msgPanel.add(textPanel, BorderLayout.CENTER);

        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10)); // 메시지 간 간격
        messagePanel.revalidate();
        messagePanel.repaint();
    }

    private void showEmojiPopup(JButton emojiButton) {
        // 팝업 패널과 스크롤바 추가
        JPanel emojiPanel = new JPanel(new GridLayout(4, 3, 5, 5)); // 4행 3열, 각 요소 간 간격 5px
        emojiPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 외곽 여백
        emojiPanel.setOpaque(true);
        emojiPanel.setBackground(Color.WHITE);

        // 이모티콘 목록 로드
        String[] emojiFiles = {
                "emo1.png", "emo2.png", "emo3.png",
                "emo4.png", "emo5.png", "emo6.png",
                "emo7.png", "emo8.png", "emo9.png",
                "emo10.png", "emo11.png", "emo12.png"
        };

        for (String emojiFile : emojiFiles) {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/emoji/" + emojiFile));
            Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaledImage);

            // 이모티콘 버튼 생성
            JButton emojiButtonItem = new JButton(resizedIcon);
            emojiButtonItem.setPreferredSize(new Dimension(50, 50)); // 버튼 크기 설정
            emojiButtonItem.setBorderPainted(false); // 버튼 테두리 제거
            emojiButtonItem.setContentAreaFilled(false); // 버튼 배경 제거
            emojiButtonItem.setFocusPainted(false); // 포커스 테두리 제거
            emojiButtonItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addImageMessage("나", "/emoji/" + emojiFile); // 선택된 이모티콘 메시지로 추가
                    ClientSocket.sendImageMessage("/emoji/" + emojiFile); // 서버로 이모티콘 메시지 전송
                }
            });
            emojiPanel.add(emojiButtonItem); // 버튼을 패널에 추가
        }

        // 스크롤 가능한 팝업 구성
        JScrollPane scrollPane = new JScrollPane(emojiPanel);
        scrollPane.setPreferredSize(new Dimension(200, 200)); // 팝업 크기 설정
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // 팝업 메뉴에 스크롤 패널 추가
        JPopupMenu emojiPopup = new JPopupMenu();
        emojiPopup.add(scrollPane);

        // 이모티콘 버튼 아래에 팝업 표시
        emojiPopup.show(emojiButton, 0, emojiButton.getHeight());
    }


    // 이미지 메시지(일단 다른 사람 메시지 스타일)
    private void addImageMessage(String senderName, String imagePath) {
        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);

        // 프로필 아이콘 생성 (기본 왼쪽 정렬)
        JLabel profilePic = new JLabel();
        ImageIcon profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
        Image profileImage = profileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        profilePic.setIcon(new ImageIcon(profileImage));

        // 텍스트 패널 (송신자 이름과 이모티콘 포함)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(senderName);
        nameLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
        textPanel.add(nameLabel);

        // 이미지 라벨 생성
        JLabel imageLabel = new JLabel();
        ImageIcon imgIcon = new ImageIcon(TalkApp.class.getResource(imagePath));
        Image img = imgIcon.getImage().getScaledInstance(100, 60, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(img));
        imageLabel.setOpaque(true);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 송신자별 스타일 적용
        if (senderName.equals("나")) {
            // 내 메시지: 오른쪽 정렬, 노란색 배경
            msgPanel.add(textPanel, BorderLayout.EAST); // 오른쪽 정렬
            imageLabel.setBackground(new Color(255, 240, 140)); // 노란색
        } else {
            // 상대방 메시지: 왼쪽 정렬, 흰색 배경
            msgPanel.add(profilePic, BorderLayout.WEST); // 프로필 왼쪽 정렬
            msgPanel.add(textPanel, BorderLayout.CENTER); // 텍스트 중간 정렬
            imageLabel.setBackground(Color.WHITE); // 흰색
        }

        textPanel.add(imageLabel);

        // 메시지 패널 추가
        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10)); // 간격 추가
        messagePanel.revalidate();
        messagePanel.repaint();
    }


    // 영상 메시지(썸네일로 표시, 비슷하게 이미지와 동일)
    private void addVideoMessage(String senderName, String videoThumbPath) {
        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);

        JLabel profilePic = new JLabel();
        ImageIcon profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
        Image profileImage = profileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        profilePic.setIcon(new ImageIcon(profileImage));
        msgPanel.add(profilePic, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(senderName);
        nameLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
        textPanel.add(nameLabel);

        // 영상 썸네일 라벨
        JLabel videoLabel = new JLabel();
        ImageIcon vidIcon = new ImageIcon(TalkApp.class.getResource(videoThumbPath));
        Image vidImg = vidIcon.getImage().getScaledInstance(100, 60, Image.SCALE_SMOOTH);
        videoLabel.setIcon(new ImageIcon(vidImg));
        videoLabel.setOpaque(true);
        videoLabel.setBackground(Color.WHITE);
        videoLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        textPanel.add(videoLabel);

        msgPanel.add(textPanel, BorderLayout.CENTER);

        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.revalidate();
        messagePanel.repaint();
    }

    private JLabel createMessageBubble(String text, Color bgColor, int alignment) {
        JLabel label = new JLabel("<html><body style='width:auto;'>" + text + "</body></html>") {
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                int textWidth = fm.stringWidth(text); // 텍스트 너비 + 패딩
                int maxWidth = 200; // 최대 말풍선 너비 제한
                int width = Math.min(textWidth, maxWidth); // 최대 너비 제한 적용
                int height = (int) Math.ceil((double) textWidth / maxWidth) * fm.getHeight() + 10; // 높이 계산
                return new Dimension(width, height);
            }
        };

        label.setFont(new Font("Kakao", Font.PLAIN, 14));
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        if (alignment == SwingConstants.RIGHT) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }

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