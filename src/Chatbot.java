import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.apache.lucene.store.RAMDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Chatbot {
	public static void main (String [] args) throws IOException, ParseException, Exception {
		
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
		Directory directory = new RAMDirectory();	
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		String queryStr = "";
		
		JSONParser parserPost = new JSONParser();
        JSONArray posts = (JSONArray) parserPost.parse(new FileReader("postAfterChineseTextSegmentation.json"));
        for(Object object : posts) {
        		JSONObject post = (JSONObject) object;
        		String title = (String) post.get("title");
        		String id = (String) post.get("id");
        		addContent(indexWriter, title, id);
        }
        indexWriter.close();
        
        JSONParser parserComment = new JSONParser();
        JSONArray comments = (JSONArray) parserComment.parse(new FileReader("comment.json"));
        HashMap commentHashMap = new HashMap();
        for(Object object : comments) {
	    		JSONObject comment = (JSONObject) object;
	    		String id = (String) comment.get("id");
	    		JSONArray content = (JSONArray) comment.get("content");
	    		commentHashMap.put(id, content.get(0));
	    }
        
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
			IndexReader indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new BM25Similarity());
			TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(hitsPerPage);
			indexSearcher.search(query, topScoreDocCollector);
			ScoreDoc[] hits = topScoreDocCollector.topDocs().scoreDocs;
			
			System.out.println("以下是相似度前 " + hits.length + " 高的標題");
			DecimalFormat nf = new DecimalFormat("##.000000");
			for(int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = indexSearcher.doc(docId);
//				System.out.println(d.get("title").replace(" | ", "") + " \t" + nf.format(hits[i].score));
				System.out.println(commentHashMap.get(d.get("id")) + " \t" + nf.format(hits[i].score));
			}

			indexReader.close();
        }
	}
	
	private static void addContent(IndexWriter indexWriter, String title, String id) throws IOException {
		Document document = new Document();
		document.add(new TextField("title", title, Field.Store.YES));
		document.add(new StringField("id", id, Field.Store.YES));
		indexWriter.addDocument(document);
	}
}
