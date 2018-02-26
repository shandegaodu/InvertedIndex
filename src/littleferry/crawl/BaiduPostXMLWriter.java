package littleferry.crawl;

//: littleferry/crawl/BaiduPostXMLWriter.java
import java.io.*;
import littleferry.share.*;
import org.dom4j.*;

/**
 * BaiduPostXMLWriter: xml file writer
 * @author chn
 */
public class BaiduPostXMLWriter {
	
	private long index;
	private Document document;
	
	/**
	 * transform BaiduPost to xml
	 * @param post a baidu post
	 */
	private void transform(BaiduPost post) {
		Element root = document.addElement("post");
		root.addElement("url").addText(post.getUrl());
		root.addElement("bar").addText(post.getBar());
		root.addElement("title").addText(post.getTitle());
		root.addElement("replies").addText(String.valueOf(post.getReplies()));
		for (BaiduNote note: post.getNotes()) {
			Element entry = root.addElement("note");
			entry.addElement("author").addText(note.getAuthor());
			entry.addElement("text").addText(note.getText());
		}
	}
	
	public BaiduPostXMLWriter(BaiduPost post) {
		document = DocumentHelper.createDocument();
		document.setXMLEncoding("utf-8");
		index = post.getIndex();
		transform(post);
	}
	
	/**
	 * save a baidu post's text information into xml file
	 * @throws IOException
	 */
	public void write() throws IOException {
		LittleFerryFileSystem.writeUTF(
			Constant.BASE_XML_DIR+index+".xml", 
			document.asXML()
		);
	}
	
}
///:~