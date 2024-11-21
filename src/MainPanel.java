import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.prefs.Preferences;

public class MainPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public JLabel nameLabel;  // 이름을 표시할 JLabel
    public JLabel profilePicLabel;  // 프로필 이미지를 표시할 JLabel
    private Preferences prefs;  // Preferences 객체 추가

    public MainPanel(String userName) {
        prefs = Preferences.userNodeForPackage(MainPanel.class);  // Preferences 초기화
        setBackground(Color.WHITE);
        setBounds(73, 0, 307, 613);
        setLayout(null);

        JLabel lblNewLabel = new JLabel("친구");
        lblNewLabel.setBounds(18, 6, 34, 35);
        lblNewLabel.setFont(new Font("Kakao", Font.PLAIN, 17));
        add(lblNewLabel);

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
        add(myProfileButton);

        // 이름을 표시할 JLabel을 생성합니다.
        nameLabel = new JLabel(userName);  // 이름을 Preferences에서 가져옴
        nameLabel.setFont(new Font("Kakao", nameLabel.getFont().getStyle(), nameLabel.getFont().getSize()));
        nameLabel.setBounds(80, 62, 100, 16);
        add(nameLabel);

        // 프로필 이미지를 표시할 JLabel을 추가합니다.
        profilePicLabel = new JLabel();
        String savedImagePath = prefs.get("profileImagePath", null);
        if (savedImagePath != null) {
            // Preferences에 저장된 이미지가 있으면 해당 이미지를 설정
            ImageIcon savedImageIcon = new ImageIcon(savedImagePath);
            Image savedImage = savedImageIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            profilePicLabel.setIcon(new ImageIcon(savedImage));
        } else {
            // 이미지가 없으면 빈 아이콘을 설정 (기본 이미지 표시하지 않음)
            profilePicLabel.setIcon(null);
        }
        profilePicLabel.setBounds(14, 40, 60, 60);
        add(profilePicLabel);

        JLabel lblNewLabel_2 = new JLabel("");
        lblNewLabel_2.setBackground(new Color(240, 240, 240));
        lblNewLabel_2.setBounds(18, 108, 268, 2);
        lblNewLabel_2.setOpaque(true);
        add(lblNewLabel_2);
    }

    // 이름을 업데이트하는 메서드를 추가합니다.
    public void setUserName(String newName) {
        nameLabel.setText(newName);  // MainPanel에서 JLabel을 통해 이름을 업데이트합니다.
        prefs.put("userName", newName);  // 변경된 이름을 Preferences에 저장
    }

    // 프로필 이미지를 업데이트하는 메서드를 추가합니다.
    public void updateProfileImage(Image newImage) {
        profilePicLabel.setIcon(new ImageIcon(newImage));  // MainPanel에서 JLabel을 통해 프로필 이미지를 업데이트합니다.
    }
}