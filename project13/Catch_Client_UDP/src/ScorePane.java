import java.awt.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class ScorePane extends JPanel {
	private JTextPane textpane;

	ScorePane() {
		setLayout(null);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 333, 187);
		add(scrollPane);

		textpane = new JTextPane();
		textpane.setEnabled(false);
		scrollPane.setViewportView(textpane);
		textpane.setDisabledTextColor(new Color(0, 0, 0));
	}

	//////////////////////// ���ھ� �� ���� ���� �����ֱ� ////////////////////////////
	public void RecvMessage(String str) {
		// ���ο� ������ ��� �޼��� new | ���� �߰��� ��� modi:id:turn:score
		// ���ο� ������ ��� �޼��� new:id:turn:score | ���� ������ ��� �޼��� modi:score:data
		// SimpleAttributeSet att = new SimpleAttributeSet();
		int len = textpane.getDocument().getLength();
		textpane.setCaretPosition(len);

		StringTokenizer st = new StringTokenizer(str, ":");
		String sFlag = new String(st.nextToken());		

		if (sFlag.equals("new")) {
			textpane.setText("");
			UpdateMessage("ID\tTURN\tSCORE\tNOW\t        \n", true);
			//textpane.replaceSelection("ID\tTURN\tSCORE\tNOW\n");
		}
		else if(sFlag.equals("modi")){
			String content = new String(st.nextToken());
			UpdateMessage(content + "\n", false);
			//textpane.replaceSelection(content + "\n");
		}
	}	
	public void UpdateMessage(String str, boolean flag)
	{
		int len = textpane.getDocument().getLength();
		textpane.setCaretPosition(len);

		SimpleAttributeSet att = new SimpleAttributeSet();
		StyleConstants.setFontSize(att, 15);
		
		if(flag)
			StyleConstants.setBackground(att, Color.green);
		else
			StyleConstants.setBackground(att, Color.white);
		
		try {
			textpane.getDocument().insertString(len, str, att);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
