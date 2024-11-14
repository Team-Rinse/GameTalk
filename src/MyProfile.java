import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;

public class MyProfile extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MyProfile() {
		setBounds(100, 100, 230, 440);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(255, 255, 255));
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel profilePic = new JLabel();
		ImageIcon profileIcon = new ImageIcon(TalkApp.class.getResource("/icon/profile.png"));
        Image profileImage = profileIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
		profilePic.setIcon(new ImageIcon(profileImage));
		
		profilePic.setBounds(70, 207, 90, 90);
		contentPane.add(profilePic);
		
		JLabel name = new JLabel("전아린");
		name.setBounds(98, 297, 39, 16);
		contentPane.add(name);
		
		JLabel lblNewLabel_2 = new JLabel("");
        lblNewLabel_2.setBackground(new Color(240, 240, 240));
        lblNewLabel_2.setBounds(0, 339, 230, 1);
        lblNewLabel_2.setOpaque(true); // 배경색이 표시되도록 설정
        contentPane.add(lblNewLabel_2);
        
        setVisible(true);
	}

}
