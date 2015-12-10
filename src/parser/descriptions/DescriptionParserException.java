package parser.descriptions;

public class DescriptionParserException extends RuntimeException {
	private static final long serialVersionUID = -6420546245130220088L;

	public DescriptionParserException() {
		super();
	}

	public DescriptionParserException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DescriptionParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public DescriptionParserException(String message) {
		super(message);
	}

	public DescriptionParserException(Throwable cause) {
		super(cause);
	}


}
