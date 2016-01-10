package editor;

public class TagParserException extends RuntimeException {
	private static final long serialVersionUID = -6420546245130220088L;

	public TagParserException() {
		super();
	}

	public TagParserException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TagParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public TagParserException(String message) {
		super(message);
	}

	public TagParserException(Throwable cause) {
		super(cause);
	}


}
