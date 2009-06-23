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
 * <b>The HTML parser in the client knowns these standard and non-standard tags and attributes:</b>
 * <ul>
 * <li>VOLUMN
 * <li>UNKNOWN
 * <li>UL
 * <li>U
 * <li>TT
 * <li>TR
 * <li>TITLE
 * <li>TEXTCODE
 * <li>TEXTAREA
 * <li>TD
 * <li>TABLE
 * <li>SUP
 * <li>SUB
 * <li>STRIKE
 * <li>SPIN
 * <li>SELECT
 * <li>RIGHT
 * <li>PRE
 * <li>P
 * <li>OPTION
 * <li>OL
 * <li>MULTIEDIT
 * <li>LI
 * <li>LEFT
 * <li>INPUT
 * <li>IMG
 * <li>I
 * <li>HTML
 * <li>H7
 * <li>H6
 * <li>H5
 * <li>H4
 * <li>H3
 * <li>H2
 * <li>H1
 * <li>FONT
 * <li>EXTEND
 * <li>EDIT
 * <li>COMMENT
 * <li>COMBOBOX
 * <li>CENTER
 * <li>BUTTON
 * <li>BR
 * <li>BR1
 * <li>BODY
 * <li>BAR
 * <li>ADDRESS
 * <li>A
 * <li>SEL
 * <li>LIST
 * <li>VAR
 * <li>FORE
 * <li>READONL
 * <li>ROWS
 * <li>VALIGN
 * <li>FIXWIDTH
 * <li>BORDERCOLORLI
 * <li>BORDERCOLORDA
 * <li>BORDERCOLOR
 * <li>BORDER
 * <li>BGCOLOR
 * <li>BACKGROUND
 * <li>ALIGN
 * <li>VALU
 * <li>READONLY
 * <li>MULTIPLE
 * <li>SELECTED
 * <li>TYP
 * <li>TYPE
 * <li>MAXLENGTH
 * <li>CHECKED
 * <li>SRC
 * <li>Y
 * <li>X
 * <li>QUERYDELAY
 * <li>NOSCROLLBAR
 * <li>IMGSRC
 * <li>B
 * <li>FG
 * <li>SIZE
 * <li>FACE
 * <li>COLOR
 * <li>DEFFON
 * <li>DEFFIXEDFONT
 * <li>WIDTH
 * <li>VALUE
 * <li>TOOLTIP
 * <li>NAME
 * <li>MIN
 * <li>MAX
 * <li>HEIGHT
 * <li>DISABLED
 * <li>ALIGN
 * <li>MSG
 * <li>LINK
 * <li>HREF
 * <li>ACTION
 * </ul>
 * 
 * @see NpcHtmlMessage#VALID_TAGS
 */
public final class NpcHtmlMessage extends L2GameServerPacket
{
	// Based on the comment before
	public static final String[] VALID_TAGS = { "volumn", "unknown", "ul", "u", "tt", "tr", "title", "textcode",
		"textarea", "td", "table", "sup", "sub", "strike", "spin", "select", "right", "pre", "p", "option", "ol",
		"multiedit", "li", "left", "input", "img", "i", "html", "h7", "h6", "h5", "h4", "h3", "h2", "h1", "font",
		"extend", "edit", "comment", "combobox", "center", "button", "br", "br1", "body", "bar", "address", "a", "sel",
		"list", "var", "fore", "readonl", "rows", "valign", "fixwidth", "bordercolorli", "bordercolorda",
		"bordercolor", "border", "bgcolor", "background", "align", "valu", "readonly", "multiple", "selected", "typ",
		"type", "maxlength", "checked", "src", "y", "x", "querydelay", "noscrollbar", "imgsrc", "b", "fg", "size",
		"face", "color", "deffon", "deffixedfont", "width", "value", "tooltip", "name", "min", "max", "height",
		"disabled", "align", "msg", "link", "href", "action" };
	
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
