package org.squashtest.it.stub.messages;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

public class MessageSourceStub implements MessageSource {
	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return StringUtils.EMPTY;
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return StringUtils.EMPTY;
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return StringUtils.EMPTY;
	}
}
