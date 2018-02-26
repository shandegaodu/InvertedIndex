package littleferry.share;

//: littleferry/share/BaiduPost.java
import java.io.IOException;
import java.util.LinkedList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

/**
 * @author chn
 * BaiduPost: class for crawling the main text information of a baidu post
 */
public class BaiduPost {
	
	private int replies;
	private long index;
	private String bar;
	private String title;
	private String url;
	private LinkedList<BaiduNote> notes;
	
	/**
	 * get all notes in a page of a post
	 * @param document html document
	 * @throws CustomException
	 */
	private void getNotes(Document document) throws CustomException {
		Elements divs = document.select("#j_p_postlist div.l_post ");
		for (Element div: divs) {
			BaiduNote note = new BaiduNote();
			Elements as = div.select("ul.p_author li.d_name a.p_author_name");
			if (!as.isEmpty()) {
				note.setAuthor(as.get(0).text());
				Elements ccs = div.select("cc div");
				if (!ccs.isEmpty()) {
					Element cc = ccs.get(0);
					Elements voicePlayers = cc.select("div.voice_player");
					if (!voicePlayers.isEmpty()) {
						for (Element voicePlayer: voicePlayers) {
							voicePlayer.remove();
						}
					}
					note.setText(cc.text());
					notes.add(note);
				} else {
					throw new CustomException("info: parsing error when getting 'text'");
				}
			} else {
				throw new CustomException("info: parsing error when getting 'author'");
			}
		}
	}
	
	public BaiduPost() {
		notes = new LinkedList<BaiduNote>();
	}
	
	public BaiduPost(String baseUrl, long index) {
		this.index = index;
		this.url = baseUrl + index;
		notes = new LinkedList<BaiduNote>();
	}
	
	/**
	 * @return the replies
	 */
	public int getReplies() {
		return replies;
	}

	/**
	 * @param replies the replies to set
	 */
	public void setReplies(int replies) {
		this.replies = replies;
	}
	
	/**
	 * @return the index
	 */
	public long getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(long index) {
		this.index = index;
	}
	
	/**
	 * @return the bar
	 */
	public String getBar() {
		return bar;
	}

	/**
	 * @param bar the bar to set
	 */
	public void setBar(String bar) {
		this.bar = bar;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @return the notes
	 */
	public LinkedList<BaiduNote> getNotes() {
		return notes;
	}
	
	/**
	 * @param author the author to add
	 * @param text the text to add
	 */
	public void addNote(String author, String text) {
		notes.add(new BaiduNote(author, text));
	}
	
	/**
	 * crawl the main text information of a post
	 * @throws IOException
	 * @throws CustomException
	 */
	public void parse() throws IOException, CustomException {
		Document document = Jsoup.connect(url).get();
		Elements header = document.getElementsByClass("core_title_txt");
		
		if (!header.isEmpty()) {
			title = header.get(0).text();
			
			Elements as = document.select("a.card_title_fname");
			if (!as.isEmpty()) {
				bar = as.get(0).text();
				
				Elements spans = document.select("#thread_theme_5 li.l_reply_num span.red");
				if (!spans.isEmpty() && spans.size() == 2) {
					replies = Integer.parseInt(spans.get(0).text());
					int pages = Integer.parseInt(spans.get(1).text());
					
					getNotes(document);
					if (pages > 1) {
						for (int i = 2; i <= pages; ++i) {
							getNotes(Jsoup.connect(url+"?pn="+i).get());
						}
					}
				} else {
					throw new CustomException("info: parsing error when getting 'replies' and 'pages'");
				}
			} else {
				throw new CustomException("info: parsing error when getting 'bar'");
			}
		} else {
			throw new CustomException("info: 404 not found");
		}
	}

}
///:~