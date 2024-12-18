import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private MyProfile myProfile;

    public JLabel nameLabel;          // 이름을 표시할 JLabel
    public JLabel profilePicLabel;   // 프로필 이미지를 표시할 JLabel
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

//        Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
//        String savedProfileImagePath = prefs.get("profileImagePath", null);
//        String savedStatusMessage = prefs.get("statusMessage", "");

        // 초기 프로필 이미지 설정
        ImageIcon profileIcon;
//        if (savedProfileImagePath != null && !savedProfileImagePath.isEmpty()) {
//            profileIcon = new ImageIcon(savedProfileImagePath);
//        } else {
        profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
//        }
        Image profileImage = profileIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);

        JButton myProfileButton = new JButton();
        myProfileButton.setIcon(new ImageIcon(profileImage));
        myProfileButton.setBounds(14, 40, 60, 60);
        myProfileButton.setContentAreaFilled(false);
        myProfileButton.setBorderPainted(false);
        myProfileButton.setFocusPainted(false);
        myProfileButton.addActionListener(e -> {
            // MyProfile을 호출할 때 MainPanel을 전달
            new MyProfile(MainPanel.this); // MainPanel 객체 전달
        });
        add(myProfileButton);

        profilePicLabel = new JLabel(new ImageIcon(profileImage));
        profilePicLabel.setBounds(14, 40, 60, 60);
        add(profilePicLabel);

        // 이름을 표시할 JLabel을 생성합니다.
        nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Kakao", Font.BOLD, 14));
        nameLabel.setBounds(80, 40, 150, 16); // 이름 위치
        add(nameLabel);

        // 상태 메시지를 표시할 JLabel을 생성합니다.
//        if (savedStatusMessage.isEmpty()) {
        statusMessageLabel = new JLabel("상태 메시지를 입력하세요");
        statusMessageLabel.setFont(new Font("Kakao", Font.ITALIC, 12));
        statusMessageLabel.setForeground(Color.GRAY);
//        } else {
//            statusMessageLabel = new JLabel(savedStatusMessage);
//            statusMessageLabel.setFont(new Font("Kakao", Font.PLAIN, 12));
//            statusMessageLabel.setForeground(Color.BLACK); // 기존 메시지 색상 설정
//        }
        statusMessageLabel.setBounds(80, 60, 200, 16); // 상태 메시지 위치
        add(statusMessageLabel);

        profilePicLabel = new JLabel();
        profilePicLabel.setBounds(14, 40, 60, 60);
        add(profilePicLabel);

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
        createChatButton.setVisible(false);
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

            // 아이콘 버튼(친구 프로필)
            JButton friendButton = new JButton();
            ImageIcon friendIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
            Image friendImg = friendIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            friendButton.setIcon(new ImageIcon(friendImg));
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
        nameLabel.setText(newName);  // MainPanel에서 JLabel을 통해 이름을 업데이트합니다.
    }

    // 프로필 이미지를 업데이트하는 메서드
    public void updateProfileImage(Image newImage) {
        profilePicLabel.setIcon(new ImageIcon(newImage)); // MainPanel에서 JLabel을 통해 프로필 이미지를 업데이트합니다.

        // 이미지를 저장
//        Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
        try {
            File tempFile = new File("profile_temp.png");
            ImageIcon icon = new ImageIcon(newImage);
            ImageIO.write((BufferedImage) icon.getImage(), "png", tempFile);
//            prefs.put("profileImagePath", tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        revalidate();
        repaint();
    }


    // 상태 메시지를 업데이트하는 메서드
    public void updateStatusMessage(String newMessage) {
        statusMessageLabel.setText(newMessage); // 상태 메시지 업데이트

//        Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
//        prefs.put("statusMessage", newMessage); // 상태 메시지 저장

        revalidate();
        repaint();
    }
}