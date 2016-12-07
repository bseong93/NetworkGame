import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.swing.*;
import java.awt.*;

public class MainPane extends JFrame {
	Container contentpane;
	ChatPane chatpane;
	PaintPane paintpane;
	ScorePane scorepane;
	JOptionPane optionpane;// = new JOptionPane("Click one:", JOptionPane.INFORMATION_MESSAGE);

	String id;

	public static DatagramSocket udp_socket; // �������
	public static InetAddress ip_addr;
	public static int port;
	public static InputStream is;
	public static OutputStream os;
	public static DataInputStream dis;
	public static DataOutputStream dos;
	
	private boolean MyTurn = false;
	

	MainPane(String _ip, int _port, String _id) throws UnknownHostException {
		this.ip_addr = InetAddress.getByName(_ip);
		this.port = _port;
		this.id = _id;

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		contentpane = getContentPane();
		contentpane.setBackground(new Color(40, 140, 190));
		setBounds(120, 70, 1050, 750);
		setLayout(null);

		// ä��â ����
		chatpane = new ChatPane(_id);
		// chatpane.setBounds(882, 250, 400, 610);
		chatpane.setBounds(703, 190, 340, 540);
		contentpane.add(chatpane);

		// �׸��׸����� ����
		paintpane = new PaintPane();
		// paintpane.setBounds(3, 3, 875, 855);
		paintpane.setBounds(3, 3, 700, 707);
		contentpane.add(paintpane);

		// ���ھ� ��Ÿ������ ����
		scorepane = new ScorePane();
		scorepane.setBounds(705, 3, 335, 185);
		contentpane.add(scorepane);

		setVisible(true);
		network();
		
		addWindowListener(new WindowAdapter() {
		      public void windowClosing(WindowEvent e) {		    	 
		    	  Send_Message("stop_connection");
	    		  System.exit(0);
		      }
		});
	}

	//////////////////////// ���� �Ҵ� ////////////////////////////
	public void network() {
		// ������ ����
		try {
			udp_socket = new DatagramSocket();
			if (udp_socket != null) // socket�� null���� �ƴҶ� ��! ����Ǿ�����
			{
				Connection(); // ���� �޼ҵ带 ȣ��
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "���� ���� ����!!\n", "Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void Connection() { // ���� ���� �޼ҵ� ����κ�
		Send_Message(id); // ���������� ����Ǹ� ���� id�� ����			
		Send_Message("1:" + id + "���� ���ӿ� �����ϼ̽��ϴ�.");
		Thread th = new Thread(new Runnable() { // �����带 ������ �����κ��� �޼����� ����
			@SuppressWarnings("null")
			@Override
			public void run() {
				byte[] bb = new byte[128];
				DatagramPacket udp_packet = new DatagramPacket(bb, bb.length);
				while (true) {
					for (int i = 0; i < bb.length; i++) {
						bb[i] = 0;
					}
					try {
						udp_socket.receive(udp_packet);
					} catch (IOException e) {
						e.printStackTrace();
					}

					char cFlag = 0;
					String msg = new String(bb);
					msg = msg.trim();

					cFlag = msg.charAt(0);
					msg = msg.substring(2);
					
					// 0�̸� ���� ���࿡ ���õ� �ý��� �޼�����
					if (cFlag == '0')
					{
						if(msg.equals("start"))
						{							
							ShowMessage("���ӽ��� !!");							
						}
						else if(msg.equals("turn"))
						{
							MyTurn = true;
							paintpane.setting(true);
							chatpane.setting(false);
							ShowMessage("��� �����Դϴ�. ������ ������ �ּ���!");
						}
						else if(msg.equals("non-turn"))
						{
							paintpane.setting(false);
							chatpane.setting(true);							
						}
						else if(msg.equals("end"))
						{
							System.out.println("���� ����");	
							System.exit(0);
						}
						else if(msg.equals("ERROR"))
						{
							ShowMessage("������ �̹� �������Դϴ�.\n��� �� �ٽ� ������ �ּ���.");
							System.exit(0);
						}
						else
							ShowMessage(msg);						
						
					}
					else if (cFlag == '1')
						chatpane.UpdateMessage(msg + "\n");
					else if (cFlag == '2')
						paintpane.RecvMessage(msg);
					else if (cFlag == '3')
						scorepane.RecvMessage(msg);

				} // while�� ��				
			}// run�޼ҵ� ��			
		});
		th.start();
	}
	
	public void ShowMessage(String str)
	{
		optionpane = new JOptionPane(str, JOptionPane.INFORMATION_MESSAGE);
		JDialog d = optionpane.createDialog("Message"); 
	    d.setLocation(getLocation().x + getWidth()/2, getLocation().y + getHeight()/2 - 50);            
	    d.setVisible(true);
	}

	//////////////////////// ������ �޼��� ���� ////////////////////////////
	public void Send_Message(String str) {
		byte[] bb = new byte[128];
		bb = str.getBytes();
		DatagramPacket udp_packet = new DatagramPacket(bb, bb.length, ip_addr, port);
		try {
			udp_socket.send(udp_packet);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "�޼��� �۽� ����!!!!", "Message", JOptionPane.ERROR_MESSAGE);
		}
	}

}
