package io.thingshub.transport.codec;

public class EncodingException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public EncodingException() {
	}

	public EncodingException(String message) {
		super(message);
	}

	public EncodingException(String message, Throwable cause) {
		super(message, cause);
	}

	public EncodingException(Throwable cause) {
		super(cause);
	}

	public EncodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}