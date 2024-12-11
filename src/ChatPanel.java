import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ChatPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public ChatPanel() {
        setBackground(Color.WHITE);
        setBounds(73, 0, 307, 613);
        setLayout(null);

        JLabel lblNewLabel = new JLabel("채팅");
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
        add(myProfileButton);

        JLabel friendName1 = new JLabel("하여린");
        friendName1.setFont(new Font("Kakao", Font.BOLD, 13));
        friendName1.setBounds(80, 52, 34, 16);
        add(friendName1);

        JLabel lblNewLabel_1 = new JLabel("네프 과제 다했어??");
        lblNewLabel_1.setFont(new Font("Kakao", Font.PLAIN, 12));
        lblNewLabel_1.setForeground(new Color(116, 116, 116));
        lblNewLabel_1.setBounds(80, 72, 221, 16);
        add(lblNewLabel_1);
    }
}
