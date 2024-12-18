import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class TalkApp extends JFrame {
    private MainPanel mainPanel;
    private ChatPanel chatPanel = new ChatPanel();
    private OptionPanel optionPanel = new OptionPanel();

    public TalkApp(String userName) {
        super("Kakao Talk");
        setVisible(true);
        setSize(380, 635);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        // 왼쪽 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(0, 0, 60, 635);
        buttonPanel.setBackground(new Color(237, 237, 237));
        buttonPanel.setLayout(null);
        getContentPane().add(buttonPanel);

        // 오른쪽 콘텐츠 패널 (CardLayout 사용)
        JPanel contentPanel = new JPanel();
        contentPanel.setBounds(60, 0, 320, 635);
        CardLayout cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        getContentPane().add(contentPanel);

        // CardLayout에 각 화면 추가
        mainPanel = new MainPanel(userName);
        contentPanel.add(mainPanel, "Main");
        contentPanel.add(chatPanel, "Chat");
        contentPanel.add(optionPanel, "Option");

        // 버튼 생성 
        ImageIcon userIcon = new ImageIcon(TalkApp.class.getResource("/icon/user.png"));
        Image userImage = userIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JButton userButton = createButton(new ImageIcon(userImage), 15, 49);
        userButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(contentPanel, "Main");
			}
        });
        buttonPanel.add(userButton);

        ImageIcon chatIcon = new ImageIcon(TalkApp.class.getResource("/icon/chat.png"));
        Image chatImage = chatIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JButton chatButton = createButton(new ImageIcon(chatImage), 15, 103);
        chatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(contentPanel, "Chat");
			}
        });
        buttonPanel.add(chatButton);

        ImageIcon optionIcon = new ImageIcon(TalkApp.class.getResource("/icon/option.png"));
        Image optionImage = optionIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JButton optionButton = createButton(new ImageIcon(optionImage), 15, 157);
        optionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(contentPanel, "Option");
			}
        });
        buttonPanel.add(optionButton);

        ClientSocket.setChatPanel(chatPanel);
        ClientSocket.setOptionPanel(optionPanel);
    }

    private JButton createButton(ImageIcon icon, int x, int y) {
        JButton button = new JButton(icon);
        button.setBounds(x, y, 30, 30);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }
}
