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
package com.l2jfree.gameserver.network.serverpackets;

import java.util.regex.Matcher;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;

public final class NpcHtmlMessage extends AbstractNpcHtmlMessage
{
	private static final String _S__1B_NPCHTMLMESSAGE = "[S] 0f NpcHtmlMessage";
	
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	private final int _npcObjId;
	private StringBuilder _builder;
	private int _itemId = 0;
	
	public NpcHtmlMessage(int npcObjId, int itemId)
	{
		_npcObjId = npcObjId;
		_itemId = itemId;
	}
	
	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		
		setHtml(text);
	}
	
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	@Override
	public void prepareToSend(L2GameClient client, L2PcInstance activeChar)
	{
		if (activeChar != null)
			activeChar.buildBypassCache(_builder);
	}
	
	@Override
	public void setHtml(CharSequence text)
	{
		if (text instanceof StringBuilder)
			_builder = (StringBuilder)text;
		else
			_builder = new StringBuilder(text);
	}
	
	@Override
	public boolean canBeSentTo(L2GameClient client, L2PcInstance activeChar)
	{
		return _builder != null;
	}
	
	public void replace(String pattern, String value)
	{
		if (_builder == null)
			return;
		
		value = NpcHtmlMessage.quoteReplacement(value);
		
		for (int index = 0; (index = _builder.indexOf(pattern, index)) != -1; index += value.length())
			_builder.replace(index, index + pattern.length(), value);
	}
	
	public void replace(String pattern, long value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public void replace(String pattern, double value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public void replace(String pattern, Object value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	/**
	 * Inverse of {@link Matcher#quoteReplacement(String)}.
	 * 
	 * @param s
	 * @return
	 */
	static String quoteReplacement(String s)
	{
		if (s.indexOf('\\') == -1)
			return s;
		
		final StringBuilder sb = new StringBuilder(s.length());
		
		for (int i = 0; i < s.length(); i++)
		{
			final char c = s.charAt(i);
			
			if (c == '\\' && i + 1 < s.length())
			{
				sb.append(s.charAt(i + 1));
				i++;
			}
			else
			{
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	@Override
	protected CharSequence getContent()
	{
		return _builder;
	}
	
	@Override
	protected int getNpcObjectId()
	{
		return _npcObjId;
	}
	
	@Override
	protected int getItemId()
	{
		return _itemId;
	}
	
	@Override
	public String getType()
	{
		return _S__1B_NPCHTMLMESSAGE;
	}
}
