import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Chatbot extends JFrame{	
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
	private JMenuItem mntmMultiroundConservation;
	private JMenuItem mntmSingleroundConservation;
	static Analyzer analyzer;
	Logger logger = Logger.getLogger(Chatbot.class);

	public static void main (String [] args) throws IOException, ParseException, Exception{
		Chatbot frame = new Chatbot();
		frame.setTitle("Smart Chatbot with Information Retrieval");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		analyzer = new Analyzer(textField, textArea, btn, false);
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
		
		mntmSingleroundConservation = new JMenuItem("Single-Round Conservation");
		mntmSingleroundConservation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.setSingleRound(true);
				analyzer.setMultiRound(false);
			}
		});
		normalMenu.add(mntmSingleroundConservation);
		
		advancedMenu = new JMenu("Advanced");
		menuBar.add(advancedMenu);
		
		mntmMultiroundConservation = new JMenuItem("Multi-Round Conservation");
		mntmMultiroundConservation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.setSingleRound(false);
				analyzer.setMultiRound(true);
			}
		});
		advancedMenu.add(mntmMultiroundConservation);
		
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
