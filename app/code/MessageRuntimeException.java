package code;

public class MessageRuntimeException extends RuntimeException {

	public MessageRuntimeException(MessageException ex) {
		super(ex);
	}
	
}
