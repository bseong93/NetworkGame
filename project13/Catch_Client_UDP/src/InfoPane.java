import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;

public class InfoPane extends JFrame {
	private Container contentpane;
	private JTextField tf_ID;
	private JButton start;
	private JLabel error;

	private String id;
	private String ip = "127.0.0.1";
	private int port = 30000;

	InfoPane() {
		setBounds(500, 200, 300, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentpane = getContentPane();
		contentpane.setBackground(new Color(80, 180, 210));
		contentpane.setLayout(null);

		JLabel label = new JLabel("사용할 닉네임을 입력해 주세요");
		label.setFont(new Font("Serif", Font.PLAIN, 17));
		label.setBounds(10, 5, 310, 30);
		contentpane.add(label);

		label = new JLabel("ID");
		label.setBounds(50, 70, 90, 30);
		contentpane.add(label);
		
		error = new JLabel("");
		error.setForeground(new Color(255, 0, 0));
		error.setBounds(50, 100, 300, 30);		
		contentpane.add(error);

		tf_ID = new JTextField();
		tf_ID.setBounds(70, 70, 150, 30);
		tf_ID.setColumns(10);
		tf_ID.addActionListener(new ButtonListener());
		contentpane.add(tf_ID);

		start = new JButton("접속");
		start.setBounds(50, 300, 200, 30);
		start.addActionListener(new ButtonListener());
		contentpane.add(start);

		setVisible(true);
	}

	//////////////////////// 버튼 리스너 ////////////////////////////
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {						
			id = tf_ID.getText().trim();
			if (id.length() > 8)
				error.setText("8글자 이내로 입력해 주세요!");
			else {
				setVisible(false);

				try {
					MainPane mainpane = new MainPane(ip, port, id);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}