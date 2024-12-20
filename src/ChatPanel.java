import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class ChatPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    JPanel chatListPanel;
    private JScrollPane chatScrollPane;
    List<ChatListItem> chatItems = new ArrayList<>();

    public ChatPanel() {
        ClientSocket.setChatPanel(this);
        setBackground(Color.WHITE);
        setBounds(73, 0, 307, 613);
        setLayout(null);

        JLabel lblNewLabel = new JLabel("채팅");
        lblNewLabel.setBounds(18, 6, 34, 35);
        lblNewLabel.setFont(new Font("Kakao", Font.PLAIN, 17));
        add(lblNewLabel);

        // 채팅 목록을 담을 패널 생성
        chatListPanel = new JPanel();
        chatListPanel.setLayout(null);
        chatListPanel.setBackground(Color.WHITE);

        chatScrollPane = new JScrollPane(chatListPanel);
        chatScrollPane.setBounds(14, 40, 280, 560);
        chatScrollPane.setBorder(null);
        add(chatScrollPane);

//        // 초기 예시 아이템(기존 코드)
//        addChatEntry("chatId1", List.of("하여린"), "네프 과제 다했어??");
    }

    // 새로운 채팅 항목 추가 메서드
    public void addChatEntry(String chatId, List<String> participants, String lastMessage) {
        // 자신의 이름을 제외한 참가자 리스트 생성
        List<String> filteredParticipants = new ArrayList<>();
        for (String participant : participants) {
            if (!participant.equals(ClientSocket.name)) {
                filteredParticipants.add(participant);
            }
        }

        ChatListItem item = new ChatListItem(chatId, filteredParticipants, lastMessage);
        chatItems.add(item);

        int y = (chatItems.size() - 1) * 80 + 5; // 각 항목당 높이를 80이라 가정

        // 프로필 패널 추가
        JPanel profilePanel = item.getProfilePanel();
        profilePanel.setBounds(1, y, 60, 60);
        chatListPanel.add(profilePanel);

        // 이름 레이블 추가
        JLabel nameLabel = item.getNameLabel();
        nameLabel.setBounds(65, y + 12, 200, 16);
        chatListPanel.add(nameLabel);

        // 마지막 메시지 레이블 추가
        JLabel lastMessageLabel = item.getLastMessageLabel();
        lastMessageLabel.setBounds(65, y + 32, 200, 16);
        chatListPanel.add(lastMessageLabel);

        // MouseListener 추가
        profilePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openChatRoom(chatId, participants);
            }
        });

        nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openChatRoom(chatId, participants);
            }
        });

        // 패널 크기 갱신
        chatListPanel.setPreferredSize(new Dimension(280, y + 80));
        chatListPanel.revalidate();
        chatListPanel.repaint();
        revalidate();
        repaint();
    }

    // 특정 채팅 방 열기
    private void openChatRoom(String chatId, List<String> participants) {
        SwingUtilities.invokeLater(() -> {
            ChatRoom chatRoom = new ChatRoom(chatId, participants);
            chatRoom.setSize(307, 613); // 기존 크기와 일치하도록 설정
            chatRoom.setLocationRelativeTo(null);
            chatRoom.setVisible(true);
        });
    }

    // 특정 채팅 방 제거 메서드
    public void removeChatEntry(String chatId) {
        ChatListItem toRemove = null;

        // chatItems에서 해당 항목 찾기
        for (ChatListItem item : chatItems) {
            if (item.getChatId().equals(chatId)) {
                toRemove = item;
                break;
            }
        }

        if (toRemove != null) {
            chatItems.remove(toRemove);
            chatListPanel.remove(toRemove.getProfilePanel());
            chatListPanel.remove(toRemove.getNameLabel());
            chatListPanel.remove(toRemove.getLastMessageLabel());

            // 각 항목의 위치 재조정
            updateChatList();

            chatListPanel.revalidate();
            chatListPanel.repaint();
        }
    }

    private void updateChatList() {
        int y = 5; // 초기 Y 좌표
        for (ChatListItem item : chatItems) {
            item.getProfilePanel().setBounds(1, y, 60, 60);
            item.getNameLabel().setBounds(65, y + 12, 200, 16);
            item.getLastMessageLabel().setBounds(65, y + 32, 200, 16);
            y += 80;
        }

        chatListPanel.setPreferredSize(new Dimension(280, y));
        chatListPanel.revalidate();
        chatListPanel.repaint();
    }

    class ChatListItem {
        private String chatId;
        List<String> participants;
        private JPanel profilePanel;
        private JLabel nameLabel;
        private JLabel lastMessageLabel;

        public ChatListItem(String chatId, List<String> participants, String lastMessage) {
            this.chatId = chatId;
            this.participants = participants;
            this.profilePanel = new JPanel();
            this.nameLabel = new JLabel(String.join(", ", participants));
            this.lastMessageLabel = new JLabel(lastMessage);

            initializeUI();
            setProfileImages();
        }

        private void initializeUI() {
            // 프로필 패널 설정
            profilePanel.setLayout(null);
            profilePanel.setBackground(new Color(0, 0, 0, 0)); // 투명 배경

            // 이름 레이블 설정
            nameLabel.setFont(new Font("Kakao", Font.BOLD, 13));

            // 마지막 메시지 레이블 설정
            lastMessageLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
            lastMessageLabel.setForeground(new Color(116, 116, 116));
        }

        void setProfileImages() {
            int imageSize = 60; // 이미지 크기
            int overlap = 25;   // 겹치는 정도
            int totalImages = participants.size();

            // 프로필 패널 초기화
            profilePanel.removeAll();
            profilePanel.setPreferredSize(new Dimension(imageSize + (totalImages - 1) * (imageSize - overlap), imageSize));

            boolean profileAdded = false; // 프로필 이미지를 한 번만 추가하도록 제어

            for (int i = 0; i < totalImages; i++) {
                String userName = participants.get(i);
                byte[] profileData = ClientSocket.userProfiles.get(userName);
                JLabel profileLabel = new JLabel();

                Image profileImage;
                if (profileData != null && profileData.length > 0) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(profileData);
                    BufferedImage bufferedImg = null;
                    try {
                        bufferedImg = ImageIO.read(bais);
                    } catch (IOException e) {
                        e.printStackTrace();
                        bufferedImg = null; // 기본 이미지로 대체
                    }
                    profileImage = bufferedImg != null
                            ? bufferedImg.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH)
                            : loadDefaultImage().getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
                } else {
                    profileImage = loadDefaultImage().getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
                }

                // 프로필 이미지를 한 번만 추가
                if (!profileAdded) {
                    profileLabel.setIcon(new ImageIcon(profileImage));
                    profileLabel.setBounds(0, 0, imageSize, imageSize); // 겹치지 않도록 위치 설정
                    profilePanel.add(profileLabel);
                    profileAdded = true;
                }
            }

            profilePanel.revalidate();
            profilePanel.repaint();
        }

        private Image loadDefaultImage() {
            try {
                return new ImageIcon(ChatPanel.class.getResource("/icon/profile.png")).getImage();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }
        }

        public String getChatId() {
            return chatId;
        }

        public JPanel getProfilePanel() {
            return profilePanel;
        }

        public JLabel getNameLabel() {
            return nameLabel;
        }

        public JLabel getLastMessageLabel() {
            return lastMessageLabel;
        }
    }

}
