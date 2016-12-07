import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.print.attribute.AttributeSet;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import java.awt.*;

public class ChatPane extends JPanel {
	private JTextPane textpane;
	private JTextField textfield;
	private JButton sendButton, readyButton;

	private String id;

	ChatPane(String _id) {
		this.id = _id;

		setLayout(null);
		// setSize(1050, 750);
		setBackground(new Color(140, 170, 230));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(3, 3, 330, 450);
		add(scrollPane);

		textpane = new JTextPane();
		textpane.setEnabled(false);
		scrollPane.setViewportView(textpane);
		textpane.setDisabledTextColor(new Color(0, 0, 0));

		textfield = new JTextField();
		textfield.setBounds(3, 455, 250, 30);
		textfield.setColumns(10);
		textfield.addActionListener(new sendbuttonlistener());
		add(textfield);

		sendButton = new JButton("전송");
		sendButton.setBounds(255, 455, 75, 30);
		sendButton.addActionListener(new sendbuttonlistener());
		add(sendButton);

		readyButton = new JButton("준비");
		readyButton.setBackground(new Color(249, 191, 0));
		readyButton.setBounds(3, 490, 330, 30);
		readyButton.addActionListener(new readybuttonlistener());
		add(readyButton);
	}

	//////////////////////// 채팅 내용 보여주기 ////////////////////////////
	public void UpdateMessage(String str) {
		int len = textpane.getDocument().getLength();
		textpane.setCaretPosition(len);

		SimpleAttributeSet att = new SimpleAttributeSet();
		//StyleConstants.setFontFamily(att, "Courier New Italic");
		StyleConstants.setFontSize(att, 15);
		
		String tmp = str.substring(1, id.length() + 1);		
		

		if (tmp.equals(id)) {
			//StyleConstants.setAlignment(att, StyleConstants.ALIGN_RIGHT);
			textpane.setParagraphAttributes(att, true);
			StyleConstants.setBackground(att, new Color(146, 165, 231));			
		} else {
			//StyleConstants.setAlignment(att, StyleConstants.ALIGN_LEFT);
			textpane.setParagraphAttributes(att, true);
			StyleConstants.setBackground(att, Color.white);			
		}

		try {				
			textpane.getDocument().insertString(len, str, att);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	//////////////////////// 버튼 리스너 ////////////////////////////
	class sendbuttonlistener implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == sendButton || e.getSource() == textfield) {
				String msg = null;
				msg = String.format("[%s] :  %s\n", id, textfield.getText());

				Send_Message(msg);

				textfield.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				textfield.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다

			}
		}
	}

	class readybuttonlistener implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readyButton.getText() == "준비") {
				readyButton.setText("준비취소");
				readyButton.setBackground(new Color(13, 197, 251));
				Send_Message("ready");
			} else {
				readyButton.setText("준비");
				readyButton.setBackground(new Color(249, 191, 0));
				Send_Message("cancel");
			}

		}
	}

	public void setting(boolean flag) {		
		readyButton.setEnabled(false);
		sendButton.setEnabled(flag);
	}

	//////////////////////// 서버로 메세지 보냄 ////////////////////////////
	public void Send_Message(String str) {
		
			str = "1:" + str;
			byte[] bb = new byte[128];
			bb = str.getBytes();
			DatagramPacket udp_packet = new DatagramPacket(bb, bb.length, MainPane.ip_addr, MainPane.port);
			try {
				MainPane.udp_socket.send(udp_packet);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "메세지 송신 에러!!!!", "Message", JOptionPane.ERROR_MESSAGE);
			}
		
	}

}
