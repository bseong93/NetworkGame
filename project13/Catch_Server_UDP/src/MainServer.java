import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.Vector;

public class MainServer extends JFrame {
	private Container contentpane;
	private JTextField textfield;
	private JButton button;
	JTextArea textarea;

	private int port; // 포트번호
	private DatagramSocket udp_socket = null;

	private Vector<UserData> v = new Vector<UserData>();
	private int PlayerCount = 0;
	private int TurnCount = 0;
	private int MaxScore = 3;
	private boolean startflag = false;
	private String answer = new String("");
	
	public static void main(String args[])
	{
		MainServer main = new MainServer();
	}


	MainServer() {
		setBounds(500, 200, 350, 550);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentpane = getContentPane();
		contentpane.setBackground(new Color(80, 180, 210));
		contentpane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(3, 3, 328, 395);
		contentpane.add(scrollPane);

		textarea = new JTextArea();
		textarea.setEnabled(false);
		scrollPane.setViewportView(textarea);
		textarea.setDisabledTextColor(new Color(0, 0, 0));

		JLabel label = new JLabel("Please Input Port Number : ");
		label.setBounds(3, 400, 250, 35);
		contentpane.add(label);

		textfield = new JTextField("30000");
		textfield.setBounds(3, 430, 210, 35);
		textfield.addActionListener(new buttonlistener());
		contentpane.add(textfield);

		button = new JButton("서버 실행");
		button.setBounds(215, 430, 117, 35);
		button.addActionListener(new buttonlistener());
		contentpane.add(button);

		setVisible(true);
	}

	/////////////////////////// 리스너 ///////////////////////////
	class buttonlistener implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == button || e.getSource() == textfield) {
				if (textfield.getText().equals("") || textfield.getText().length() == 0) {
					textfield.setText("포트번호를 입력해주세요");
					textfield.requestFocus(); // 포커스를 다시 textField에 넣어준다
				} else {
					try {
						port = Integer.parseInt(textfield.getText());
						server_start();
					} catch (Exception er) {
						// 사용자가 숫자로 입력하지 않았을시에는 재입력을 요구한다
						textfield.setText("숫자로 입력해주세요");
						textfield.requestFocus();
					}
				}
			}

		}
	}

	////////////////////// 서버 시작, 사용자 접속을 받을 스레드 ///////////////////////////
	private void server_start() {
		Thread th = new Thread(new Runnable() {
			public void run() {
				// socket = new ServerSocket(Port); // 서버가 포트 여는부분
				button.setText("서버실행중");
				button.setEnabled(false);
				textfield.setEnabled(false);
				// DatagramSocket udp_socket = null;
				try {
					udp_socket = new DatagramSocket(port);
				} catch (SocketException e) {
					e.printStackTrace();
				}
				byte[] bb = new byte[128];
				DatagramPacket udp_packet = new DatagramPacket(bb, bb.length);
				while (true) {
					for (int i = 0; i < bb.length; i++)
						bb[i] = 0;

					try {
						udp_socket.receive(udp_packet);
						// 새로운 유저 참가시
						if (compare(udp_packet.getSocketAddress()) == null) {
							// 이미 게임이 시작중임
							if (startflag) {
								byte[] buffer = new String("0:ERROR").getBytes();
								DatagramPacket temp_packet = new DatagramPacket(buffer, buffer.length, udp_packet.getSocketAddress());
								udp_socket.send(temp_packet);
								continue;
							} else {
								PlayerCount++;
								v.add(new UserData(udp_packet.getSocketAddress(), udp_packet.getData()));
								BroadCastUserData();
								continue;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					String msgStr = new String(bb).trim();
					textarea.append(udp_packet.getAddress() + ":" + udp_packet.getPort() + " " + msgStr + "\n");
					textarea.setCaretPosition(textarea.getText().length());

					if (msgStr.equals("stop_connection")) {
						DeleteUser(udp_packet.getSocketAddress());
						continue;
					}

					char cFlag = msgStr.charAt(0);

					// 채팅 패널에서 날라온 메세지

					if (cFlag == '1') {
						String Msg = msgStr.substring(2).trim();
						String Answer_Msg = null;
						int n = Msg.indexOf(':');
						
						try{
							Answer_Msg  = Msg.substring(n + 3, n + 3 + answer.length());
						}
						catch(StringIndexOutOfBoundsException e)
						{
							Answer_Msg = new String("");
						}
						
						
						if (!answer.equals("") && Answer_Msg.equals(answer)) {
							UserData userdata = compare(udp_packet.getSocketAddress());
							userdata.score++;
							BroadCastUserData();
							BroadCastMsg(new String(udp_packet.getData()));
							if (userdata.score >= MaxScore) {
								BroadCastMsg("0:" + userdata.id + "님 정답!!!\n" + userdata.id + "님이 " + MaxScore
										+ "을 달성하여 " + userdata.id + "님의 승리!!!");
								BroadCastMsg("0:end");
								ReleaseData();
							} else {
								BroadCastMsg("0:" + userdata.id + "님 정답!!!");
								NextTurn();
							}
							continue;
						} else if (Msg.equals("ready")) {
							UserData userdata = compare(udp_packet.getSocketAddress());
							userdata.isturn = new String("준비완료");
							BroadCastUserData();

							// 모든 유저 확인해서 모두 ready 상태면 출제자한테 0:stop 보냄, 게임이 끝나면
							// 모두한테 0:start보냄
							CheckReady();
							continue;
						} else if (Msg.equals("cancel")) {
							UserData userdata = compare(udp_packet.getSocketAddress());
							userdata.isturn = new String("");
							BroadCastUserData();
							continue;
						}
					}
					// 그림 패널에서 날라온 메세지
					else if (cFlag == '2') {
						StringTokenizer st = new StringTokenizer(msgStr.substring(2), ":");
						String sFlag = st.nextToken();
						if (sFlag.equals("submit")) {
							answer = new String(st.nextToken().trim());
							continue;
						} else if (sFlag.equals("giveup")) {
							BroadCastMsg("0:" + "무승부!!! 이번 정답은 " + answer + " 였습니다");
							NextTurn();
							continue;
						}
					}

					BroadCastMsg(new String(udp_packet.getData()));
				}
			}
		});
		th.start();
	}

	/////////////////////////// 각 소켓을 저장할 클래스 ///////////////////////////
	class UserData {
		SocketAddress address;

		String id;
		String isturn;
		int score;
		int turn;

		UserData(SocketAddress _address, byte[] b) {
			this.address = _address;

			id = new String(b).trim();
			isturn = new String("");
			score = 0;
			turn = PlayerCount;
		}

		String GetDataString() {
			return new String("3:modi:" + id + '\t' + turn + '\t' + score + '\t' + isturn);
		}
	}

	/////////////////////////// 필요한 메소드들 ///////////////////////////
	UserData compare(SocketAddress _address) {
		for (int i = 0; i < v.size(); i++) {
			if (v.elementAt(i).address.toString().equals(_address.toString()))
				return v.elementAt(i);
		}
		return null;
	}

	void DeleteUser(SocketAddress _address) {
		int i = 0;
		for (; i < v.size(); i++) {
			if (v.elementAt(i).address.toString().equals(_address.toString())) {
				v.removeElementAt(i);
				PlayerCount--;
				break;
			}
		}

		for (; i < v.size(); i++) {
			v.elementAt(i).turn--;
		}			
		if(startflag)
		{
			if(TurnCount >= PlayerCount)
				TurnCount--;
			NextTurn();
		}
		if(v.size() == 0)
			ReleaseData();
		
		
		BroadCastUserData();		
	}

	void BroadCastUserData() {
		BroadCastMsg("3:new");

		DatagramPacket udp_packet = null;
		for (int i = 0; i < v.size(); i++) {
			String msg = v.elementAt(i).GetDataString();
			BroadCastMsg(msg);
		}
	}

	void BroadCastMsg(String msg) {
		msg = msg.trim();
		byte[] buffer = msg.getBytes();
		DatagramPacket udp_packet = null;
		for (int i = 0; i < v.size(); i++) {
			udp_packet = new DatagramPacket(buffer, buffer.length, v.elementAt(i).address);
			try {
				udp_socket.send(udp_packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void CheckReady() {
		// 모두 준비상태인지 확인
		for (int i = 0; i < v.size(); i++) {
			if (v.elementAt(i).isturn.equals(""))
				return;
		}
		// 모두 준비상태면 턴 표시하고 게임 시작
		startflag = true;
		BroadCastMsg("0:start");
		NextTurn();

	}

	void NextTurn() {
		answer = new String("");
		DatagramPacket udp_packet = null;
		byte buffer[] = new String("0:turn").getBytes();
		byte buffer2[] = new String("0:non-turn").getBytes();
		for (int i = 0; i < v.size(); i++) {
			if (i == TurnCount) {
				v.elementAt(i).isturn = new String("출제자");
				udp_packet = new DatagramPacket(buffer, buffer.length, v.elementAt(i).address);
				try {
					udp_socket.send(udp_packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				v.elementAt(i).isturn = new String("");
				udp_packet = new DatagramPacket(buffer2, buffer2.length, v.elementAt(i).address);
				try {
					udp_socket.send(udp_packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		TurnCount++;
		if (TurnCount >= PlayerCount)
			TurnCount = 0;

		BroadCastMsg("2:color:init");
		BroadCastUserData();
	}

	void ReleaseData() {
		answer = new String("");
		v.removeAllElements();
		PlayerCount = 0;
		TurnCount = 0;
		startflag = false;

	}

	/////////////////////////// MainPane ///////////////////////////
}

// udp_packet = new DatagramPacket(buffer, buffer.length,
// udp_packet.getAddress(), udp_packet.getPort());
// echo back
// udp_socket.send(udp_packet);
// broadcast
