import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
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
	
	StandardAnalyzer standardAnalyzer;
	Directory directoryPost;
	IndexWriterConfig indexWriterConfig;
	IndexWriter indexWriter;
	mmseg4j seg;
	
	JTextField textField;
	JTextArea textArea;
	JButton btn;
	String type = "singleRound";
	String queryStr = "";
	String extendqueryStr = "";
	int cntRound = 0;
	List<String> reply = new ArrayList<String>();
	NumberFormat nf = new DecimalFormat("###");	
	Map<String, Integer> txtHashMap = new HashMap<String, Integer>();
	
	Logger logger = Logger.getLogger(Analyzer.class);
	
	//do the evaluation
	boolean evaluation = false;
	
	public Analyzer(JTextField textField, JTextArea textArea, JButton btn) {
		this.textField = textField;
		this.textArea = textArea;
		this.btn = btn;
	}
	
	public void eventBuilder() throws IOException, ParseException, Exception {
		textField.setEnabled(false);
		btn.setEnabled(false);
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
			for(Object object : posts) {
				JSONObject post = (JSONObject) object;
				String title = (String) post.get("title");
				String id = (String) post.get("id");
				addContent(indexWriter, title, id);
			}
			indexWriter.close();
			textArea.append("3922512 個標題已完成載入\n");
			logger.info("3922512 個標題已完成載入");
		}

		textArea.append("我係自動回答機械人，隨便說吧: \n");
		logger.info("我係自動回答機械人，隨便說吧:");
		
		textField.setEnabled(true);
		btn.setEnabled(true);

		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sendRequest(textArea, seg, standardAnalyzer, directoryPost);
				} catch (Exception e1) {
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
						logger.error(e1);
					}
				}
			}
		});
		
		if(evaluation) {
			evaluation();
		}
	}
	
	private void sendRequest(JTextArea textArea, mmseg4j seg, StandardAnalyzer standardAnalyzer, Directory directoryPost) throws Exception{
		textArea.append("你： " + textField.getText() + "\n");
		logger.info("你： " + textField.getText());
		
		if(type.equals("singleRound")) {
			queryStr = singleRoundQuery();
		}else{
			queryStr = multiRoundQuery();
		}
		
		textField.setText("");

		if(!queryStr.isEmpty()) {
			String querystrAfterChineseTextSegmentation = seg.segmentation(queryStr);
			System.out.println(querystrAfterChineseTextSegmentation);

			Query query = new QueryParser("title", standardAnalyzer).parse(querystrAfterChineseTextSegmentation);

			int hitsPerPage = 10;
			IndexReader indexReader = DirectoryReader.open(directoryPost);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new BM25Similarity((float)1.2, 1));
			ScoreDoc scoreDoc = new ScoreDoc(20, 200);
			TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(hitsPerPage, scoreDoc);
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
					JSONArray contents = (JSONArray) comment.get("contents");
					commentHashMap.put(id, contents);
				}
			}

			for(int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = indexSearcher.doc(docId);
				Post p = new Post();
				JSONArray contents = (JSONArray) commentHashMap.get(d.get("id"));
				for(Object o : contents) {
					JSONObject jo = (JSONObject) o;
					String content = jo.get("content").toString();
					String contentAfterSegmentation = jo.get("contentAfterSegmentation").toString();
					String I = jo.get("i").toString();
					if(Integer.valueOf(I) > 1) {
						addContent(indexWriter, contentAfterSegmentation, d.get("id"), I);
						Comment c = new Comment();
						c.setId(d.get("id"));
						c.setI(I);
						c.setContent(content.trim());
						c.setContentAfterSegmentation(contentAfterSegmentation);
						c.setScore(0);
						if(!c.getContent().isEmpty()) {
							commentList.add(c);
						}
					}
				}
				p.setId(d.get("id"));
				p.setTitle(d.get("title").replace(" | ", ""));
				p.setScore(hits[i].score * hits[i].score);
				postList.add(p);
				logger.info(d.get("id") + "   " + d.get("title").replace(" | ", "") + "  " + nf.format(hits[i].score));
			}

			indexWriter.close();
			indexReader.close();

			//calculate the result
			hitsPerPage = 100;
			IndexReader indexReaderComment = DirectoryReader.open(directoryComment);
			IndexSearcher indexSearcherComment = new IndexSearcher(indexReaderComment);
			indexSearcherComment.setSimilarity(new BM25Similarity());
			TopScoreDocCollector topScoreDocCollectorComment = TopScoreDocCollector.create(hitsPerPage);
			Query query2 = new QueryParser("title", standardAnalyzer).parse(querystrAfterChineseTextSegmentation);
			indexSearcherComment.search(query2, topScoreDocCollectorComment);
			hits = topScoreDocCollectorComment.topDocs().scoreDocs;
			for(Comment c : commentList) {
				float postScore = 0;

				for(int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = indexSearcherComment.doc(docId);

					if(c.getId().equals(d.get("id")) && c.getI().equals(d.get("i"))) {
						float score = (hits[i].score * hits[i].score);
						c.setScore((postScore + score) / Integer.valueOf(c.getI()));
					}
					
					if(c.getContent().contains("�")) {
						c.setScore(0);
					}
				}
			}

			//sort the commentList
			Collections.sort(commentList, new Comparator<Comment>() {
				public int compare(Comment c1, Comment c2) {
					return Double.compare(c2.getScore(), c1.getScore());
				}
			});

			switch (type) {
			case "singleRound":
				singleRound(commentList);
				break;
			case "multiRoundQ1Keyword":
				multiRoundQ1Keyword(commentList);
				break;
			case "multiRoundQ1":
				multiRoundQ1(commentList);
				break;
			case "multiRoundQ1R1":
				multiRoundQ1R1(commentList);
				break;
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
	
	public void reset() {
		queryStr = "";
		extendqueryStr = "";
		cntRound = 0;
		reply = new ArrayList<String>();
		txtHashMap = new HashMap<String, Integer>();
		textArea.setText("我係自動回答機械人，隨便說吧:" + "\n");
	}
	
	public void setType (String type) {
		this.type = type;
		reset();
	}
	
	public String singleRoundQuery() {
		return textField.getText();
	}
	
	public String multiRoundQuery() {
		if(cntRound == 3) {
			reset();
		}
		cntRound++;
		return textField.getText() + " " + extendqueryStr;
	}
	
	public void singleRound(List<Comment> commentList) {
		textArea.append("機械人： " + commentList.get(0).getContent().trim() + "\n");
		logger.info("機械人： " + commentList.get(0).getContent());
		logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
		for (Comment c : commentList) {
			logger.info(c.getId() + "-" + c.getI() + "  " + c.getContent() + "  " + nf.format(c.getScore()));
		}
	}
	
	public void multiRoundQ1Keyword(List<Comment> commentList) {
		//do the count
		Map<String, Integer> txtHashMap = new HashMap<String, Integer>();
		for(Comment c : commentList) {
			List<String> list = new ArrayList<String>();
			String content = c.getContentAfterSegmentation().replace(" | ", "");
			for(int i = 0; i < content.length() - 1; i++) {
				int cnt = i + 1;
				for(int j = cnt; j < content.length(); j++) {
					String s = content.substring(i, j);
					if(s.length() > 0 && !s.matches(".*[a-z].*"))
						list.add(s);
				}
			}
			for(String s : list) {
				int count = txtHashMap.containsKey(s) ? txtHashMap.get(s) : 0;
				txtHashMap.put(s, count + 1);
			}
		}
		
		Object[] a = txtHashMap.entrySet().toArray();
		Arrays.sort(a, new Comparator() { 
			public int compare(Object o1, Object o2) { 
				return ((Map.Entry<String, Integer>) o2).getValue().compareTo(((Map.Entry<String, Integer>) o1).getValue());
		    }
		});
		for (Object e : a) {
			int length = ((Map.Entry<String, Integer>) e).getKey().toString().trim().length();
			if(queryStr.contains(((Map.Entry<String, Integer>) e).getKey()) && ((Map.Entry<String, Integer>) e).getKey().length() > 1) {
				System.out.println(((Map.Entry<String, Integer>) e).getKey() + " : " + ((Map.Entry<String, Integer>) e).getValue());
				if(!extendqueryStr.contains(((Map.Entry<String, Integer>) e).getKey())) {
					extendqueryStr = extendqueryStr + " " + ((Map.Entry<String, Integer>) e).getKey();
				}
			}
		}
		
		if(cntRound == 0) {
			textArea.append("機械人： " + commentList.get(0).getContent().trim() + "\n");
			logger.info("機械人： " + commentList.get(0).getContent());
			logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
			for (Comment c : commentList) {
				logger.info(c.getId() + "-" + c.getI() + "  " + c.getContent() + "  " + nf.format(c.getScore()));
			}
			reply.add(commentList.get(0).getId() + "-" + commentList.get(0).getI());
		}else {
			Iterator<Comment> i = commentList.iterator();
			while (i.hasNext()) {
				Comment c = i.next();
				for(String r : reply)
					if((c.getId() + "-" + c.getI()).equals(r))
						i.remove();
			}
			textArea.append("機械人： " + commentList.get(0).getContent().trim() + "\n");
			logger.info("機械人： " + commentList.get(0).getContent());
			logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
			for (Comment c : commentList) {
				logger.info(c.getId() + "-" + c.getI() + "  " + c.getContent() + "  " + nf.format(c.getScore()));
			}
			reply.add(commentList.get(0).getId() + "-" + commentList.get(0).getI());
		}
	}
	
	public void multiRoundQ1(List<Comment> commentList) {
		extendqueryStr = queryStr;
		if(cntRound == 0) {
			textArea.append("機械人： " + commentList.get(0).getContent().trim() + "\n");
			logger.info("機械人： " + commentList.get(0).getContent());
			logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
			for (Comment c : commentList) {
				logger.info(c.getId() + "-" + c.getI() + "  " + c.getContent() + "  " + nf.format(c.getScore()));
			}
			reply.add(commentList.get(0).getId() + "-" + commentList.get(0).getI());
		}else {
			Iterator<Comment> i = commentList.iterator();
			while (i.hasNext()) {
				Comment c = i.next();
				for(String r : reply)
					if((c.getId() + "-" + c.getI()).equals(r))
						i.remove();
			}
			textArea.append("機械人： " + commentList.get(0).getContent().trim() + "\n");
			logger.info("機械人： " + commentList.get(0).getContent());
			logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
			for (Comment c : commentList) {
				logger.info(c.getId() + "-" + c.getI() + "  " + c.getContent() + "  " + nf.format(c.getScore()));
			}
			reply.add(commentList.get(0).getId() + "-" + commentList.get(0).getI());
		}
	}
	
	public void multiRoundQ1R1(List<Comment> commentList) {
		if(cntRound == 0) {
			textArea.append("機械人： " + commentList.get(0).getContent().trim() + "\n");
			logger.info("機械人： " + commentList.get(0).getContent());
			logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
			for (Comment c : commentList) {
				logger.info(c.getId() + "-" + c.getI() + "  " + c.getContent() + "  " + nf.format(c.getScore()));
			}
			reply.add(commentList.get(0).getId() + "-" + commentList.get(0).getI());
			extendqueryStr = queryStr + commentList.get(0).getContent().trim();
		}else {
			Iterator<Comment> i = commentList.iterator();
			while (i.hasNext()) {
				Comment c = i.next();
				for(String r : reply)
					if((c.getId() + "-" + c.getI()).equals(r))
						i.remove();
			}
			textArea.append("機械人： " + commentList.get(0).getContent().trim() + "\n");
			logger.info("機械人： " + commentList.get(0).getContent());
			logger.info("以下是相似度前 " + commentList.size() + " 高的回覆");
			for (Comment c : commentList) {
				logger.info(c.getId() + "-" + c.getI() + "  " + c.getContent() + "  " + nf.format(c.getScore()));
			}
			reply.add(commentList.get(0).getId() + "-" + commentList.get(0).getI());
			extendqueryStr = queryStr + commentList.get(0).getContent().trim();
		}
	}
	
	public void evaluation() throws IOException {
		System.out.println("Start to do evaluation.");
		BufferedReader br = null;
		FileReader fr = null;
		BufferedWriter bw = null;
		FileWriter fw = null;
		File file = new File("Evaluation Result");
		List<String> evaluationDataList = new ArrayList<String>();
		int cntL0 = 0;
		int cntL1 = 0;
		int cntL2 = 0;
		int cntNotMatch = 0;
		try {
			br = new BufferedReader(new FileReader("/Users/wongkaho/Eclipse Workspace/Chatbot/resources/evaluationData"));
			fr = new FileReader("/Users/wongkaho/Eclipse Workspace/Chatbot/resources/evaluationData");
			br = new BufferedReader(fr);
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				evaluationDataList.add(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		for(String queryStr : evaluationDataList) {
			try {
				fw = new FileWriter(file.getAbsoluteFile(), true);
				bw = new BufferedWriter(fw);
				Integer currentID = null;
				
				bw.write("你： " + queryStr);
				bw.newLine();


				String querystrAfterChineseTextSegmentation = seg.segmentation(queryStr);
				System.out.println(querystrAfterChineseTextSegmentation);

				Query query = new QueryParser("title", standardAnalyzer).parse(querystrAfterChineseTextSegmentation);

				int hitsPerPage = 10;
				IndexReader indexReader = DirectoryReader.open(directoryPost);
				IndexSearcher indexSearcher = new IndexSearcher(indexReader);
				indexSearcher.setSimilarity(new BM25Similarity((float)1.2, 1));
				ScoreDoc scoreDoc = new ScoreDoc(20, 200);
				TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(hitsPerPage, scoreDoc);
				indexSearcher.search(query, topScoreDocCollector);
				ScoreDoc[] hits = topScoreDocCollector.topDocs().scoreDocs;

				standardAnalyzer = new StandardAnalyzer();
				RAMDirectory directoryComment = new RAMDirectory();
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
				IndexWriter indexWriter = new IndexWriter(directoryComment, indexWriterConfig);
				List<Post> postList = new ArrayList<Post>();
				List<Comment> commentList = new ArrayList<Comment>();

				DecimalFormat nf = new DecimalFormat("#0.000000");

				HashMap commentHashMap = new HashMap();
				for(int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = indexSearcher.doc(docId);
					int shortID = Integer.valueOf(d.get("id")) / 10000;
					if(i == 0) {
						currentID = Integer.valueOf(d.get("id"));
					}
					JSONParser parserComment = new JSONParser();
					JSONArray comments = (JSONArray) parserComment.parse(new FileReader(RESOURCE + COMMENT + shortID + ".json"));
					for(Object objectComment : comments) {
						JSONObject comment = (JSONObject) objectComment;
						String id = (String) comment.get("id");
						JSONArray contents = (JSONArray) comment.get("contents");
						commentHashMap.put(id, contents);
					}
				}

				for(int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = indexSearcher.doc(docId);
					Post p = new Post();
					JSONArray contents = (JSONArray) commentHashMap.get(d.get("id"));
					for(Object o : contents) {
						JSONObject jo = (JSONObject) o;
						String content = jo.get("content").toString();
						String contentAfterSegmentation = jo.get("contentAfterSegmentation").toString();
						String I = jo.get("i").toString();
						if(Integer.valueOf(I) > 1) {
							addContent(indexWriter, contentAfterSegmentation, d.get("id"), I);
							Comment c = new Comment();
							c.setId(d.get("id"));
							c.setI(I);
							c.setContent(content.trim());
							c.setContentAfterSegmentation(contentAfterSegmentation);
							c.setScore(0);
							if(!c.getContent().isEmpty()) {
								commentList.add(c);
							}
						}
					}
					p.setId(d.get("id"));
					p.setTitle(d.get("title").replace(" | ", ""));
					p.setScore(hits[i].score * hits[i].score);
					postList.add(p);
				}
				
				indexWriter.close();
				indexReader.close();

				//calculate the result
				hitsPerPage = 100;
				IndexReader indexReaderComment = DirectoryReader.open(directoryComment);
				IndexSearcher indexSearcherComment = new IndexSearcher(indexReaderComment);
				indexSearcherComment.setSimilarity(new BM25Similarity());
				TopScoreDocCollector topScoreDocCollectorComment = TopScoreDocCollector.create(hitsPerPage);
				Query query2 = new QueryParser("title", standardAnalyzer).parse(querystrAfterChineseTextSegmentation);
				indexSearcherComment.search(query2, topScoreDocCollectorComment);
				hits = topScoreDocCollectorComment.topDocs().scoreDocs;
				for(Comment c : commentList) {
					float postScore = 0;

					for(int i = 0; i < hits.length; ++i) {
						int docId = hits[i].doc;
						Document d = indexSearcherComment.doc(docId);

						if(c.getId().equals(d.get("id")) && c.getI().equals(d.get("i"))) {
							float score = (hits[i].score * hits[i].score);
							c.setScore((postScore + score) / Integer.valueOf(c.getI()));
						}
						
						if(c.getContent().contains("�")) {
							c.setScore(0);
						}
					}
				}

				//sort the commentList
				Collections.sort(commentList, new Comparator<Comment>() {
					public int compare(Comment c1, Comment c2) {
						return Double.compare(c2.getScore(), c1.getScore());
					}
				});
				
				if(Integer.parseInt(commentList.get(0).getId()) != currentID) {
					if(commentList.get(0).getContent().trim().contains("L0(+0)")) {
						bw.write("機械人： " + commentList.get(0).getContent().trim().replace("L0(+0)", "Not match"));
					}else if(commentList.get(0).getContent().trim().contains("L1(+1)")){
						bw.write("機械人： " + commentList.get(0).getContent().trim().replace("L1(+2)", "Not match"));
					}else if(commentList.get(0).getContent().trim().contains("L2(+2)")){
						bw.write("機械人： " + commentList.get(0).getContent().trim().replace("L2(+2)", "Not match"));
					}
					cntNotMatch++;
				}else {
					bw.write("機械人： " + commentList.get(0).getContent().trim());
					if(commentList.get(0).getContent().trim().contains("L0(+0)")) {
						cntL0++;
					}else if(commentList.get(0).getContent().trim().contains("L1(+1)")){
						cntL1++;
					}else if(commentList.get(0).getContent().trim().contains("L2(+2)")){
						cntL2++;
					}	
				}
				bw.newLine();

				if(bw != null)
					bw.close();
				if(fw != null)
					fw.close();
			} catch (Exception e1) {
				logger.error(e1);
			}
		}
		System.out.println("Not Match: " + cntNotMatch + ", L0(+0): " + cntL0 + ", L1(+1): " + cntL1 + ", L2(+2): " + cntL2);
		System.out.println("Evaluation end.");
	}
}
