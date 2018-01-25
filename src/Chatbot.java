import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

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

public class Chatbot {
	
	static final String INDEX_DIRECTORY_POST = "/Users/wongkaho/Eclipse Workspace/Chatbot/INDEX_DIRECTORY_POST";
	static final String INDEX_DIRECTORY_COMMENT = "/Users/wongkaho/Eclipse Workspace/Chatbot/INDEX_DIRECTORY_COMMENT";
	
	public static void main (String [] args) throws IOException, ParseException, Exception {
		System.out.println("正在啟動自動回答機械人");
		
		StandardAnalyzer standardAnalyzer;
		Directory directoryPost, directoryComment;
		IndexWriterConfig indexWriterConfig;
		IndexWriter indexWriter;
				
		standardAnalyzer = new StandardAnalyzer();
		directoryPost = new RAMDirectory();
//		directoryPost = FSDirectory.open(Paths.get(INDEX_DIRECTORY_POST));
		indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
		indexWriter = new IndexWriter(directoryPost, indexWriterConfig);
		String queryStr = "";
		
		System.out.println("載入 562074 個標題中...");
		JSONParser parserPost = new JSONParser();
        JSONArray posts = (JSONArray) parserPost.parse(new FileReader("postAfterChineseTextSegmentation.json"));
        for(Object object : posts) {
        		JSONObject post = (JSONObject) object;
        		String title = (String) post.get("title");
        		String id = (String) post.get("id");
        		addContent(indexWriter, title, id);
        }
        indexWriter.close();
        System.out.println(posts.size() + " 個標題已完成載入");
        
        System.out.println("載入 2727863 個回覆中...");
        JSONParser parserComment = new JSONParser();
        JSONArray comments = (JSONArray) parserComment.parse(new FileReader("commentAfterChineseTextSegmentation.json"));
        HashMap commentHashMap = new HashMap();
        for(Object objectComment : comments) {
	    		JSONObject comment = (JSONObject) objectComment;
	    		String id = (String) comment.get("id");
	    		JSONArray contents = (JSONArray) comment.get("content");
	    		commentHashMap.put(id, contents);
	    }
        System.out.println(comments.size() + " 個回覆已完成載入");
        
        //read Chinese stop words
        Scanner stopwordScanner = new Scanner(new File("chinese_sw.txt"));
        HashSet<String> stopwords = new HashSet<String>();
        while (stopwordScanner.hasNext()){
        	stopwords.add(stopwordScanner.next());
        }
        stopwordScanner.close();

        mmseg4j seg = new mmseg4j();
        
        while (true) {
	        System.out.print("我係自動回答機械人，隨便說吧: ");
	        Scanner scanner = new Scanner(System.in);
	        queryStr = scanner.nextLine();
	        
	        String querystrAfterChineseTextSegmentation = seg.segmentation(queryStr, stopwords);
	        
	        Query query = new QueryParser("title", standardAnalyzer).parse(querystrAfterChineseTextSegmentation);
	        
	        int hitsPerPage = 10;
			IndexReader indexReader = DirectoryReader.open(directoryPost);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new BM25Similarity());
			TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(hitsPerPage);
			indexSearcher.search(query, topScoreDocCollector);
			ScoreDoc[] hits = topScoreDocCollector.topDocs().scoreDocs;
			
			standardAnalyzer = new StandardAnalyzer();
//	        directoryComment = FSDirectory.open(Paths.get(INDEX_DIRECTORY_COMMENT));
			directoryComment = new RAMDirectory();
			indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
			indexWriter = new IndexWriter(directoryComment, indexWriterConfig);
			
			System.out.println("以下是相似度前 " + hits.length + " 高的標題");
			DecimalFormat nf = new DecimalFormat("#0.000000");
			for(int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = indexSearcher.doc(docId);
		        
				Object comment = commentHashMap.get(d.get("id"));
				JSONArray contents = (JSONArray) comment;
				for(int j = 1; j < contents.size(); j++) {
					addContent(indexWriter, contents.get(j).toString(), d.get("id"), String.valueOf(j+1));
				}
				
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
			System.out.println("以下是相似度前 " + hits.length + " 高的回覆");
			for(int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = indexSearcherComment.doc(docId);
				System.out.println(d.get("id") + "-" + d.get("i") + "\t " + d.get("title").replace(" | ", "") + " \t" + nf.format(hits[i].score));
			}
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
