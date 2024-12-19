import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private MyProfile myProfile;

    public JLabel nameLabel;          // 이름을 표시할 JLabel
    public JLabel profilePicLabel;    // 프로필 이미지를 표시할 JLabel
    public JLabel statusMessageLabel; // 상태 메시지를 표시할 JLabel

    private JPanel friendListPanel;  // 친구 리스트를 보여줄 패널
    private JScrollPane friendScrollPane;

    private JButton startChatButton;
    private JButton createChatButton;
    private boolean showingCheckBoxes = false; // 체크박스 표시 상태
    private List<JCheckBox> friendCheckBoxes = new ArrayList<>(); // 체크박스 관리를 위한 리스트

    public MainPanel(String userName) {
        ClientSocket.setMainPanel(this);
        setBackground(Color.WHITE);
        setBounds(73, 0, 307, 613);
        setLayout(null);

        // 초기 프로필 이미지 설정
        ImageIcon profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
        Image profileImage = profileIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);

        profilePicLabel = new JLabel(new ImageIcon(profileImage));
        profilePicLabel.setBounds(14, 40, 60, 60);
        profilePicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MyProfile(MainPanel.this);
            }
        });
        add(profilePicLabel);

        // 이름을 표시할 JLabel을 생성합니다.
        nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Kakao", Font.BOLD, 14));
        nameLabel.setBounds(80, 40, 150, 16); // 이름 위치
        add(nameLabel);

        // 상태 메시지를 표시할 JLabel을 생성합니다.
        statusMessageLabel = new JLabel("상태 메시지를 입력하세요");
        statusMessageLabel.setFont(new Font("Kakao", Font.ITALIC, 12));
        statusMessageLabel.setForeground(Color.GRAY);
        statusMessageLabel.setBounds(80, 60, 200, 16); // 상태 메시지 위치
        add(statusMessageLabel);

        JLabel line = new JLabel("");
        line.setBackground(new Color(240, 240, 240));
        line.setBounds(18, 108, 268, 2);
        line.setOpaque(true);
        add(line);

        startChatButton = new JButton("Start Chat");
        startChatButton.setBounds(186, 9, 100, 30);
        startChatButton.addActionListener(e -> {
            // Start Chat 버튼 클릭 시 체크박스 표시 상태 토글
            showingCheckBoxes = !showingCheckBoxes;
            updateFriends(ClientSocket.currentUsers);

            if (showingCheckBoxes) {
                // 체크박스를 보여주는 상태가 되면, "Create Chat" 버튼 활성화
                createChatButton.setVisible(true);
            } else {
                // 체크박스 숨김 상태면 Create Chat 버튼 숨기기
                createChatButton.setVisible(false);
            }
        });
        add(startChatButton);

        createChatButton = new JButton("Create Chat");
        createChatButton.setBounds(103, 242, 100, 30);
        createChatButton.setVisible(false);
        createChatButton.addActionListener(e -> {
            // 체크박스에서 선택한 사용자 목록을 서버에 전송
            List<String> selectedUsers = new ArrayList<>();
            for (JCheckBox cb : friendCheckBoxes) {
                if (cb.isSelected()) {
                    selectedUsers.add(cb.getText());
                }
            }

            if (!selectedUsers.isEmpty()) {
                try {
                    DataOutputStream os = ClientSocket.getDataOutputStream();
                    os.writeUTF("CREATE_CHAT");
                    os.writeInt(selectedUsers.size());
                    for (String user : selectedUsers) {
                        os.writeUTF(user);
                    }
                    os.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            // 체크박스 모드 비활성화
            showingCheckBoxes = false;
            updateFriends(ClientSocket.currentUsers);
        });
        add(createChatButton);

        JLabel friendListLabel = new JLabel("친구");
        friendListLabel.setBounds(0, 0, 61, 16);
        friendListLabel.setForeground(new Color(117, 117, 117));
        friendListLabel.setFont(new Font("Kakao", Font.PLAIN, 13));

        friendListPanel = new JPanel();
        friendListPanel.setLayout(null);
        friendListPanel.setBackground(Color.WHITE);
        friendListPanel.add(friendListLabel);

        friendScrollPane = new JScrollPane(friendListPanel);
        friendScrollPane.setBounds(18, 130, 268, 400);
        friendScrollPane.setBorder(null);
        add(friendScrollPane);

        ClientSocket.requestUserList();
    }

    // 친구 목록 업데이트 메서드
    public void updateFriends(List<String> friends) {
        friendListPanel.removeAll();
        friendCheckBoxes.clear();

        int y = 10;
        for (String friend : friends) {
            if (friend.equals(ClientSocket.name)) {
                continue;
            }

            // 프로필 이미지 가져오기
            byte[] profileData = ClientSocket.userProfiles.get(friend);
            Image friendImg;
            if (profileData != null) {
                friendImg = Toolkit.getDefaultToolkit().createImage(profileData);
                friendImg = friendImg.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            } else {
                ImageIcon defaultIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
                friendImg = defaultIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            }

            // 프로필 아이콘 버튼
            JButton friendButton = new JButton(new ImageIcon(friendImg));
            friendButton.setFocusPainted(false);
            friendButton.setContentAreaFilled(false);
            friendButton.setBorderPainted(false);
            friendButton.setBounds(0, y, 50, 50);
            friendListPanel.add(friendButton);

            // 이름 라벨
            JLabel friendNameLabel = new JLabel(friend);
            friendNameLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
            friendNameLabel.setBounds(60, y + 17, 200, 16);
            friendListPanel.add(friendNameLabel);

            if (showingCheckBoxes) {
                JCheckBox cb = new JCheckBox();
                cb.setText(friend);
                cb.setBounds(230, y + 13, 25, 25);
                cb.setOpaque(false);
                friendListPanel.add(cb);
                friendCheckBoxes.add(cb);
            }

            y += 60;
        }

        friendListPanel.setPreferredSize(new java.awt.Dimension(268, y));
        friendListPanel.revalidate();
        friendListPanel.repaint();
        revalidate();
        repaint();
    }

    // 이름을 업데이트하는 메서드
    public void setUserName(String newName) {
        if (newName == null || newName.isEmpty()) return;

        nameLabel.setText(newName);  // 이름 UI 업데이트
    }

    // 프로필 이미지를 업데이트하는 메서드 (서버에서 받은 업데이트)
    public void updateProfileImage(Image newImage) {
        if (newImage == null) return;

        // 원하는 크기
        int targetWidth = profilePicLabel.getWidth();
        int targetHeight = profilePicLabel.getHeight();

        // 원본 이미지 크기
        int originalWidth = newImage.getWidth(null);
        int originalHeight = newImage.getHeight(null);

        // 크기 조정 비율 계산
        double widthRatio = (double) targetWidth / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        // 조정된 크기 계산
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // 이미지 크기 조정
        Image scaledImage = newImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // 프로필 이미지 UI 업데이트
        profilePicLabel.setIcon(new ImageIcon(scaledImage));

        revalidate();
        repaint();
    }


    // 상태 메시지를 업데이트하는 메서드
    public void updateStatusMessage(String newMessage) {
        if (newMessage == null || newMessage.isEmpty()) return;

        statusMessageLabel.setText(newMessage); // 상태 메시지 업데이트

        revalidate();
        repaint();
    }
}
