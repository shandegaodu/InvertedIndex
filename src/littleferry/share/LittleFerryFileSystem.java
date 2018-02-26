package littleferry.share;

//: littleferry/share/LittleFerryFileSystem.java
import java.io.*;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

/**
 * @author chn
 * LittleFerryFileSystem: a file system for project littleferry
 */
public class LittleFerryFileSystem {
	
	private static FileSystem hdfs = null;
	
	/**
	 * initialize
	 * @throws IOException
	 */
	public static void initialize() throws IOException {
		Configuration configuration = new Configuration();
		configuration.addResource(new Path(Constant.CORE_SITE_XML));
		configuration.addResource(new Path(Constant.HDFS_SITE_XML));
		configuration.addResource(new Path(Constant.MAPRED_SITE_XML));
		hdfs = FileSystem.get(configuration);
		Path path = null;
		
		for (String directory: Constant.BASE_DIRS) {
			path = new Path(directory);
			if (!hdfs.exists(path)) hdfs.mkdirs(path);
		}
	}
	
	/**
	 * write content into file whose name is filename
	 * @param fileName file's name
	 * @param content content to write in file
	 * @throws IOException
	 */
	public static void writeUTF(String fileName, String content) throws IOException {
		Path path = new Path(fileName);
		FSDataOutputStream fout = null;
		synchronized (hdfs) {
			fout = hdfs.create(path);
		}
		//fout.writeUTF(content);
		fout.write(content.getBytes());
		fout.flush();
		fout.close();
	}
	
	/**
	 * get input stream of given file
	 * @param fileName file's full name
	 * @return input stream of file
	 * @throws IOException
	 */
	public static InputStream getInputStream(String fileName) throws IOException {
		return hdfs.open(new Path(fileName));
	}
	
	/**
	 * get output stream of given file
	 * @param fileName file's full name
	 * @param overwrite whether overwrite the file
	 * @return output stream of file
	 * @throws IOException
	 */
	public static OutputStream getOutputStream(String fileName, boolean overwrite) throws IOException {
		return hdfs.create(new Path(fileName), overwrite);
	}
	
	/**
	 * get a buffered reader of given file
	 * @param fileName file's full name
	 * @return a buffered reader
	 * @throws IOException
	 */
	public static BufferedReader getFileReader(String fileName) throws IOException {
		return new BufferedReader(new InputStreamReader(hdfs.open(new Path(fileName))));
	}
	
	/**
	 * get all files whose name contain keyword in a given directory
	 * @param path directory to search
	 * @param keyword keyword
	 * @return all files whose name contain keyword
	 * @throws IOException
	 */
	public static ArrayList<String> findFiles(String path, String keyword) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		
		FileStatus[] files = hdfs.listStatus(new Path(path));
		for (FileStatus file: files) {
			final Path filePath = file.getPath();
			if (!file.isDir() && filePath.getName().contains(keyword)) {
				result.add(filePath.toString());
			}
		}
		
		return result;
	}

}
///:~