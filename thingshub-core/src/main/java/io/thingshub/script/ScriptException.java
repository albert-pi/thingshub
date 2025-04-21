package io.thingshub.script;

public class ScriptException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ScriptException(String message) {
		super(message);
	}

	public ScriptException(String message, Throwable t) {
		super(message, t);
	}
}
