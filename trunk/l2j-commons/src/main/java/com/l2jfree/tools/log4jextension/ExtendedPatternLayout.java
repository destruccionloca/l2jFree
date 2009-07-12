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

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;

/**
 * Overload of PatternLayout class to handle throwable
 */
public class ExtendedPatternLayout extends PatternLayout
{
	/**
	 * Default Constructor
	 */
	public ExtendedPatternLayout()
	{
		this(DEFAULT_CONVERSION_PATTERN);
	}

	/**
	 * Conctructor with specific pattern
	 * 
	 * @param pattern
	 *            the pattern
	 */
	public ExtendedPatternLayout(String pattern)
	{
		super(pattern);
	}

	/**
	 * @see org.apache.log4j.PatternLayout#createPatternParser(java.lang.String)
	 */
	@Override
	public PatternParser createPatternParser(String pattern)
	{
		PatternParser result;
		if (pattern == null)
			result = new ExtendedPatternParser(DEFAULT_CONVERSION_PATTERN);
		else
			result = new ExtendedPatternParser(pattern);

		return result;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.apache.log4j.PatternLayout#ignoresThrowable() Return false,
	 *      l'ExtendedPattern utilise les Throwables !
	 */
	@Override
	public boolean ignoresThrowable()
	{
		return false;
	}
}
