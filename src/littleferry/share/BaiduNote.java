package littleferry.share;

//: littleferry/share/BaiduNote.java

/**
 * BaiduNote: class for saving the main text information of a note
 */
public class BaiduNote {
	
	private String author;
	private String text;
	
	public BaiduNote() {}
	
	public BaiduNote(String author, String text) {
		this.author = author;
		this.text = text;
	}
	
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
}
///:~