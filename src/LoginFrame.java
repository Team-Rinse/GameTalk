
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {

	private JPanel contentPane;
	private JTextField nameField;
	private JTextField ipField;
	private JTextField portField;
	private JLabel portLabel;

	public LoginFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 257, 378);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(188, 203, 220));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton loginButton = new JButton("login");
		loginButton.setFont(new Font("Kakao", Font.PLAIN, 13));
		loginButton.setBackground(new Color(251, 229, 77));
		loginButton.setBounds(62, 296, 130, 29);
		loginButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String name = nameField.getText().trim();
		        String ip = ipField.getText().trim();
		        int port;

		        try {
		            port = Integer.parseInt(portField.getText());
		        } catch (NumberFormatException ex) {
		            JOptionPane.showMessageDialog(null, "Port 번호가 유효하지 않습니다.");
		            return;
		        }

		        try {
		            Socket socket = new Socket(ip, port); // 서버 연결
		            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		            DataInputStream is = new DataInputStream(socket.getInputStream());
		            ClientSocket mySocket = new ClientSocket(name, socket, os, is);

		            // 클라이언트 이름 전송
		            os.writeUTF(name);

		            // 새로운 TalkApp 창 실행
		            setVisible(false);
		            EventQueue.invokeLater(() -> {
		                TalkApp talkApp = new TalkApp(name);
		                talkApp.setVisible(true);
		            });
		        } catch (IOException ioException) {
		            JOptionPane.showMessageDialog(null, "서버에 연결할 수 없습니다.");
		            ioException.printStackTrace();
		        }
		    }
		});

		
		nameField = new JTextField();
		nameField.setHorizontalAlignment(SwingConstants.CENTER);
		nameField.setFont(new Font("Kakao", Font.PLAIN, 13));
		nameField.setBounds(62, 115, 130, 40);
		contentPane.add(nameField);
		nameField.setColumns(10);
		
		ipField = new JTextField();
		ipField.setHorizontalAlignment(SwingConstants.CENTER);
		ipField.setText("127.0.0.1");
		ipField.setFont(new Font("Kakao", Font.PLAIN, 13));
		ipField.setColumns(10);
		ipField.setBounds(62, 179, 130, 40);
		contentPane.add(ipField);
		
		portField = new JTextField();
		portField.setHorizontalAlignment(SwingConstants.CENTER);
		portField.setText("6000");
		portField.setFont(new Font("Kakao", Font.PLAIN, 13));
		portField.setColumns(10);
		portField.setBounds(62, 243, 130, 40);
		contentPane.add(portField);
		contentPane.add(loginButton);
		
		JLabel nameLabel = new JLabel("name");
		nameLabel.setFont(new Font("Kakao", Font.PLAIN, 13));
		nameLabel.setBounds(110, 101, 34, 16);
		contentPane.add(nameLabel);
		
		JLabel ipLabel = new JLabel("IP");
		ipLabel.setFont(new Font("Kakao", Font.PLAIN, 13));
		ipLabel.setBounds(119, 163, 16, 16);
		contentPane.add(ipLabel);
		
		portLabel = new JLabel("port");
		portLabel.setFont(new Font("Kakao", Font.PLAIN, 13));
		portLabel.setBounds(113, 228, 24, 16);
		contentPane.add(portLabel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}
