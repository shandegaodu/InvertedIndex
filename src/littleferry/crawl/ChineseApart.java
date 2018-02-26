package littleferry.crawl;

//: littleferry/crawl/ChineseApart.java
import java.io.*;
import littleferry.share.*;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * @author  weiwei
 * ChineseApart: class for taking Chinese apart
 */
public class ChineseApart {
	
	private BaiduPost post;
	
	public ChineseApart(BaiduPost post){
		this.post = post;
	}

	/**
	 * Chinese apart
	 * @throws IOException
	 */
	public String apart() throws IOException {
		String prefix = post.getIndex() + ":";
		StringBuffer content = new StringBuffer();
		
		StringReader reader = new StringReader(post.getTitle());
		IKSegmenter ik = new IKSegmenter(reader, true); // 当为true时，分词器进行最大词长切分
		Lexeme lexeme = null;
		
		content.append(prefix);
		while ((lexeme = ik.next()) != null) {
			content.append(" ");
			content.append(lexeme.getLexemeText());
		}
		content.append("\n");
		
		for (BaiduNote note: post.getNotes()) {
			reader = new StringReader(note.getText());
			ik = new IKSegmenter(reader, true); // 当为true时，分词器进行最大词长切分
			lexeme = null;
			
			content.append(prefix);
			while ((lexeme = ik.next()) != null) {
				content.append(" ");
				content.append(lexeme.getLexemeText());
			}
			content.append("\n");
		}
		
		return content.toString();
	}
	
}
///:~