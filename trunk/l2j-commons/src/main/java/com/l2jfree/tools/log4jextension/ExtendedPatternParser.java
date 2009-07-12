/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
