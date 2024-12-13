import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        bottomPanel.setPreferredSize(new Dimension(307, 50));
        bottomPanel.setBackground(Color.WHITE);

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

        sendButton = new JButton("전송");
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

    // 이미지 메시지(일단 다른 사람 메시지 스타일)
    private void addImageMessage(String senderName, String imagePath) {
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

        // 이미지 라벨
        JLabel imageLabel = new JLabel();
        ImageIcon imgIcon = new ImageIcon(TalkApp.class.getResource(imagePath));
        Image img = imgIcon.getImage().getScaledInstance(100, 60, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(img));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        textPanel.add(imageLabel);

        msgPanel.add(textPanel, BorderLayout.CENTER);

        messagePanel.add(msgPanel);
        messagePanel.add(Box.createVerticalStrut(10));
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

}
