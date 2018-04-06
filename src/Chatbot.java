import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

public class Chatbot extends JFrame{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static JTextField textField;
	private static JTextArea textArea;
	private static JButton btn;
	private JScrollPane scrollPane;
	private GroupLayout gl_contentPane;
	private JMenuBar menuBar;
	private JMenu normalMenu;
	private JMenu exitMenu;
	private JMenu advancedMenu;
	private JMenuItem mntmExit;
	private JMenuItem mntmSingleroundConversation;
	private JMenuItem mntmMultiroundQ1KeywordConversation;
	private JMenuItem mntmMultiroundQ1Conversation;
	private JMenuItem mntmMultiroundQ1R1Conversation;
	static Analyzer analyzer;
	Logger logger = Logger.getLogger(Chatbot.class);

	public static void main (String [] args) throws IOException, ParseException, Exception{
		Chatbot frame = new Chatbot();
		frame.setTitle("Smart Chatbot with Information Retrieval");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		analyzer = new Analyzer(textField, textArea, btn);
		analyzer.eventBuilder();
	}

	public Chatbot() {
		initCompoents();
	}

	private void initCompoents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		normalMenu = new JMenu("Normal");
		menuBar.add(normalMenu);
		
		mntmSingleroundConversation = new JMenuItem("Single-Round Conservation");
		mntmSingleroundConversation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.append("Single-Round Conservation \n");
				analyzer.setType("singleRound");
			}
		});
		normalMenu.add(mntmSingleroundConversation);
		
		advancedMenu = new JMenu("Advanced");
		menuBar.add(advancedMenu);
		
		mntmMultiroundQ1KeywordConversation = new JMenuItem("Multi-Round Conversation: Q1 with Keyword");
		mntmMultiroundQ1KeywordConversation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.append("Multi-Round Conversation: Q1 with Keyword \n");
				analyzer.setType("multiRoundQ1Keyword");
			}
		});
		advancedMenu.add(mntmMultiroundQ1KeywordConversation);
		
		mntmMultiroundQ1Conversation = new JMenuItem("Multi-Round Conversation: Q1");
		mntmMultiroundQ1Conversation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.append("Multi-Round Conversation: Q1 \n");
				analyzer.setType("multiRoundQ1");
			}
		});
		advancedMenu.add(mntmMultiroundQ1Conversation);
		
		mntmMultiroundQ1R1Conversation = new JMenuItem("Multi-Round Conversation: Q1 and R1");
		mntmMultiroundQ1R1Conversation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.append("Multi-Round Conversation: Q1 and R1 \n");
				analyzer.setType("multiRoundQ1R1");
			}
		});
		advancedMenu.add(mntmMultiroundQ1R1Conversation);
		
		
		exitMenu = new JMenu("Exit");
		menuBar.add(exitMenu);
		
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		exitMenu.add(mntmExit);

		scrollPane = new JScrollPane();

		textField = new JTextField();
		textField.setColumns(10);

		btn = new JButton("Send");

		gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
										.addComponent(textField, GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
										.addGap(9)
										.addComponent(btn, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED))
								.addGroup(gl_contentPane.createSequentialGroup()
										.addGap(6)
										.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)))
						.addGap(3))
				);
		gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
						.addGap(12)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGap(3))
				);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);
		contentPane.setLayout(gl_contentPane);
	}
}
