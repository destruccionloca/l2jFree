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
import com.l2jfree.lang.Replaceable;

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes
 * 
 * @see AbstractNpcHtmlMessage
 */
public final class NpcQuestHtmlMessage extends L2GameServerPacket
{
	private final int	_npcObjId;
	private Replaceable	_replaceable;
	private int			_questId	= 0;
	
	/**
	 * @param npcObjId
	 * @param questId
	 */
	public NpcQuestHtmlMessage(int npcObjId, int questId)
	{
		_npcObjId = npcObjId;
		_questId = questId;
	}
	
	@Override
	public void prepareToSend(L2GameClient client, L2PcInstance activeChar)
	{
		if (activeChar != null)
			activeChar.buildBypassCache(_replaceable);
	}
	
	public void setHtml(CharSequence text)
	{
		_replaceable = Replaceable.valueOf(text);
	}
	
	@Override
	public boolean canBeSentTo(L2GameClient client, L2PcInstance activeChar)
	{
		return _replaceable != null;
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
		_replaceable.replace(pattern, value);
	}
	
	public void replace(String pattern, long value)
	{
		_replaceable.replace(pattern, value);
	}
	
	public void replace(String pattern, double value)
	{
		_replaceable.replace(pattern, value);
	}
	
	public void replace(String pattern, Object value)
	{
		_replaceable.replace(pattern, value);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x8d);
		writeD(_npcObjId);
		writeS(_replaceable);
		writeD(_questId);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:8D NpcQuestHtmlMessage";
	}
}
