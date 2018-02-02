import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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

public class Analyzer {
	private static final String INDEX_DIRECTORY = "/Users/wongkaho/Eclipse Workspace/Chatbot/resources/INDEX_DIRECTORY/";
	private static final String RESOURCE = "/Users/wongkaho/Eclipse Workspace/Chatbot/resources/";
	private static final String COMMENT = "commentAfterChineseTextSegmentation";
	private static final String POST = "postAfterChineseTextSegmentation";
	
	Logger logger = Logger.getLogger(Analyzer.class);
	JTextField textField;
	JTextArea textArea;
	JButton btnNewButton;
	NumberFormat nf = new DecimalFormat("###");			
	
	StandardAnalyzer standardAnalyzer;
	Directory directoryPost;
	IndexWriterConfig indexWriterConfig;
	IndexWriter indexWriter;
	mmseg4j seg;
	
	public Analyzer(JTextField textField, JTextArea textArea, JButton btnNewButton) {
		this.textField = textField;
		this.textArea = textArea;
		this.btnNewButton = btnNewButton;
	}
	
	public void eventBuilder() throws IOException, ParseException, Exception {
		textField.setEnabled(false);
		btnNewButton.setEnabled(false);
		textArea.append("正在啟動自動回答機械人... \n");
		logger.info("正在啟動自動回答機械人...");
		
		seg = new mmseg4j();
		standardAnalyzer  = new StandardAnalyzer();
		
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

			textArea.append("載入 3922512 個標題中... \n");
			logger.info("載入 3922512 個標題中...");
			JSONParser parserPost = new JSONParser();
			JSONArray posts = (JSONArray) parserPost.parse(new FileReader(RESOURCE + POST + ".json"));
//			double cnt = 0.0;
//			double progress, preprogress = 0;
			for(Object object : posts) {
				JSONObject post = (JSONObject) object;
				String title = (String) post.get("title");
				String id = (String) post.get("id");
				addContent(indexWriter, title, id);
//				cnt++;
//				progress = cnt / posts.size() * 100;
//				if(progress != preprogress) {
//					textArea.setText("正在啟動自動回答機械人... \n載入 3922512 個標題中..." + nf.format(progress) + "%");
//					preprogress = progress;
//				}
			}
			indexWriter.close();
			textArea.append("3922512 個標題已完成載入\n");
			logger.info("3922512 個標題已完成載入");
		}

		textArea.append("我係自動回答機械人，隨便說吧: \n");
		logger.info("我係自動回答機械人，隨便說吧:");
		
		textField.setEnabled(true);
		btnNewButton.setEnabled(true);

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sendRequest(textArea, seg, standardAnalyzer, directoryPost);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error(e1.getLocalizedMessage());
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
						logger.error(e1.getLocalizedMessage());
					}
				}
			}
		});
	}
	
	private void sendRequest(JTextArea textArea, mmseg4j seg, StandardAnalyzer standardAnalyzer, Directory directoryPost) throws Exception{
		textArea.append("你： " + textField.getText() + "\n");
		logger.info("你： " + textField.getText());
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

			logger.info("以下是相似度前 " + hits.length + " 高的標題");
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
				logger.info(d.get("id") + "   " + d.get("title").replace(" | ", "") + "  " + nf.format(hits[i].score));
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
			textArea.append("機械人： " + commentList.get(0).getTitle().replace(" | ", "") + "\n");
			logger.info("機械人： " + commentList.get(0).getTitle().replace(" | ", ""));
			logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
			for (Comment c : commentList) {
				logger.info(c.getId() + "-" + c.getI() + "  " + c.getTitle().replace(" | ", "") + "  " + nf.format(c.getScore()));
			}
		}else {
			textArea.append("輸入錯誤，再說多次吧。" + "\n");
		}
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
}
