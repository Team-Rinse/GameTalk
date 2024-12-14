import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class OptionPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public OptionPanel() {
		setBackground(Color.WHITE);
        setBounds(73, 0, 307, 613);
        setLayout(null);

        JLabel optionLabel = new JLabel("더보기");
        optionLabel.setBounds(18, 6, 50, 35);
        optionLabel.setFont(new Font("Kakao", Font.PLAIN, 17));
        add(optionLabel);

        JLabel coinLabel = new JLabel("100pt");
        coinLabel.setBackground(new Color(245, 245, 245));
        coinLabel.setBounds(18, 63, 271, 50);
        coinLabel.setOpaque(true);
        add(coinLabel);
	}

}
