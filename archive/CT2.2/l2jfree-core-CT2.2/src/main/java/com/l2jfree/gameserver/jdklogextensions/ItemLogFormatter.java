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
package com.l2jfree.gameserver.jdklogextensions;

import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

/**
 * @author Advi
 */
public final class ItemLogFormatter extends L2LogFormatter
{
	@Override
	protected void format0(LogRecord record, TextBuilder tb)
	{
		appendDate(record, tb);
		appendMessage(record, tb);
		appendParameters(record, tb, ", ", true);
	}
}
