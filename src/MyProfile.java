import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.prefs.Preferences;

public class MyProfile extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField nameField;
    private JLabel profilePic;
    private MainPanel mainPanel;  // MainPanel 객체를 저장할 변수 추가

    private Preferences prefs;

    public MyProfile(MainPanel mainPanel) {
        this.mainPanel = mainPanel;  // MainPanel 객체를 받아옴
        prefs = Preferences.userNodeForPackage(MyProfile.class);

        setBounds(100, 100, 230, 440);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 255, 255));
        contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));

        setContentPane(contentPane);
        contentPane.setLayout(null);

        profilePic = new JLabel();
        String savedImagePath = prefs.get("profileImagePath", null);

        if (savedImagePath != null) {
            // 이미지가 있으면 해당 이미지를 설정
            ImageIcon profileIcon = new ImageIcon(savedImagePath);
            Image profileImage = profileIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            profilePic.setIcon(new ImageIcon(profileImage));
        } else {
            // 이미지가 없으면 기본 이미지로 설정하지 않음 (빈 아이콘 설정)
            profilePic.setIcon(null);
        }
        profilePic.setBounds(70, 207, 90, 90);
        contentPane.add(profilePic);

        JLabel editProfilePicBtn = new JLabel();
        ImageIcon pencilIcon = new ImageIcon(TalkApp.class.getResource("/icon/pencil.png"));
        Image pencilImage = pencilIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        editProfilePicBtn.setIcon(new ImageIcon(pencilImage));
        editProfilePicBtn.setBounds(165, 235, 16, 16);
        editProfilePicBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseProfileImage();
            }
        });
        contentPane.add(editProfilePicBtn);

        nameField = new JTextField(prefs.get("userName", "전아린"));  // Preferences에서 이름을 가져옴
        nameField.setBounds(70, 297, 90, 20);
        nameField.setBorder(null);
        contentPane.add(nameField);

        JLabel editNameBtn = new JLabel();
        editNameBtn.setIcon(new ImageIcon(pencilImage));
        editNameBtn.setBounds(165, 297, 16, 16);
        editNameBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nameField.setEditable(!nameField.isEditable());
                if (!nameField.isEditable()) {
                    nameField.setBackground(Color.WHITE);
                    mainPanel.updateName(nameField.getText());  // 이름 변경 후 MainPanel에 반영
                    dispose();  // 창 닫기
                } else {
                    nameField.setBackground(new Color(240, 240, 240));
                    nameField.requestFocus();
                }
            }
        });
        contentPane.add(editNameBtn);

        JLabel separator = new JLabel("");
        separator.setBackground(new Color(240, 240, 240));
        separator.setBounds(0, 339, 230, 1);
        separator.setOpaque(true);
        contentPane.add(separator);

        setVisible(true);
    }

    private void chooseProfileImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            ImageIcon newProfileIcon = new ImageIcon(selectedFile.getPath());
            Image newProfileImage = newProfileIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            profilePic.setIcon(new ImageIcon(newProfileImage));

            prefs.put("profileImagePath", selectedFile.getPath());  // 선택한 이미지 경로를 Preferences에 저장
            mainPanel.updateProfileImage(newProfileImage);  // MainPanel에 반영
        }
    }
}
