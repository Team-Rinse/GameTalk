import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class OptionPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private int point = 100900;
    private JLabel coinLabel;
    HashMap<String, Integer> purchasedEmojis;

    public OptionPanel() {
        setBackground(Color.WHITE);
        setBounds(73, 0, 307, 613);
        setLayout(null);

        purchasedEmojis = new HashMap<>();

        JLabel optionLabel = new JLabel("더보기");
        optionLabel.setBounds(18, 6, 50, 35);
        optionLabel.setFont(new Font("Kakao", Font.PLAIN, 17));
        add(optionLabel);

        coinLabel = new JLabel(point + " pt");
        coinLabel.setBackground(new Color(245, 245, 245));
        coinLabel.setBounds(18, 63, 271, 50);
        coinLabel.setOpaque(true);
        add(coinLabel);

        JButton emojiButton = new JButton("");
        ImageIcon emojiIcon = new ImageIcon(OptionPanel.class.getResource("/icon/emoji.png"));
        Image emojiImg = emojiIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        emojiButton.setIcon(new ImageIcon(emojiImg));
        emojiButton.setBounds(65, 150, 50, 50);
        emojiButton.addActionListener(e -> {
            MarketFrame marketFrame = new MarketFrame(point, OptionPanel.this);
            marketFrame.setVisible(true);
        });
        add(emojiButton);

        JButton storageButton = new JButton("");
        ImageIcon storageIcon = new ImageIcon(OptionPanel.class.getResource("/icon/storage.png"));
        Image storageImg = storageIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        storageButton.setIcon(new ImageIcon(storageImg));
        storageButton.setBounds(176, 150, 50, 50);
        storageButton.addActionListener(e -> {
            StorageDialog storageDialog = new StorageDialog();
            storageDialog.setVisible(true);
        });
        add(storageButton);

        JLabel emojiLabel = new JLabel("이모티콘");
        emojiLabel.setFont(new Font("Kakao", Font.PLAIN, 11));
        emojiLabel.setBounds(70, 201, 36, 16);
        add(emojiLabel);

        JLabel emojiLabel_1 = new JLabel("보관함");
        emojiLabel_1.setFont(new Font("Kakao", Font.PLAIN, 11));
        emojiLabel_1.setBounds(187, 201, 36, 16);
        add(emojiLabel_1);
    }

    public void updatePoint(int newPoint) {
        this.point = newPoint;
        coinLabel.setText(point + " pt");
    }

    public void addPurchasedEmoji(String emojiName, int delta) {
        int currentCount = purchasedEmojis.getOrDefault(emojiName, 0);
        purchasedEmojis.put(emojiName, Math.max(currentCount + delta, 0)); // 음수 방지
    }

    class StorageDialog extends JDialog {
        public StorageDialog() {
            setModal(true);
            setSize(300, 380);
            setBackground(Color.WHITE);
            setResizable(false);
            setUndecorated(true);

            int parentX = OptionPanel.this.getLocationOnScreen().x;
            int parentY = OptionPanel.this.getLocationOnScreen().y;
            int parentWidth = OptionPanel.this.getWidth();
            int parentHeight = OptionPanel.this.getHeight();

            int dialogWidth = getWidth();
            int dialogHeight = getHeight();

            int dialogX = parentX + (parentWidth - dialogWidth) / 2;
            int dialogY = parentY + (parentHeight - dialogHeight) / 2;
            setLocation(dialogX, dialogY);

            getContentPane().setBackground(new Color(245, 245, 245));
            setLayout(null);

            JLabel label = new JLabel("이모티콘 보관함");
            label.setFont(new Font("Kakao", Font.PLAIN, 15));
            label.setBounds(100, 10, 200, 30);
            add(label);

            JPanel scrollPanel = new JPanel();
            scrollPanel.setLayout(null);
            scrollPanel.setBackground(new Color(245, 245, 245));

            int yPosition = 10;

            for (Map.Entry<String, Integer> entry : purchasedEmojis.entrySet()) {
                String emoji = entry.getKey();
                int count = entry.getValue();

                JLabel emojiLabel = new JLabel();
                ImageIcon icon = new ImageIcon(OptionPanel.class.getResource("/emoji/" + emoji + ".png"));
                Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                emojiLabel.setIcon(new ImageIcon(img));
                emojiLabel.setBounds(20, yPosition, 70, 70);
                scrollPanel.add(emojiLabel);

                JLabel countLabel = new JLabel("x" + count);
                countLabel.setFont(new Font("Kakao", Font.PLAIN, 15));
                countLabel.setBounds(100, yPosition + 25, 100, 20);
                scrollPanel.add(countLabel);

                yPosition += 90;
            }

            scrollPanel.setPreferredSize(new java.awt.Dimension(250, yPosition));

            JScrollPane scrollPane = new JScrollPane(scrollPanel);
            scrollPane.setBounds(10, 50, 280, 260);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null); // 테두리 제거
            add(scrollPane);

            JButton closeButton = new JButton("닫기");
            closeButton.setFont(new Font("Kakao", Font.PLAIN, 12));
            closeButton.setBounds(100, 330, 100, 30);
            closeButton.addActionListener(e -> dispose());
            add(closeButton);
        }
    }
}
