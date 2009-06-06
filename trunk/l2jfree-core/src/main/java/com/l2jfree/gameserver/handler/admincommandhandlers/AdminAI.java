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
package com.l2jfree.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Attackable.AggroInfo;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;


public class AdminAI implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_show_ai" };

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.equals("admin_show_ai"))
		{
			L2Object target = activeChar.getTarget();
			if (!(target instanceof L2Character) || !((L2Character)target).hasAI())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}

			L2CharacterAI ai = ((L2Character) target).getAI();
			CtrlIntention intention = ai.getIntention();
			String param0 = ai.getIntentionArg0() == null ? "--" : ai.getIntentionArg0().toString();
			String param1 = ai.getIntentionArg1() == null ? "--" : ai.getIntentionArg1().toString();

			NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
			TextBuilder html1 = new TextBuilder("<html><body><center><font color=\"LEVEL\">AI Information</font></center><br><br>");

			html1.append("<font color=\"LEVEL\">Intention</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Intention:</td><td>" + intention.toString() + "</td></tr>");
			html1.append("<tr><td>Parameter0:</td><td>" + param0 + "</td></tr>");
			html1.append("<tr><td>Parameter1:</td><td>" + param1 + "</td></tr>");
			html1.append("</table><br><br>");

			if (target instanceof L2Attackable)
			{
				AggroInfo[] aggro = ((L2Attackable)target).copyAggroList();

				html1.append("<font color=\"LEVEL\">Aggrolist</font>");
				html1.append("<table border=\"0\" width=\"100%\">");
				for (AggroInfo a : aggro)
				{
					html1.append("<tr><td>"+a.getAttacker().getName()+"</td><td>" + a.getHate() + " ("+a.getDamage()+")</td></tr>");
				}
				html1.append("</table><br><br>");
			}

			html1.append("<button value=\"Refresh\" action=\"bypass -h admin_show_ai\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");

			html1.append("</body></html>");
			html.setHtml(html1.toString());
			activeChar.sendPacket(html);
		}

		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}