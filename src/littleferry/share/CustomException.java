package littleferry.share;

//: littleferry/share/CustomException.java

/**
 * @author chn
 * CustomException: custom exception
 */
public class CustomException extends Exception {
	
	private static final long serialVersionUID = 987654321;
	
	public CustomException(String message) {
		super(message);
	}
	
}
///:~