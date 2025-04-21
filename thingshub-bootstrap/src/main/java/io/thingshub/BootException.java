package io.thingshub;

public class BootException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BootException(String cause, Throwable e) {
		super(cause, e);
	}

	public BootException(String cause) {
		super("cause");
	}
}
