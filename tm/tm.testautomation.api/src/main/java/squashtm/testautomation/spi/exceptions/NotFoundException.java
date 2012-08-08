package squashtm.testautomation.spi.exceptions;

public class NotFoundException extends TestAutomationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1185667228759223292L;

	
	public NotFoundException() {
		super();
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}

}
