import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ScrollBarControl extends JPanel {
	JSlider slider;
	public ScrollBarControl() {
		super(true);
		//this.setBackground(new Color(56, 153, 224));		
		this.setLayout(new BorderLayout());
		slider = new JSlider(JSlider.HORIZONTAL, 0, 50, 25);
		slider.setBackground(new Color(56, 153, 224));

		slider.setMinorTickSpacing(2);
		slider.setMajorTickSpacing(10);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		Hashtable label = new Hashtable();
		label.put(new Integer(0), new JLabel("S"));
		label.put(new Integer(50), new JLabel("L"));		
		slider.setLabelTable(label);
		slider.setPaintLabels(true);

		// We'll just use the standard numeric labels for now...
		//slider.setLabelTable(slider.createStandardLabels(10));

		add(slider, BorderLayout.CENTER);
		slider.addChangeListener(new MyListener());
	}

	class MyListener implements ChangeListener {
		public void stateChanged(ChangeEvent changeEvent) {
			Object source = changeEvent.getSource();
			if(source instanceof JSlider)
			{
				String msg = new String("color:stroke:" + ((JSlider)source).getValue());
				Send_Message(msg);
			}				
		}
	}
	
	public void SetValue(int n)
	{
		slider.setValue(n);
	}

	//////////////////////// 서버로 메세지 보냄 ////////////////////////////
	public void Send_Message(String str) {
		if (PaintPane.flag) {
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
}