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
