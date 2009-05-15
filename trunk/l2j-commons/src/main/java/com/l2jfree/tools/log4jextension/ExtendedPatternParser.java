package com.l2jfree.tools.log4jextension;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Overload PatternParser class
 */
public class ExtendedPatternParser extends PatternParser
{
	private static final char STACKTRACE_CHAR = 's';

	/**
	 * Constructor with a specific pattern
	 * 
	 * @param _pattern
	 *            the pattern
	 */
	public ExtendedPatternParser(String _pattern)
	{
		super(_pattern);
	}

	/**
	 * @see org.apache.log4j.helpers.PatternParser#finalizeConverter(char)
	 */
	@Override
	public void finalizeConverter(char formatChar)
	{
		PatternConverter pc = null;
		switch (formatChar)
		{
		case STACKTRACE_CHAR:
			pc = new ThrowablePatternConverter(formattingInfo);
			currentLiteral.setLength(0);
			addConverter(pc);
			break;
		default:
			super.finalizeConverter(formatChar);
		}
	}

	private class ThrowablePatternConverter extends PatternConverter
	{
		ThrowablePatternConverter(FormattingInfo _formattingInfo)
		{
			super(_formattingInfo);
		}

		/**
		 * @see org.apache.log4j.helpers.PatternConverter#convert(org.apache.log4j.spi.LoggingEvent)
		 */
		@Override
		public String convert(LoggingEvent event)
		{
			String sbReturn;
			try 
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw, true);
				event.getThrowableInformation().getThrowable().printStackTrace(pw);
				return sw.toString();
			}
			catch (NullPointerException ex)
			{
				sbReturn = ""; // //$NON-NLS-1$
			}
			return sbReturn;
		}
	}
}
