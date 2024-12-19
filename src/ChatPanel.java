
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
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

        // 초기 예시 아이템(기존 코드)
        addChatEntry("chatId1", List.of("하여린"), "네프 과제 다했어??");
    }

    // 새로운 채팅 항목 추가 메서드
    public void addChatEntry(String chatId, List<String> participants, String lastMessage) {
        ChatListItem item = new ChatListItem(chatId, participants, lastMessage);
        chatItems.add(item);

        int y = (chatItems.size() - 1) * 80 + 5; // 각 항목당 높이를 80이라 가정

        // 프로필 버튼 추가
        JButton profileButton = item.getProfileButton();
        profileButton.setBounds(1, y, 60, 60);
        chatListPanel.add(profileButton);

        // 이름 레이블 추가
        JLabel nameLabel = item.getNameLabel();
        nameLabel.setBounds(65, y + 12, 200, 16);
        chatListPanel.add(nameLabel);

        // 마지막 메시지 레이블 추가
        JLabel lastMessageLabel = item.getLastMessageLabel();
        lastMessageLabel.setBounds(65, y + 32, 200, 16);
        chatListPanel.add(lastMessageLabel);

        // ActionListener 추가
        ActionListener openChatRoomListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openChatRoom(chatId, participants);
            }
        };
        profileButton.addActionListener(openChatRoomListener);
        nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openChatRoomListener.actionPerformed(null);
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
            chatListPanel.remove(toRemove.getProfileButton());
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
            item.getProfileButton().setBounds(1, y, 60, 60);
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
        private JButton profileButton;
        private JLabel nameLabel;
        private JLabel lastMessageLabel;

        public ChatListItem(String chatId, List<String> participants, String lastMessage) {
            this.chatId = chatId;
            this.participants = participants;
            this.profileButton = new JButton();
            this.nameLabel = new JLabel(String.join(", ", participants));
            this.lastMessageLabel = new JLabel(lastMessage);

            initializeUI();
        }

        private void initializeUI() {
            // 프로필 이미지 설정
            setProfileImages();

            // 이름 레이블 설정
            nameLabel.setFont(new Font("Kakao", Font.BOLD, 13));

            // 마지막 메시지 레이블 설정
            lastMessageLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
            lastMessageLabel.setForeground(new Color(116, 116, 116));

            // 프로필 버튼 설정
            profileButton.setContentAreaFilled(false);
            profileButton.setBorderPainted(false);
            profileButton.setFocusPainted(false);
        }

        void setProfileImages() {
            // 참여자들의 프로필 이미지를 조합하여 표시
            int imageSize = 30;
            int overlap = 10;
            int totalImages = participants.size();

            BufferedImage combinedImage = new BufferedImage(
                    imageSize + (totalImages - 1) * (imageSize - overlap),
                    imageSize,
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics g = combinedImage.getGraphics();

            for (int i = 0; i < totalImages; i++) {
                String userName = participants.get(i);
                byte[] profileData = ClientSocket.userProfiles.get(userName);
                Image profileImage;
                if (profileData != null && profileData.length > 0) {
                    profileImage = Toolkit.getDefaultToolkit().createImage(profileData);
                } else {
                    ImageIcon defaultIcon = new ImageIcon(getClass().getResource("/icon/profile.png"));
                    profileImage = defaultIcon.getImage();
                }
                profileImage = profileImage.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
                g.drawImage(profileImage, i * (imageSize - overlap), 0, null);
            }
            g.dispose();

            profileButton.setIcon(new ImageIcon(combinedImage));
        }

        public String getChatId() {
            return chatId;
        }

        public JButton getProfileButton() {
            return profileButton;
        }

        public JLabel getNameLabel() {
            return nameLabel;
        }

        public JLabel getLastMessageLabel() {
            return lastMessageLabel;
        }
    }

}
