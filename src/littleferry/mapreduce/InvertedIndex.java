package littleferry.mapreduce;

//: littleferry/mapreduce/InvertedIndex.java
import java.io.*;
import java.util.*;
import littleferry.share.Constant;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * @author chn, weiwei
 * InvertedIndex: webpages ----(inverted index)----> index files
 */
public class InvertedIndex {
	
	/**
	 * ReduceValue: fileName and frequency
	 */
	public static class ReduceValue {
		
		private long frequency;
		private String str;
		
		/**
		 * @return the frequency
		 */
		public long getFrequency() {
			return frequency;
		}
		
		/**
		 * @param frequency the frequency to set
		 */
		public void setFrequency(long frequency) {
			this.frequency = frequency;
		}
		
		/**
		 * @return the str
		 */
		public String getStr() {
			return str;
		}
		
		/**
		 * @param str the str to set
		 */
		public void setStr(String str) {
			this.str = str;
		}
		
	}
	
	static Comparator<ReduceValue> OrderIsdn =  new Comparator<ReduceValue>() {
		
		@Override
        public int compare(ReduceValue o1, ReduceValue o2) {
            long numbera = o1.getFrequency();
            long numberb = o2.getFrequency();
            if (numberb > numbera) {  
                return 1;  
            } else if (numberb < numbera) {  
                return -1;  
            } else {  
                return 0;  
            }          
        }
        
    };
	
	/**
	 * InvertedIndexOutputFormat: custom output format: <word>,<file>:<frequency>,...
	 */
	public static class InvertedIndexOutputFormat extends FileOutputFormat<Text, Text> {
		
		/**
		 * InvertedIndexRecordWriter: custom record writer for generating multiply index files and second index file
		 */
		protected static class InvertedIndexRecordWriter extends RecordWriter<Text, Text> {
			
			private int counter = 0;
			private FileSystem hdfs = null;
			private PrintWriter writer = null;
			private PrintWriter indexWriter = null;
			private String baseDir = null;
			private String baseFileName = null;
			
			public InvertedIndexRecordWriter(FileSystem hdfs, String baseDir, String baseFileName) {
				this.hdfs = hdfs;
				this.baseDir = baseDir;
				this.baseFileName = baseFileName;
			}
			
			@Override
			public void write(Text key, Text value) 
			throws IOException, InterruptedException {
				String keys = key.toString();
				final int index = keys.indexOf(":");
				final int label = Integer.parseInt(keys.substring(0, index));
				
				if (counter < label) {
					counter = label;
					if (writer != null) {
						writer.close();
						writer = null;
					}
					if (indexWriter == null) 
						indexWriter = new PrintWriter(hdfs.create(new Path(baseDir+baseFileName+"-index")));
					String fileName = baseFileName + "-" + counter;
					writer = new PrintWriter(hdfs.create(new Path(baseDir+fileName)));
					indexWriter.println(keys.substring(index+1)+":"+fileName);
				}
				
				writer.println(keys.substring(index+1)+value.toString());
			}
			
			@Override
			public void close(TaskAttemptContext context) 
			throws IOException, InterruptedException {
				if (writer != null) {
					writer.close();
					writer = null;
				}
				if (indexWriter != null) {
					indexWriter.close();
					indexWriter = null;
				}
			}
		}
		
		@Override
		public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext job) 
		throws IOException, InterruptedException {
			return new InvertedIndexRecordWriter(
				FileSystem.get(job.getConfiguration()), 
				FileOutputFormat.getOutputPath(job).toString()+"/", 
				job.getTaskAttemptID().getTaskID().toString().substring(5)
			);
		}
	}
	
	/**
	 * InvertedIndexMapper: custom mapper: {<offset>: "<line>"} -> {"<word>:<file>": "<frequency>"}
	 */
	public static class InvertedIndexMapper extends Mapper<Object, Text, Text, Text> {
		
		private Text outputKey = new Text();
		final private Text outputValue = new Text("1");
		
		@Override
		public void map(Object key, Text value, Context context) 
		throws IOException, InterruptedException {
			String values = value.toString();
			final int index = values.indexOf(":");
			
			for (String word: values.substring(index+1).split("\\s")) {
				if (!word.equals("")) {
					outputKey.set(word.toLowerCase()+":"+values.substring(0, index));
					context.write(outputKey, outputValue);
				}
			}
		}
	}
	
	/**
	 * InvertedIndexCombiner: custom combiner: {"<word>:<file>": list("<frequency>")} -> {"<word>": "<file>:<frequency>"}
	 */
	public static class InvertedIndexCombiner extends Reducer<Text, Text, Text, Text> {
		
		private Text outputKey = new Text();
		private Text outputValue = new Text();
		
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) 
		throws IOException, InterruptedException {
			long sum = 0;
			for (Text value: values) {
				sum += Long.parseLong(value.toString());
			}
			
			final String keys = key.toString();
			final int index = keys.lastIndexOf(":");
			outputKey.set(keys.substring(0, index));
			outputValue.set(keys.substring(index+1)+":"+sum);
			context.write(outputKey, outputValue);
		}
	}
	
	/**
	 * InvertedIndexReducer: custom reducer: {"<word>": list("<file>:<frequency>")} -> {"<word>": "<file>:<frequency>,..."}
	 */
	public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
		
		private static final int MAXIMUM = 100;
		
		private int counter = 0;
		private int label = 1;
		private Text outputKey = new Text();
		private Text outputValue = new Text();
		
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) 
		throws IOException, InterruptedException {
			StringBuffer lists = new StringBuffer();
			PriorityQueue<ReduceValue> queue =  new PriorityQueue<ReduceValue>(11, OrderIsdn);
			for (Text value: values) {
				String[] str = value.toString().split(":");
				ReduceValue rv = new ReduceValue();
				rv.setStr(str[0]);
				rv.setFrequency(Long.parseLong(str[1]));
				queue.add(rv);
			}
			while (!queue.isEmpty())
			{
				ReduceValue reva = queue.poll();
				lists.append(","+reva.getStr()+":"+reva.getFrequency());
			}
			
			if (++counter > MAXIMUM) {
				counter = 1;
				++label;
			}
				
			outputKey.set(label+":"+key);
			outputValue.set(lists.toString());
			context.write(outputKey, outputValue);
		}
	}

	/**
	 * mapreduce initialization and execution for inverted index
	 * @param args [directory saves webpages' content, directory saves index files]
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) 
	throws IOException, InterruptedException, ClassNotFoundException {
		args = new String[] {Constant.BASE_TXT_DIR, Constant.BASE_INDEX_DIR};
		Configuration configuration = new Configuration();
		configuration.addResource(new Path(Constant.CORE_SITE_XML));
		configuration.addResource(new Path(Constant.HDFS_SITE_XML));
		configuration.addResource(new Path(Constant.MAPRED_SITE_XML));
		String[] command = new GenericOptionsParser(configuration, args).getRemainingArgs();
		
		Job job = new Job(configuration, "inverted index");
		
		job.setJarByClass(InvertedIndex.class);
		
		job.setMapperClass(InvertedIndexMapper.class);
		//job.setMapOutputKeyClass(Text.class);
		//job.setMapOutputValueClass(Text.class);
		
		job.setCombinerClass(InvertedIndexCombiner.class);
		
		job.setReducerClass(InvertedIndexReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(InvertedIndexOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(command[0]));
		FileOutputFormat.setOutputPath(job, new Path(command[1]));
		
		System.out.println("inverted index begins");
		if (job.waitForCompletion(true)) {
			System.out.println("inverted index ends");
		} else {
			System.err.println("inverted index failed");
		}
	}

}
///:~