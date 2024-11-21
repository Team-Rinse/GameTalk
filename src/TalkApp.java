import java.awt.EventQueue;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TalkApp extends JFrame {
	private MainPanel mainPanel;
	private ChatPanel chatPanel;
	
    public TalkApp(String userName) {
        super("Kakao Talk");
        setVisible(true);
        setSize(380, 635);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
        
        JPanel panel = new JPanel();
        panel.setBackground(new Color(237, 237, 237));
        getContentPane().add(panel);
        panel.setLayout(null);
        
        ImageIcon icon = new ImageIcon(TalkApp.class.getResource("/icon/user.png")); // 절대 경로로 리소스 불러오기
        Image userImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JButton userButton = new JButton(new ImageIcon(userImage));
        userButton.setLocation(20, 49);
        userButton.setSize(30, 30);
        userButton.setContentAreaFilled(false); // 배경 투명하게
        userButton.setBorderPainted(false);     // 테두리 없애기
        userButton.setFocusPainted(false);
        userButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(chatPanel != null) {
					panel.remove(chatPanel);
					chatPanel = null;
				} 
				mainPanel = new MainPanel(userName);
				panel.add(mainPanel);
				repaint();
				revalidate();
			}
        });
        panel.add(userButton);
        
        JButton chatButton = new JButton("");
        chatButton.setLocation(20, 103);
        ImageIcon chatIcon = new ImageIcon(TalkApp.class.getResource("/icon/chat.png"));
        Image chatImage = chatIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        chatButton.setIcon(new ImageIcon(chatImage));
        chatButton.setSize(30, 30);
        chatButton.setContentAreaFilled(false);
        chatButton.setBorderPainted(false);
        chatButton.setFocusPainted(false);
        chatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mainPanel != null) {
					panel.remove(mainPanel);
					mainPanel = null;
				}
				chatPanel = new ChatPanel();
				panel.add(chatPanel);
				repaint();
				revalidate();
			}
        });
        panel.add(chatButton);
        
        JButton optionButton = new JButton("");
        optionButton.setSize(30, 30);
        optionButton.setLocation(20, 145);
        optionButton.setContentAreaFilled(false);
        optionButton.setBorderPainted(false);
        optionButton.setFocusPainted(false);
        panel.add(optionButton);
        
        mainPanel = new MainPanel(userName);
        panel.add(mainPanel);
    }

    public void run() {
        // run 메서드 구현
    }

}
