import java.awt.Dimension;
import java.awt.Image;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Font;

public class MarketFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private int point;
    private JLabel pointLabel;
    private OptionPanel optionPanel; // OptionPanel 참조

    public MarketFrame(int point, OptionPanel optionPanel) {
        this.point = point; // 초기 포인트
        this.optionPanel = optionPanel; // OptionPanel 참조
        setBounds(100, 100, 380, 450);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 255, 255));
        contentPane.setLayout(null); 
        contentPane.setPreferredSize(new Dimension(380, 800)); 
        drawProducts();

        JScrollPane scrollPane = new JScrollPane(contentPane);
        
        JLabel titleLabel = new JLabel("이모티콘 샵");
        titleLabel.setFont(new Font("Kakao", Font.PLAIN, 17));
        titleLabel.setBounds(18, 6, 82, 26);
        contentPane.add(titleLabel);
        
        pointLabel = new JLabel("보유 포인트: " + point + "pt");
        pointLabel.setFont(new Font("Kakao", Font.PLAIN, 15));
        pointLabel.setBounds(204, 8, 159, 26);
        contentPane.add(pointLabel);
        
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // 세로 스크롤 활성화
        setContentPane(scrollPane);

        setVisible(true); // 창 표시
    }

    private void drawProducts() {
        int xBase = 20; // 시작 x 좌표
        int yBase = 50; // 시작 y 좌표
        int xGap = 170; // 이모지 간 가로 간격
        int yGap = 120; // 이모지 간 세로 간격

        for (int i = 0; i < 12; i++) {
            int row = i / 2; // 행 계산
            int col = i % 2; // 열 계산
            int price = 100 + i * 500;

            JButton productButton = new JButton();
            productButton.setName("emo" + (i + 1));
            ImageIcon icon = new ImageIcon(MarketFrame.class.getResource("/emoji/emo" + (i + 1) + ".png"));
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            productButton.setIcon(new ImageIcon(img));
            productButton.setBounds(xBase + col * xGap, yBase + row * yGap, 100, 100);
            productButton.setContentAreaFilled(false); 
            productButton.setBorderPainted(false);
            productButton.setFocusPainted(false);
            productButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (point >= price) {
                        point -= price;
                        JOptionPane.showMessageDialog(null, "구매 완료!");
                        updatePointLabel();
                        optionPanel.updatePoint(point);
                        optionPanel.addPurchasedEmoji(productButton.getName(), 1);
                    } else {
                        JOptionPane.showMessageDialog(null, "포인트가 부족합니다.");
                    }
                }
            });
            contentPane.add(productButton);

            JLabel priceLabel = new JLabel(price + "pt");
            priceLabel.setBounds(xBase + col * xGap + 110, yBase + row * yGap + 40, 50, 20); 
            contentPane.add(priceLabel);
        }
    }

    private void updatePointLabel() {
        pointLabel.setText("보유 포인트: " + point + "pt");
    }
}
