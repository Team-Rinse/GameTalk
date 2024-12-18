
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JPanel chatListPanel;
    private JScrollPane chatScrollPane;
    private List<ChatListItem> chatItems = new ArrayList<>();

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
        addChatEntry("하여린", "네프 과제 다했어??");
    }

    // 새로운 채팅 항목 추가 메서드
    public void addChatEntry(String friendName, String lastMessage) {
        int y = chatItems.size() * 80 + 5; // 각 항목당 높이를 80이라 가정
        
        // 프로필 아이콘
        JButton myProfileButton = new JButton();
        ImageIcon profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
        Image profileImage = profileIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        myProfileButton.setIcon(new ImageIcon(profileImage));
        myProfileButton.setBounds(1, y, 60, 60);
        myProfileButton.setContentAreaFilled(false); 
        myProfileButton.setBorderPainted(false);    
        myProfileButton.setFocusPainted(false); 
        chatListPanel.add(myProfileButton);
        
        JLabel friendNameLabel = new JLabel(friendName);
        friendNameLabel.setFont(new Font("Kakao", Font.BOLD, 13));
        friendNameLabel.setBounds(65, y + 12, 200, 16);
        chatListPanel.add(friendNameLabel);
        
        JLabel lastMessageLabel = new JLabel(lastMessage);
        lastMessageLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
        lastMessageLabel.setForeground(new Color(116, 116, 116));
        lastMessageLabel.setBounds(65, y + 32, 200, 16);
        chatListPanel.add(lastMessageLabel);
        
        // 익명 클래스를 사용하여 ActionListener 추가
        ActionListener openChatRoomListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ChatRoom 열기
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ChatRoom chatRoom = new ChatRoom(friendName, List.of(ClientSocket.name, friendName)); // 예시로 이름만 전달
                        chatRoom.setSize(307, 613);
                        chatRoom.setLocationRelativeTo(null);
                        chatRoom.setVisible(true);
                    }
                });
            }
        };
        myProfileButton.addActionListener(openChatRoomListener);

        // 라벨에 MouseListener 추가
        friendNameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openChatRoomListener.actionPerformed(null); // 버튼과 동일한 로직 실행
            }
        });
        
        // chatItems 리스트에 관리용 객체 추가(필요시)
        chatItems.add(new ChatListItem(friendName, lastMessage, myProfileButton, friendNameLabel, lastMessageLabel));

        // 패널 크기 갱신
        chatListPanel.setPreferredSize(new Dimension(280, y + 80));
        chatListPanel.revalidate();
        chatListPanel.repaint();
        revalidate();
        repaint();
    }
    
    // 특정 채팅 친구의 마지막 메시지 업데이트 메서드(옵션)
    public void updateLastMessage(String friendName, String lastMessage) {
        for (ChatListItem item : chatItems) {
            if (item.getFriendName().equals(friendName)) {
                item.getLastMessageLabel().setText(lastMessage);
                return;
            }
        }
        // 만약 해당 채팅이 없으면 새로 추가할 수도 있음
        addChatEntry(friendName, lastMessage);
    }
    
    public void removeChatEntry(String friendName) {
        ChatListItem toRemove = null;

        // chatItems에서 해당 항목 찾기
        for (ChatListItem item : chatItems) {
            if (item.getFriendName().equals(friendName)) {
                toRemove = item;
                break;
            }
        }

        if (toRemove != null) {
            chatItems.remove(toRemove);
            chatListPanel.remove(toRemove.profileButton);
            chatListPanel.remove(toRemove.nameLabel);
            chatListPanel.remove(toRemove.lastMessageLabel);

            chatListPanel.revalidate();
            chatListPanel.repaint();
        }
    }

    private void updateChatList() {
        int y = 5; // 초기 Y 좌표
        for (ChatListItem item : chatItems) {
            item.profileButton.setBounds(1, y, 60, 60);
            item.nameLabel.setBounds(65, y + 12, 200, 16);
            item.lastMessageLabel.setBounds(65, y + 32, 200, 16);
            y += 80;
        }

        chatListPanel.setPreferredSize(new Dimension(280, y));
        chatListPanel.revalidate();
        chatListPanel.repaint();
    }

    // 필요시 ChatListItem 내부 클래스
    class ChatListItem {
        private String friendName;
        private String lastMessage;
        private JButton profileButton;
        private JLabel nameLabel;
        private JLabel lastMessageLabel;

        public ChatListItem(String friendName, String lastMessage, JButton profileButton, JLabel nameLabel, JLabel lastMessageLabel) {
            this.friendName = friendName;
            this.lastMessage = lastMessage;
            this.profileButton = profileButton;
            this.nameLabel = nameLabel;
            this.lastMessageLabel = lastMessageLabel;
        }

        public String getFriendName() { return friendName; }
        public JLabel getLastMessageLabel() { return lastMessageLabel; }
    }
}
