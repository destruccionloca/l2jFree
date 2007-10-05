/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.communitybbs.Manager;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ShowBoard;


/**
 * 
 * This class is the mother class of all class callable in communityBoard 
 * it define two methods : parseCmd and parseWrite
 * this methods return html code to the player.
 * Maybe we could use freemarker to manage external templates one day
 * To be continued...
 * 
 */
public abstract class BaseBBSManager
{
	public abstract void parsecmd(String command, L2PcInstance activeChar);
	public abstract void parsewrite(String ar1,String ar2,String ar3,String ar4,String ar5, L2PcInstance activeChar);
	protected void separateAndSend(String html, L2PcInstance activeChar)
	{
		if (html == null) return;
		if (html.length() < 8180)
		{
			activeChar.sendPacket(new ShowBoard(html, "101"));
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < 8180 * 2)
		{
			activeChar.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			activeChar.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < 8180 * 3)
		{
			activeChar.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			activeChar.sendPacket(new ShowBoard(html.substring(8180, 8180 * 2), "102"));
			activeChar.sendPacket(new ShowBoard(html.substring(8180 * 2, html.length()), "103"));
		}
	}
	/**
	 * @param html
	 */
	protected void send1001(String html, L2PcInstance activeChar)
	{
		if (html.length() < 8180)
		{
			activeChar.sendPacket(new ShowBoard(html, "1001"));			
		}
	}
	/**
	 * @param i
	 */
	protected void send1002(L2PcInstance activeChar)
	{		
		send1002(activeChar," "," ","0");
	}
	/**
	 * @param activeChar
	 * @param string
	 * @param string2
	 */
	protected void send1002(L2PcInstance activeChar, String string, String string2,String string3)
	{		
		FastList<String> _arg = new FastList<String>();
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add(activeChar.getName());
		_arg.add(Integer.toString(activeChar.getObjectId()));
		_arg.add(activeChar.getAccountName());
		_arg.add("9");
		_arg.add(string2);
		_arg.add(string2);
		_arg.add(string);		
		_arg.add(string3);
		_arg.add(string3);
		_arg.add("0");
		_arg.add("0");
		activeChar.sendPacket(new ShowBoard(_arg));
	}
}
