import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public MainPanel() {
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
        myProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				MyProfile mp = new MyProfile();
			}
        });
        add(myProfileButton);
        
        JLabel lblNewLabel_1 = new JLabel("전아린");
        lblNewLabel_1.setFont(new Font("Kakao", lblNewLabel_1.getFont().getStyle(), lblNewLabel_1.getFont().getSize()));
        lblNewLabel_1.setBounds(80, 62, 34, 16);
        add(lblNewLabel_1);
        
        JLabel lblNewLabel_2 = new JLabel("");
        lblNewLabel_2.setBackground(new Color(240, 240, 240));
        lblNewLabel_2.setBounds(18, 108, 268, 2);
        lblNewLabel_2.setOpaque(true); // 배경색이 표시되도록 설정
        add(lblNewLabel_2);
        
        JLabel lblNewLabel_3 = new JLabel("친구");
        lblNewLabel_3.setFont(new Font("Kakao", lblNewLabel_3.getFont().getStyle(), lblNewLabel_3.getFont().getSize()));
        lblNewLabel_3.setForeground(new Color(117, 117, 117));
        lblNewLabel_3.setBounds(18, 112, 34, 35);
        add(lblNewLabel_3);
        
        JButton btnNewButton = new JButton("");
        ImageIcon addUserIcon = new ImageIcon(TalkApp.class.getResource("/icon/user-plus.png"));
        Image addUserImg = addUserIcon.getImage().getScaledInstance(25, 20, Image.SCALE_SMOOTH);
        btnNewButton.setBackground(null);
        btnNewButton.setIcon(new ImageIcon(addUserImg));
        btnNewButton.setContentAreaFilled(false); // 배경 투명하게
        btnNewButton.setBorderPainted(false);   
        btnNewButton.setBounds(271, 6, 25, 20);
        btnNewButton.setFocusPainted(false);
        add(btnNewButton);
	}

}
