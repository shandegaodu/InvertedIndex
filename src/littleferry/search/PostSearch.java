package littleferry.search;

//: littleferry/search/PostSearch.java
import java.io.*;
import java.util.*;
import littleferry.share.*;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * @author chn
 * PostSearch: api for search engine
 */
public class PostSearch {
	
	private TreeMap<String, String> indices;
	
	/**
	 * initialize
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		indices = new TreeMap<String, String>();
		
		for (String path: LittleFerryFileSystem.findFiles(Constant.BASE_INDEX_DIR, "index")) {
			BufferedReader reader = LittleFerryFileSystem.getFileReader(path);
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] pair = line.split(":");
				indices.put(pair[0], pair[1]);
			}
			
			reader.close();
		}
	}
	
	public PostSearch() throws IOException {
		initialize();
	}
	
	/**
	 * get the inverted index's value of a single keyword
	 * @param keyword a keyword
	 * @return inverted index's value
	 * @throws IOException
	 */
	public String singleSearch(String keyword) throws IOException {
		if (keyword != null) {
			Map.Entry<String, String> pair = indices.floorEntry(keyword);
			if (pair != null) {
				BufferedReader reader = LittleFerryFileSystem.getFileReader(Constant.BASE_INDEX_DIR+pair.getValue());
				
				int index = 0;
				String line = null, result = null;
				while ((line = reader.readLine()) != null) {
					index = line.indexOf(",");
					if (line.substring(0, index).equals(keyword)) {
						result = line.substring(index+1);
						break;
					}
				}
				
				reader.close();
				
				return result;
			}
		}
		
		return null;
	}
	
	/**
	 * get the inverted index's value of some keywords
	 * @param keyword a string contains some keywords
	 * @return sum of inverted index's value
	 * @throws IOException
	 */
	public LongPair[] search(String keyword) throws IOException {
		if (keyword != null) {
			StringReader reader = new StringReader(keyword);
			IKSegmenter segmenter = new IKSegmenter(reader, true);
			Lexeme lexeme = null;
			HashMap<Long, Long> hashMap = new HashMap<Long, Long>();
			
			while ((lexeme = segmenter.next()) != null) {
				String values = singleSearch(lexeme.getLexemeText());
				if (values != null) {
					String[] pairs = values.split(",");
					for (String pair: pairs) {
						final int index = pair.indexOf(":");
						Long key = new Long(pair.substring(0, index));
						Long value = hashMap.get(key);
						if (value == null) value = new Long(pair.substring(index+1));
						else value += Long.parseLong(pair.substring(index+1));
						hashMap.put(key, value);
					}
				}
			}
			
			if (!hashMap.isEmpty()) {
				PriorityQueue<LongPair> queue = new PriorityQueue<LongPair>();
				
				for (Map.Entry<Long, Long> pair: hashMap.entrySet()) {
					queue.offer(new LongPair(pair.getValue(), pair.getKey()));
				}
				
				if (!queue.isEmpty()) {
					final int length = queue.size();
					LongPair[] array = new LongPair[length];
					
					for (int i = 0; i < length; ++i) {
						array[i] = queue.poll();
					}
					
					return array;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * get a baidu post according to its index
	 * @param index the index of a baidu post
	 * @return a baidu post
	 * @throws IOException
	 */
	public BaiduPost getBaiduPost(long index) throws IOException {
		return new BaiduPostXMLReader(
			LittleFerryFileSystem.getInputStream(
				Constant.BASE_XML_DIR+index+".xml"
		)).getPost();
	}

	/**
	 * for test and debugging
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("for test and debugging");
		LittleFerryFileSystem.initialize();
		PostSearch search = new PostSearch();
		System.out.println(search.singleSearch("转身"));
		System.out.println(search.singleSearch("搭配"));
		System.out.println(search.singleSearch("地狱"));
		System.out.println(search.singleSearch("人么"));
	}

}
///:~