/**
 * 
 */
package net.sf.l2j;

import java.util.Properties;

/**
 * @author Noctarius
 *
 */
public class L2Properties extends Properties
{
	private static final long serialVersionUID = -4599023842346938325L;

	@Override
	public String getProperty(String key, String defaultValue)
	{
		String value = super.getProperty(key, defaultValue);
		
		return value.trim();
	}
}
