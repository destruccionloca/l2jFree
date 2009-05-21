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

import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;

/**
*
* the HTML parser in the client knowns these standard and non-standard tags and attributes
* VOLUMN
* UNKNOWN
* UL
* U
* TT
* TR
* TITLE
* TEXTCODE
* TEXTAREA
* TD
* TABLE
* SUP
* SUB
* STRIKE
* SPIN
* SELECT
* RIGHT
* PRE
* P
* OPTION
* OL
* MULTIEDIT
* LI
* LEFT
* INPUT
* IMG
* I
* HTML
* H7
* H6
* H5
* H4
* H3
* H2
* H1
* FONT
* EXTEND
* EDIT
* COMMENT
* COMBOBOX
* CENTER
* BUTTON
* BR
* BODY
* BAR
* ADDRESS
* A
* SEL
* LIST
* VAR
* FORE
* READONL
* ROWS
* VALIGN
* FIXWIDTH
* BORDERCOLORLI
* BORDERCOLORDA
* BORDERCOLOR
* BORDER
* BGCOLOR
* BACKGROUND
* ALIGN
* VALU
* READONLY
* MULTIPLE
* SELECTED
* TYP
* TYPE
* MAXLENGTH
* CHECKED
* SRC
* Y
* X
* QUERYDELAY
* NOSCROLLBAR
* IMGSRC
* B
* FG
* SIZE
* FACE
* COLOR
* DEFFON
* DEFFIXEDFONT
* WIDTH
* VALUE
* TOOLTIP
* NAME
* MIN
* MAX
* HEIGHT
* DISABLED
* ALIGN
* MSG
* LINK
* HREF
* ACTION
*/
public final class NpcHtmlMessage extends L2GameServerPacket
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
	
	public void setHtml(CharSequence text)
	{
		_builder = new StringBuilder(text);
	}
	
	public void setHtml(StringBuilder text)
	{
		_builder = text;
	}
	
	public void setFile(String path)
	{
		String content = HtmCache.getInstance().getHtm(path);
		
		if (content == null)
		{
			content = "<html><body>Sorry, my HTML is missing!<br>" + path + "</body></html>";
			
			_log.warn("Missing html page: " + path);
		}
		
		setHtml(content);
	}
	
	public void replace(String pattern, String value)
	{
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
	
	@Override
	protected void writeImpl()
	{
		writeC(0x19);
		writeD(_npcObjId);
		
		if (_builder.length() > 8192)
		{
			writeS("<html><body>Sorry, the HTML is too long!</body></html>");
			
			_log.warn("The HTML is too long! This will crash the client!");
		}
		else
			writeS(_builder);
		
		writeD(_itemId);
	}
	
	@Override
	public String getType()
	{
		return _S__1B_NPCHTMLMESSAGE;
	}
}
