package com.l2jfree.loginserver.util.logging;

import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

import com.l2jfree.util.logging.L2LogFormatter;

public abstract class L2LoginLogFormatter extends L2LogFormatter
{
	@Override
	protected void format0(LogRecord record, TextBuilder tb)
	{
		appendDate(record, tb);
		appendMessage(record, tb);
		appendParameters(record, tb, ", ", true);
	}
}
