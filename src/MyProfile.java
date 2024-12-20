import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MyProfile extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField nameField;
    private JTextField statusMessageField; // 상태 메시지 필드 추가
    private JLabel profilePic;
    private MainPanel mainPanel; // MainPanel 객체를 저장할 변수 추가


    public MyProfile(MainPanel mainPanel) {
        this.mainPanel = mainPanel; // MainPanel 객체를 받아옴

        setBounds(100, 100, 230, 500); // 창 높이를 늘림 (상태 메시지 추가)
        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 255, 255));
        contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));

        setContentPane(contentPane);
        contentPane.setLayout(null);

        profilePic = new JLabel();
        ImageIcon profileIcon = (ImageIcon) mainPanel.profilePicLabel.getIcon(); // MainPanel의 profilePicLabel에서 아이콘 가져오기

        if (profileIcon != null) {
            // 이미지가 있으면 해당 이미지를 설정
            Image profileImage = profileIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            profilePic.setIcon(new ImageIcon(profileImage));
        } else {
            // 이미지가 없으면 기본 이미지로 설정하지 않음 (빈 아이콘 설정)
            profilePic.setIcon(null);
        }
        profilePic.setBounds(70, 150, 90, 90);
        contentPane.add(profilePic);

        JLabel editProfilePicBtn = new JLabel();
        ImageIcon pencilIcon = new ImageIcon(TalkApp.class.getResource("/icon/pencil.png"));
        Image pencilImage = pencilIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        editProfilePicBtn.setIcon(new ImageIcon(pencilImage));
        editProfilePicBtn.setBounds(165, 178, 16, 16);
        editProfilePicBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseProfileImage();
            }
        });
        contentPane.add(editProfilePicBtn);

        nameField = new JTextField(mainPanel.nameLabel.getText());
        nameField.setBounds(70, 260, 90, 20);
        nameField.setBorder(null);
        contentPane.add(nameField);

        JLabel editNameBtn = new JLabel();
        editNameBtn.setIcon(new ImageIcon(pencilImage));
        editNameBtn.setBounds(165, 260, 16, 16);
        editNameBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nameField.setEditable(!nameField.isEditable());
                if (!nameField.isEditable()) {
                    nameField.setBackground(Color.WHITE);
                    String newName = nameField.getText();
                    mainPanel.setUserName(newName); // 이름 변경 후 MainPanel에 반영

                    // 서버로 업데이트 요청
                    ClientSocket.updateProfile(newName, getCurrentProfileImageBytes(), statusMessageField.getText());

                } else {
                    nameField.setBackground(new Color(240, 240, 240));
                    nameField.requestFocus();
                }
            }
        });
        contentPane.add(editNameBtn);

        statusMessageField = new JTextField(); // 저장된 상태 메시지 불러오기
        statusMessageField.setBounds(70, 300, 90, 20);
        statusMessageField.setBorder(null);
        contentPane.add(statusMessageField);

        JLabel editStatusMessageBtn = new JLabel();
        editStatusMessageBtn.setIcon(new ImageIcon(pencilImage));
        editStatusMessageBtn.setBounds(165, 300, 16, 16);
        editStatusMessageBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                statusMessageField.setEditable(!statusMessageField.isEditable());
                if (!statusMessageField.isEditable()) {
                    statusMessageField.setBackground(Color.WHITE);
                    String newStatusMessage = statusMessageField.getText();

                    // 서버로 업데이트 요청
                    ClientSocket.updateProfile(nameField.getText(), getCurrentProfileImageBytes(), newStatusMessage);

                    mainPanel.updateStatusMessage(newStatusMessage); // MainPanel에 반영
                } else {
                    statusMessageField.setBackground(new Color(240, 240, 240));
                    statusMessageField.requestFocus();
                }
            }
        });

        contentPane.add(editStatusMessageBtn);

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

            try {
                Image newProfileImage = ImageIO.read(selectedFile).getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                profilePic.setIcon(new ImageIcon(newProfileImage));

                // 이미지 파일을 바이트 배열로 변환
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(ImageIO.read(selectedFile), "png", baos);
                byte[] imageBytes = baos.toByteArray();

                // 서버로 프로필 업데이트 전송
                ClientSocket.updateProfile(nameField.getText(), imageBytes, statusMessageField.getText());

                mainPanel.updateProfileImage(newProfileImage); // MainPanel에 반영

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "이미지 로드 실패", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private byte[] getCurrentProfileImageBytes() {
        try {
            ImageIcon icon = (ImageIcon) profilePic.getIcon();
            if (icon == null) {
                return null; // 아이콘이 없는 경우 null 반환
            }

            Image img = icon.getImage();

            // 이미지를 BufferedImage로 변환
            BufferedImage bufferedImage = new BufferedImage(
                    img.getWidth(null),
                    img.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();

            // BufferedImage를 바이트 배열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null; // 변환 실패 시 null 반환
        }
    }
}
