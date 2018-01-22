import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Word;

public class mmseg4j {
	
	protected Dictionary dic;  
    
	public mmseg4j() {  
		//set the dictionary path
		System.setProperty("mmseg.dic.path", "data");   
		dic = Dictionary.getInstance();
	}  
	
	public String segmentation(String sentence, HashSet<String> stopwords) throws IOException {
		Reader reader = new StringReader(sentence);  
	    StringBuilder stringBuilder = new StringBuilder();  
	    ComplexSeg seg = new ComplexSeg(dic);  
	    MMSeg mmSeg = new MMSeg(reader, seg);  
	    Word word = null;  
	    boolean first = true;  
	    while((word = mmSeg.next()) != null) {  
//	        if(stopwords.contains(word.getString())) {
//	        		break;
//	        }
	        if(!first) {  
        			stringBuilder.append(" ");  
	        }
	        stringBuilder.append(word.getString());
	        first = false;
	    }  
	    return stringBuilder.toString();  
	} 
}
