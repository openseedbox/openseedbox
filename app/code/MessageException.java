package code;

/**
 *
 * @author erin
 */
public class MessageException extends Exception {
	public MessageException(String message) {
		super(message);
	}
	
	public MessageException(String message, Object... args) {
		super(String.format(message, args));
	}
}
