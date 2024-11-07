import java.awt.EventQueue;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TalkApp extends JFrame {
    public TalkApp() {
        super("Kakao Talk");
        setVisible(true);
        setSize(380, 635);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
        
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);
        
        ImageIcon icon = new ImageIcon(TalkApp.class.getResource("/icon/person.png")); // 절대 경로로 리소스 불러오기
        Image userImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JButton userButton = new JButton(new ImageIcon(userImage));
        userButton.setLocation(20, 49);
        userButton.setSize(30, 30);
        userButton.setContentAreaFilled(false); // 배경 투명하게
        userButton.setBorderPainted(false);     // 테두리 없애기
        userButton.setFocusPainted(false);
        panel.add(userButton);
        
        JButton chatButton = new JButton("New button");
        chatButton.setLocation(20, 97);
        chatButton.setSize(30, 30);
        chatButton.setContentAreaFilled(false);
        chatButton.setBorderPainted(false);
        chatButton.setFocusPainted(false);
        panel.add(chatButton);
        
        JButton optionButton = new JButton("New button");
        optionButton.setSize(30, 30);
        optionButton.setLocation(20, 145);
        optionButton.setContentAreaFilled(false);
        optionButton.setBorderPainted(false);
        optionButton.setFocusPainted(false);
        panel.add(optionButton);
        
        JPanel panel_1 = new JPanel();
        panel_1.setBackground(Color.WHITE);
        panel_1.setBounds(73, 0, 307, 613);
        panel.add(panel_1);
        panel_1.setLayout(null);
        
        JLabel lblNewLabel = new JLabel("친구");
        lblNewLabel.setFont(new Font("/font/KakaoRegular.ttf", Font.PLAIN, 16));
        lblNewLabel.setBounds(18, 6, 34, 35);
        panel_1.add(lblNewLabel);
        
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
        panel_1.add(myProfileButton);
        
        JLabel lblNewLabel_1 = new JLabel("전아린");
        lblNewLabel_1.setBounds(80, 62, 34, 16);
        panel_1.add(lblNewLabel_1);
        
        JLabel lblNewLabel_2 = new JLabel("");
        lblNewLabel_2.setBackground(new Color(240, 240, 240));
        lblNewLabel_2.setBounds(18, 108, 268, 2);
        lblNewLabel_2.setOpaque(true); // 배경색이 표시되도록 설정
        panel_1.add(lblNewLabel_2);
        
        JLabel lblNewLabel_3 = new JLabel("친구");
        lblNewLabel_3.setFont(new Font("Dialog", Font.PLAIN, 14));
        lblNewLabel_3.setForeground(new Color(117, 117, 117));
        lblNewLabel_3.setBounds(18, 112, 34, 35);
        panel_1.add(lblNewLabel_3);
    }

    public void run() {
        // run 메서드 구현
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new TalkApp();
        });
    }
}
