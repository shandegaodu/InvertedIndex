package littleferry.search;

//: littleferry/search/BaiduPostXMLReader.java
import java.io.*;
import littleferry.share.*;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

/**
 * @author chn
 * BaiduPostXMLReader: xml file reader
 */
public class BaiduPostXMLReader {
	
	private Document document;
	
	public BaiduPostXMLReader(InputStream in) {
		SAXReader reader = new SAXReader();
		reader.setEncoding("utf-8");
		try {
			document = reader.read(in);
		} catch (DocumentException de) {
			de.printStackTrace();
		}
	}
	
	/**
	 * get a baidu post's text information from xml file
	 * @return a post
	 */
	public BaiduPost getPost() {
		BaiduPost post = new BaiduPost();
		
		Element root = document.getRootElement();
		String url = root.element("url").getText();
		post.setIndex(Long.parseLong(url.substring(url.lastIndexOf("/")+1)));
		post.setUrl(url);
		post.setBar(root.element("bar").getText());
		post.setTitle(root.element("title").getText());
		post.setReplies(Integer.parseInt(root.element("replies").getText()));
		for (Element note: root.elements("note")) {
			post.addNote(note.element("author").getText(), note.element("text").getText());
		}
		
		return post;
	}

}
///:~