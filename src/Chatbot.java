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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

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

	private static final String INDEX_DIRECTORY = "/Users/wongkaho/Eclipse Workspace/Chatbot/resources/INDEX_DIRECTORY/";
	private static final String RESOURCE = "/Users/wongkaho/Eclipse Workspace/Chatbot/resources/";
	private static final String COMMENT = "commentAfterChineseTextSegmentation";
	private static final String POST = "postAfterChineseTextSegmentation";
	private JPanel contentPane;
	private JTextField textField;


	public static void main (String [] args){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Chatbot frame = new Chatbot();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Chatbot() throws IOException, ParseException, Exception {
		initCompoents();
	}

	private void initCompoents() throws IOException, ParseException, Exception {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();

		textField = new JTextField();
		textField.setColumns(10);

		JButton btnNewButton = new JButton("Send");

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(textField, GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
							.addGap(9)
							.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
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
						.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGap(3))
		);

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);
		contentPane.setLayout(gl_contentPane);

		textArea.append("正在啟動自動回答機械人... \n");

		StandardAnalyzer standardAnalyzer;
		Directory directoryPost;
		IndexWriterConfig indexWriterConfig;
		IndexWriter indexWriter;
		//		String queryStr = "";
		standardAnalyzer = new StandardAnalyzer();

		//check whether done the indexing
		File file = new File(INDEX_DIRECTORY);
		if (!file.exists()) {
			file.mkdir();
		}
		if(file.list().length > 0){
			directoryPost = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		}else {
			directoryPost = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
			indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
			indexWriter = new IndexWriter(directoryPost, indexWriterConfig);

			textArea.append("載入 3922512 個標題中...");
			JSONParser parserPost = new JSONParser();
			JSONArray posts = (JSONArray) parserPost.parse(new FileReader(RESOURCE + POST + ".json"));
			for(Object object : posts) {
				JSONObject post = (JSONObject) object;
				String title = (String) post.get("title");
				String id = (String) post.get("id");
				addContent(indexWriter, title, id);
			}
			indexWriter.close();
			textArea.append("3922512 個標題已完成載入\n");
		}

		mmseg4j seg = new mmseg4j();

		textArea.append("我係自動回答機械人，隨便說吧: \n");

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sendRequest(textArea, seg, standardAnalyzer, directoryPost);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		textField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					try {
						sendRequest(textArea, seg, standardAnalyzer, directoryPost);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
	}

	private static void addContent(IndexWriter indexWriter, String title, String id) throws IOException {
		Document document = new Document();
		document.add(new TextField("title", title, Field.Store.YES));
		document.add(new StringField("id", id, Field.Store.YES));
		indexWriter.addDocument(document);
	}

	private static void addContent(IndexWriter indexWriter, String title, String id, String i) throws IOException {
		Document document = new Document();
		document.add(new TextField("title", title, Field.Store.YES));
		document.add(new StringField("id", id, Field.Store.YES));
		document.add(new StringField("i", i, Field.Store.YES));
		indexWriter.addDocument(document);
	}

	private void sendRequest(JTextArea textArea, mmseg4j seg, StandardAnalyzer standardAnalyzer, Directory directoryPost) throws Exception{
		textArea.append("你： " + textField.getText() + "\n");
		String queryStr = textField.getText();
		textField.setText("");

		if(!queryStr.isEmpty()) {
			String querystrAfterChineseTextSegmentation = seg.segmentation(queryStr);

			Query query = new QueryParser("title", standardAnalyzer).parse(querystrAfterChineseTextSegmentation);

			int hitsPerPage = 10;
			IndexReader indexReader = DirectoryReader.open(directoryPost);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new BM25Similarity());
			TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(hitsPerPage);
			indexSearcher.search(query, topScoreDocCollector);
			ScoreDoc[] hits = topScoreDocCollector.topDocs().scoreDocs;

			standardAnalyzer = new StandardAnalyzer();
			RAMDirectory directoryComment = new RAMDirectory();
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
			IndexWriter indexWriter = new IndexWriter(directoryComment, indexWriterConfig);
			List<Post> postList = new ArrayList<Post>();
			List<Comment> commentList = new ArrayList<Comment>();

//			textArea.append("以下是相似度前 " + hits.length + " 高的標題" + "\n");
			System.out.println("以下是相似度前 " + hits.length + " 高的標題");
			DecimalFormat nf = new DecimalFormat("#0.000000");

			HashMap commentHashMap = new HashMap();
			for(int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = indexSearcher.doc(docId);
				int shortID = Integer.valueOf(d.get("id")) / 10000;
				JSONParser parserComment = new JSONParser();
				JSONArray comments = (JSONArray) parserComment.parse(new FileReader(RESOURCE + COMMENT + shortID + ".json"));
				for(Object objectComment : comments) {
					JSONObject comment = (JSONObject) objectComment;
					String id = (String) comment.get("id");
					JSONArray contents = (JSONArray) comment.get("content");
					commentHashMap.put(id, contents);
				}
			}

			for(int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = indexSearcher.doc(docId);
				Post p = new Post();
				Object comment = commentHashMap.get(d.get("id"));
				JSONArray contents = (JSONArray) comment;
				for(int j = 1; j < contents.size(); j++) {
					addContent(indexWriter, contents.get(j).toString(), d.get("id"), String.valueOf(j+1));
					Comment c = new Comment();
					c.setId(d.get("id"));
					c.setI(String.valueOf(j+1));
					c.setTitle(contents.get(j).toString());
					c.setScore(0);
					if(!c.getTitle().isEmpty())
						commentList.add(c);
				}
				p.setId(d.get("id"));
				p.setTitle(d.get("title").replace(" | ", ""));
				p.setScore(hits[i].score * hits[i].score);
				postList.add(p);
//				textArea.append(d.get("id") + " \t " + d.get("title").replace(" | ", "") + " \t" + nf.format(hits[i].score) + "\n");
				System.out.println(d.get("id") + " \t " + d.get("title").replace(" | ", "") + " \t" + nf.format(hits[i].score));
			}

			indexWriter.close();
			indexReader.close();

			hitsPerPage = 100;
			IndexReader indexReaderComment = DirectoryReader.open(directoryComment);
			IndexSearcher indexSearcherComment = new IndexSearcher(indexReaderComment);
			indexSearcherComment.setSimilarity(new BM25Similarity());
			TopScoreDocCollector topScoreDocCollectorComment = TopScoreDocCollector.create(hitsPerPage);
			indexSearcherComment.search(query, topScoreDocCollectorComment);
			hits = topScoreDocCollectorComment.topDocs().scoreDocs;
			for(Comment c : commentList) {
				float postScore = 0;
				for(Post p : postList) {
					if(p.getId().equals(c.getId())) {
						postScore = p.getScore();
					}
				}
				c.setScore(postScore);

				for(int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = indexSearcherComment.doc(docId);
					
					if(c.getId().equals(d.get("id")) && c.getI().equals(d.get("i"))) {
						float score = (hits[i].score * hits[i].score) / Integer.valueOf(c.getI());
//						System.out.println(c.getTitle() + score + " " + hits[i].score + " " + i);
						c.setScore(postScore + score);
					}
				}
			}

			//sort the commentList
			Collections.sort(commentList,
					new Comparator<Comment>() {
				public int compare(Comment c1, Comment c2) {
					return (int) (c2.getScore() - c1.getScore());
				}
			});
//			textArea.append("以下是相似度前 " + commentList.size() + " 高的回覆" + "\n");
			System.out.println("以下是相似度前 " + commentList.size() + " 高的回覆");
			textArea.append("機械人： " + commentList.get(0).getTitle().replace(" | ", "") + "\n");
			for (Comment c : commentList) {
//				textArea.append(c.getId() + "-" + c.getI() + "\t " + c.getTitle().replace(" | ", "") + " \t" + nf.format(c.getScore()) + "\n");
				System.out.println(c.getId() + "-" + c.getI() + "\t " + c.getTitle().replace(" | ", "") + " \t" + nf.format(c.getScore()));
			}
		}else {
			textArea.append("輸入錯誤，再說多次吧。" + "\n");
			//	System.out.println("輸入錯誤，再說多次吧。");
		}
//		textArea.append("我係自動回答機械人，隨便說吧: ");
	}

}
