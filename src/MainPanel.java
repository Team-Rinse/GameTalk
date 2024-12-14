import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import server.Server;

public class MainPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public JLabel nameLabel;  // 이름을 표시할 JLabel
    public JLabel profilePicLabel;  // 프로필 이미지를 표시할 JLabel

    private JPanel friendListPanel; // 친구 리스트를 보여줄 패널
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

        JButton myProfileButton = new JButton();
        ImageIcon profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
        Image profileImage = profileIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        myProfileButton.setIcon(new ImageIcon(profileImage));
        myProfileButton.setBounds(14, 40, 60, 60);
        myProfileButton.setContentAreaFilled(false);
        myProfileButton.setBorderPainted(false);
        myProfileButton.setFocusPainted(false);
        myProfileButton.addActionListener(e -> {
            // MyProfile을 호출할 때 MainPanel을 전달
            MyProfile mp = new MyProfile(MainPanel.this);  // MainPanel 객체 전달
        });

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
        startChatButton.setText("Cancel");
        startChatButton.setText("Start Chat");
        add(startChatButton);
        startChatButton.setText("Start Chat");
        add(myProfileButton);

        // 이름을 표시할 JLabel을 생성합니다.
        nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Kakao", nameLabel.getFont().getStyle(), nameLabel.getFont().getSize()));
        nameLabel.setBounds(80, 62, 100, 16);
        add(nameLabel);

        JLabel friendLabel = new JLabel("친구");
        friendLabel.setBounds(18, 6, 34, 35);
        friendLabel.setFont(new Font("Kakao", Font.PLAIN, 17));
        add(friendLabel);

        profilePicLabel = new JLabel();
        profilePicLabel.setBounds(14, 40, 60, 60);
        add(profilePicLabel);

        JLabel line = new JLabel("");
        line.setBackground(new Color(240, 240, 240));
        line.setBounds(18, 108, 268, 2);
        line.setOpaque(true);
        add(line);

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

    // 이름을 업데이트하는 메서드를 추가합니다.
    public void setUserName(String newName) {
        nameLabel.setText(newName);  // MainPanel에서 JLabel을 통해 이름을 업데이트합니다.
    }

    // 프로필 이미지를 업데이트하는 메서드를 추가합니다.
    public void updateProfileImage(Image newImage) {
        profilePicLabel.setIcon(new ImageIcon(newImage));  // MainPanel에서 JLabel을 통해 프로필 이미지를 업데이트합니다.
    }
}
