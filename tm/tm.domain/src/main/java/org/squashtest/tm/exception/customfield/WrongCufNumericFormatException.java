package org.squashtest.tm.exception.customfield;

import org.squashtest.tm.core.foundation.exception.ActionException;

/**
 * Created by jthebault on 20/07/2016.
 */
public class WrongCufNumericFormatException extends ActionException {
	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;

	private static final String WRONG_NUMERIC_CUF_VALUE_KEY = "message.cuf.value.wrong.numeric.value";

	public WrongCufNumericFormatException() {
		super();
	}

	public WrongCufNumericFormatException(Exception cause) {
		super(cause);
	}

	@Override
	public String getI18nKey() {
		return WRONG_NUMERIC_CUF_VALUE_KEY;
	}

}
