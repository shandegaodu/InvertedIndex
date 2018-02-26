package littleferry.crawl;

//: littleferry/crawl/BaiduPostCrawler.java
import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import littleferry.share.*;

/**
 * @author chn
 * BaiduPostCrawler: a monitor thread for monitoring the crawling process
 */
public class BaiduPostCrawler extends Thread {
	
	private final int MAX_POST_NUM = 10000;
	private final String BASE_URL = "http://tieba.baidu.com/p/";
	
	private int counter, label;
	private int crawlerNumber, handlerNumber;
	private long startIndex, endIndex;
	private StringBuffer buffer;
	private ConcurrentLinkedQueue<Long> indexQueue;
	private ConcurrentLinkedQueue<BaiduPost> postQueue;
	
	/**
	 * CrawlerThread: thread for crawling text information
	 */
	private class CrawlerThread extends Thread {
		
		private volatile boolean flag;
		private Long index;
		
		/**
		 * crawl a baidu post
		 * @throws IOException
		 * @throws CustomException
		 */
		private void crawl() throws IOException, CustomException {
			long label = index.longValue();
			if (label % 100 == 0) System.out.println(getName()+": "+label);
			BaiduPost post = new BaiduPost(BASE_URL, label);
			post.parse();
			offerPost(post);
		}
		
		public CrawlerThread(int label) {
			super("crawler "+label);
			
			flag = true;
			start();
		}
		
		@Override
		public void run() {
			while (flag) {
				index = pollIndex();
				try {
					if (index != null) {
						crawl();
					} else {
						sleep(1000);
					}
				} catch (CustomException ce) {
					//System.out.println(ce.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * thread will stop
		 */
		public void terminate() {
			this.flag = false;
		}
	}
	
	/**
	 * HandlerThread: thread for saving text information
	 */
	private class HandlerThread extends Thread {
		
		private volatile boolean flag;
		private BaiduPost post;
		
		/**
		 * save text information into 2 files
		 * @throws IOException
		 */
		private void write() throws IOException {
			long label = post.getIndex();
			if (label % 100 == 0) System.out.println(getName()+": "+label);
			BaiduPostXMLWriter xmlWriter = new BaiduPostXMLWriter(post);
			xmlWriter.write();
			ChineseApart seperator = new ChineseApart(post);
			writeString(seperator.apart());
		}
		
		public HandlerThread(int label) {
			super("handler "+label);
			
			flag = true;
			start();
		}
		
		@Override
		public void run() {
			while (flag) {
				post = pollPost();
				try {
					if (post != null) {
						write();
					} else {
						sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * thread will stop
		 */
		public void terminate() {
			this.flag = false;
		}
	}
	
	/**
	 * pop an index of a post
	 * @return an index
	 */
	private synchronized Long pollIndex() {
		if (!indexQueue.isEmpty()) {
			return indexQueue.poll();
		} else {
			return null;
		}
	}
	
	/**
	 * push an index of a post
	 * @param index an index
	 */
	private synchronized void offerIndex(Long index) {
		indexQueue.offer(index);
	}
	
	/**
	 * pop a baidu post instance
	 * @return a post instance
	 */
	private synchronized BaiduPost pollPost() {
		if (!postQueue.isEmpty()) {
			return postQueue.poll();
		} else {
			return null;
		}
	}
	
	/**
	 * push a baidu post instance
	 * @param post a post instance
	 */
	private synchronized void offerPost(BaiduPost post) {
		postQueue.offer(post);
	}
	
	/**
	 * write content into file
	 * @param content content to write
	 * @throws IOException
	 */
	private synchronized void writeString(String content) throws IOException {
		if (++counter > MAX_POST_NUM) {
			LittleFerryFileSystem.writeUTF(Constant.BASE_TXT_DIR+label+".txt", buffer.toString());
			buffer.setLength(0);
			counter = 1;
			++label;
		}
		
		buffer.append(content);
	}
	
	public BaiduPostCrawler(int crawlerNumber, int handlerNumber, long startIndex, long endIndex) {
		this.crawlerNumber = crawlerNumber;
		this.handlerNumber = handlerNumber;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		counter = 0;
		label = 1;
		buffer = new StringBuffer();
		indexQueue = new ConcurrentLinkedQueue<Long>();
		postQueue = new ConcurrentLinkedQueue<BaiduPost>();
	}
	
	@Override
	public void run() {
		System.out.println("monitor: initializing...");
		int i = 0;
		CrawlerThread[] crawlers = new CrawlerThread[crawlerNumber];
		HandlerThread[] handlers = new HandlerThread[handlerNumber];
		for (i = 0; i < crawlerNumber; ++i) {
			crawlers[i] = new CrawlerThread(i+1);
		}
		for (i = 0; i < handlerNumber; ++i) {
			handlers[i] = new HandlerThread(i+1);
		}
		
		System.out.println("monitor: processing...");
		for (long index = startIndex; index <= endIndex; ++index) {
			offerIndex(Long.valueOf(index));
		}
		
		try {
			System.out.println("monitor: waiting for crawlers...");
			while (!indexQueue.isEmpty()) sleep(500);
			
			for (i = 0; i < crawlerNumber; ++i) {
				crawlers[i].terminate();
			}
			for (i = 0; i < crawlerNumber; ++i) {
				crawlers[i].join();
			}
			
			System.out.println("monitor: waiting for handlers...");
			while (!postQueue.isEmpty()) sleep(500);
			
			for (i = 0; i < handlerNumber; ++i) {
				handlers[i].terminate();
			}
			for (i = 0; i < handlerNumber; ++i) {
				handlers[i].join();
			}
			
			LittleFerryFileSystem.writeUTF(Constant.BASE_TXT_DIR+label+".txt", buffer.toString());
			buffer.setLength(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("monitor: stop");
	}
	
	/**
	 * for test and debugging
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("for test and debugging");
		LittleFerryFileSystem.initialize();
		BaiduPostCrawler crawler = new BaiduPostCrawler(2, 2, 4163000000L, 4163009999L);
		crawler.start();
		crawler.join();
	}

}
///:~