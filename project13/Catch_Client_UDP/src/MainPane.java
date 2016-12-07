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

	public static DatagramSocket udp_socket; // 연결소켓
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

		// 채팅창 붙임
		chatpane = new ChatPane(_id);
		// chatpane.setBounds(882, 250, 400, 610);
		chatpane.setBounds(703, 190, 340, 540);
		contentpane.add(chatpane);

		// 그림그리는판 붙임
		paintpane = new PaintPane();
		// paintpane.setBounds(3, 3, 875, 855);
		paintpane.setBounds(3, 3, 700, 707);
		contentpane.add(paintpane);

		// 스코어 나타내는판 붙임
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

	//////////////////////// 소켓 할당 ////////////////////////////
	public void network() {
		// 서버에 접속
		try {
			udp_socket = new DatagramSocket();
			if (udp_socket != null) // socket이 null값이 아닐때 즉! 연결되었을때
			{
				Connection(); // 연결 메소드를 호출
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "소켓 접속 에러!!\n", "Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void Connection() { // 실직 적인 메소드 연결부분
		Send_Message(id); // 정상적으로 연결되면 나의 id를 전송			
		Send_Message("1:" + id + "님이 게임에 참여하셨습니다.");
		Thread th = new Thread(new Runnable() { // 스레드를 돌려서 서버로부터 메세지를 수신
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
					
					// 0이면 게임 진행에 관련된 시스템 메세지임
					if (cFlag == '0')
					{
						if(msg.equals("start"))
						{							
							ShowMessage("게임시작 !!");							
						}
						else if(msg.equals("turn"))
						{
							MyTurn = true;
							paintpane.setting(true);
							chatpane.setting(false);
							ShowMessage("당신 차례입니다. 문제를 출제해 주세요!");
						}
						else if(msg.equals("non-turn"))
						{
							paintpane.setting(false);
							chatpane.setting(true);							
						}
						else if(msg.equals("end"))
						{
							System.out.println("게임 종료");	
							System.exit(0);
						}
						else if(msg.equals("ERROR"))
						{
							ShowMessage("게임이 이미 시작중입니다.\n잠시 뒤 다시 접속해 주세요.");
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

				} // while문 끝				
			}// run메소드 끝			
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

	//////////////////////// 서버로 메세지 보냄 ////////////////////////////
	public void Send_Message(String str) {
		byte[] bb = new byte[128];
		bb = str.getBytes();
		DatagramPacket udp_packet = new DatagramPacket(bb, bb.length, ip_addr, port);
		try {
			udp_socket.send(udp_packet);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "메세지 송신 에러!!!!", "Message", JOptionPane.ERROR_MESSAGE);
		}
	}

}
