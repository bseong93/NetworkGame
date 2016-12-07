import java.awt.event.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.net.DatagramPacket;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class PaintPane extends JPanel {

	private MyPane paint_pane = new MyPane();
	private JOptionPane optionpane;
	ScrollBarControl scrollbar = new ScrollBarControl();
	private JButton button_list[] = { new JButton(), new JButton(), new JButton(), new JButton(), new JButton("eraser"),
			new JButton("clear"), new JButton("Undo") };
	private JTextField answer_input = new JTextField();
	private JButton answer_submit = new JButton("정답 제출");
	private JButton answer_giveup = new JButton("정답 공개");
	private Color color_list[] = { Color.black, Color.red, Color.green, Color.blue, Color.white, Color.white,
			Color.gray };
	private String msg_list[] = { "black", "red", "green", "blue", "eraser", "init", "undo" };
	private Color now_color = new Color(0, 0, 0);

	Point start_node = null, node = null;
	Point point;

	private int point_size = 15;
	static boolean flag = true, startflag = false;

	PaintPane() {
		setLayout(null);
		setBackground(new Color(56, 153, 224));

		paint_pane.setBounds(15, 20, 670, 610);
		answer_input.setBounds(445, 595, 150, 35);
		answer_submit.setBounds(595, 595, 90, 35);
		answer_giveup.setBounds(595, 630, 90, 35);
		scrollbar.setBounds(10, 635, 580, 35);

		AddButton();
		add(scrollbar);
		add(paint_pane);		
	}

	//////////////////////// 버튼을 붙임 ////////////////////////////
	public void AddButton() {
		for (int i = 0; i < 7; i++) {
			button_list[i].setBounds(15 + 80 * i, 670, 75, 40);
			button_list[i].setBackground(color_list[i]);
			AddActionListener(button_list[i], i);
			add(button_list[i]);
		}
		add(answer_input);
		answer_submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!answer_input.getText().equals("")) {
					Send_Message("submit:" + answer_input.getText().trim());
					answer_input.setEnabled(false);
					answer_submit.setEnabled(false);
					answer_giveup.setEnabled(true);
				}

			}
		});
		answer_input.setEnabled(false);
		answer_submit.setEnabled(false);
		add(answer_submit);
		answer_giveup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!answer_input.getText().trim().equals(""))
					Send_Message("giveup");
			}
		});
		answer_giveup.setEnabled(false);
		add(answer_giveup);
	}

	public void AddActionListener(JButton button, int i) {
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Send_Message("color:" + msg_list[i]);
			}
		});
	}

	// 그림을 그릴 판
	//////////////////////////////////////////////// ////////////////////////////////////////////////////

	class MyPane extends Canvas {
		ImageIcon icon = new ImageIcon("images/note.png");
		Image img = icon.getImage();

		Image doublebuffer;
		Image doublebuffer_pre;
		Graphics gc, gc_pre;
		
		int width = 9;

		MyPane() {
			MyMouseListener listener = new MyMouseListener();

			addMouseListener(listener);
			addMouseMotionListener(listener);
		}

		public void backup() {
			gc_pre.drawImage(doublebuffer, 0, 0, 670, 610, this);
		}

		public void undo() {
			Image TempBuffer = createImage(670, 610);
			Graphics TempGC = TempBuffer.getGraphics();

			TempGC.drawImage(doublebuffer, 0, 0, 670, 610, this);
			gc.drawImage(doublebuffer_pre, 0, 0, 670, 610, this);
			gc_pre.drawImage(TempBuffer, 0, 0, 670, 610, this);

			repaint();
		}

		public void paint(Graphics g) {
			if (gc == null) {
				doublebuffer = createImage(670, 610);
				gc = doublebuffer.getGraphics();
				gc.drawImage(img, 0, 0, 670, 610, this);
			}
			if (gc_pre == null) {
				doublebuffer_pre = createImage(670, 610);
				gc_pre = doublebuffer_pre.getGraphics();
				gc_pre.drawImage(img, 0, 0, 670, 610, this);
			}
			update(g);
		}

		public void update(Graphics g) {
			if (gc == null)
				return;

			doublebuffering();// 오프스크린 버퍼에 그리기
			g.drawImage(doublebuffer, 0, 0, this);// 오프스크린 버퍼를 메인화면에 그린다.
		}

		public void doublebuffering() {
			Graphics2D g2d = (Graphics2D) gc;
			g2d.setColor(now_color);
			
			g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			if (start_node != null && node == null)
				gc.fillOval((int)(start_node.getX() - width/2), (int)( start_node.getY() - width/2), width, width);
				//gc.fillOval((int) start_node.getX() - 5, (int) start_node.getY() - 5, 10, 10);
			if (start_node != null && node != null)
				g2d.draw(new Line2D.Float(start_node.x, start_node.y, node.x, node.y));
		}

		class MyMouseListener extends MouseAdapter {
			public void mousePressed(MouseEvent e) {
				if (startflag && answer_input.isEnabled())
				{
					ShowMessage("정답부터 입력해주세요 !!");
					return;
				}
				String msg = new String("mouse_down");
				Send_Message(msg);
				msg = new String("point:" + e.getX() + ":" + e.getY());
				Send_Message(msg);				
			}

			public void mouseDragged(MouseEvent e) {
				String msg = new String("point:" + e.getX() + ":" + e.getY());
				Send_Message(msg);
			}

			public void mouseReleased(MouseEvent e) {
				String msg = new String("mouse_up");
				Send_Message(msg);
			}
		}
	}

	// Panel

	//////////////////////// 서버로 메세지 보냄 ////////////////////////////
	public void Send_Message(String str) {
		if (flag) {
			str = "2:" + str;
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

	//////////////////////// PaintPane으로 메시지가 전달되었을 경우 호출
	public void RecvMessage(String str) {
		// 좌표일 경우 point:x:y | 색상일 경우 color:green | 지우개일경우 erase | 모두 지울 경우 init
		// 차례가 아닐 경우 stop | 차례일 경우 start를 서버로부터 받는다.
		StringTokenizer st = new StringTokenizer(str, ":");
		String sFlag = new String(st.nextToken());

		if (sFlag.equals("point")) {
			Point TempPoint = new Point(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
			if (start_node == null)
				start_node = TempPoint;
			else if (node == null)
				node = TempPoint;
			else {
				start_node = node;
				node = TempPoint;
			}
			paint_pane.repaint();
		} else if (sFlag.equals("mouse_up")) {
			start_node = node = null;
		} else if (sFlag.equals("mouse_down")) {
			paint_pane.backup();
		} else if (sFlag.equals("color")) {
			String color = new String(st.nextToken());
			switch (color) {
			case "black":
				now_color = new Color(0, 0, 0);
				break;
			case "red":
				now_color = new Color(255, 0, 0);
				break;
			case "green":
				now_color = new Color(0, 255, 0);
				break;
			case "blue":
				now_color = new Color(0, 0, 255);
				break;
			case "eraser":
				now_color = new Color(255, 255, 255);
				break;
			case "init":
				paint_pane.backup();
				paint_pane.gc.drawImage(paint_pane.img, 0, 0, 670, 610, paint_pane);
				paint_pane.repaint();
				break;
			case "undo":
				paint_pane.undo();
				break;
			case "stroke":
				int n = Integer.parseInt(st.nextToken());
				paint_pane.width = n;
				break;
			}
		}
	}

	public void ShowMessage(String str) {
		optionpane = new JOptionPane(str, JOptionPane.ERROR_MESSAGE);
		JDialog d = optionpane.createDialog("Error");
		d.setLocation(this.getLocationOnScreen().x + this.getWidth() / 2,
				this.getLocationOnScreen().y + this.getHeight() / 2 - 50);
		d.setVisible(true);
	}

	//////////////////////// 차례일 경우 or 차례가 아닐경우 ////////////////////////
	public void setting(boolean flag) {
		this.startflag = true;

		this.flag = flag;
		scrollbar.setEnabled(flag);
		answer_input.setText("");
		answer_input.setEnabled(flag);
		answer_submit.setEnabled(flag);
		answer_giveup.setEnabled(false);
	}
}

class PointData {
	Point po;
	Color color;

	public PointData(Point _po, Color _color) {
		this.po = new Point(_po);
		this.color = _color;
	}
}
