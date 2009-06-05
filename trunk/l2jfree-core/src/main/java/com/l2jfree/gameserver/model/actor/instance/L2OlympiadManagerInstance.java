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
package com.l2jfree.gameserver.model.actor.instance;

import java.util.HashMap;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Multisell;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.olympiad.Olympiad;
import com.l2jfree.gameserver.network.serverpackets.ExHeroList;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * Olympiad Npc's Instance
 * 
 * @author godson
 */
public class L2OlympiadManagerInstance extends L2NpcInstance
{
	private final static Log	_log		= LogFactory.getLog(L2OlympiadManagerInstance.class.getName());

	private static final int	GATE_PASS	= 6651;

	public L2OlympiadManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("OlympiadDesc"))
		{
			int val = Integer.parseInt(command.substring(13, 14));
			String suffix = command.substring(14);
			showChatWindow(player, val, suffix);
		}
		else if (command.startsWith("OlympiadNoble"))
		{
			if (!player.isNoble() || player.getClassId().level() < 3)
				return;

			int val = Integer.parseInt(command.substring(14));
			NpcHtmlMessage reply;
			TextBuilder replyMSG;

			switch (val)
			{
				case 1:
					Olympiad.getInstance().unRegisterNoble(player);
					break;
				case 2:
					int classed = 0;
					int nonClassed = 0;
					int[] array = Olympiad.getInstance().getWaitingList();

					if (array != null)
					{
						classed = array[0];
						nonClassed = array[1];

					}

					reply = new NpcHtmlMessage(getObjectId());
					replyMSG = new TextBuilder("<html><body>The number of people on the waiting list for Grand Olympiad<center>");
					replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");
					replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");
					replyMSG.append("<tr><td fixwidth=100>Class (Individual)</td>");
					replyMSG.append("<td fixwidth=170 align=right>");
					replyMSG.append((classed < 100 ? "Fewer than 100" : "More than 100"));
					replyMSG.append("</td></tr>");
					replyMSG.append("<tr><td fixwidth=100>Class-Irrelevant (Team)</td>");
					replyMSG.append("<td fixwidth=170 align=right>Fewer than 100</td>"); // once team (3vs3) will be supported, unhardcode Fewer than 100 as its done with others
					replyMSG.append("</td></tr>");
					replyMSG.append("<tr><td fixwidth=100>Class-Irrelevant (Individual)</td>");
					replyMSG.append("<td fixwidth=170 align=right>");
					replyMSG.append((nonClassed < 100 ? "Fewer than 100" : "More than 100"));
					replyMSG.append("</td></tr></table>");
					replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
					replyMSG.append("<table width=270 border=0 cellpadding=0 cellspacing=0>");
					replyMSG.append("<tr><td width=90 height=20 align=center>");
					replyMSG.append("<button value=\"Back\" action=\"bypass -h npc_");
					replyMSG.append(String.valueOf(getObjectId()));
					replyMSG.append("_OlympiadDesc 2a\" ");
					replyMSG.append("width=80 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table></center>");
					replyMSG.append("</body></html>");

					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 3:
					int points = Olympiad.getInstance().getNoblePoints(player.getObjectId());
					if (points >= 0)
					{
						reply = new NpcHtmlMessage(getObjectId());
						replyMSG = new TextBuilder("<html><body>");
						replyMSG.append("There are " + points + " Grand Olympiad " + "points granted for this event.<br><br>" + "<a action=\"bypass -h npc_"
								+ getObjectId() + "_OlympiadDesc 2a\">Return</a>");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 4:
					Olympiad.getInstance().registerNoble(player, false);
					break;
				case 5:
					Olympiad.getInstance().registerNoble(player, true);
					break;
				case 6:
					int passes = Olympiad.getInstance().getNoblessePasses(player.getObjectId());
					if (passes > 0)
					{
						// Sends the correct packet even for newly created items
						player.addItem("Olympiad", GATE_PASS, passes, player, true, true);
					}
					else
					{
						reply = new NpcHtmlMessage(getObjectId());
						String msg = "<html><body>Grand Olympiad Manager:<br>" +
								"I'm sorry. You do not have enough points to obtain Olympiad Tokens. Better luck next time.<br>" +
								"<a action=\"bypass -h npc_" + String.valueOf(getObjectId()) +
								"_OlympiadDesc 2a\">Return</a></body></html>";
						reply.setHtml(msg);
						player.sendPacket(reply);
					}
					break;
				case 7:
					L2Multisell.getInstance().separateAndSend(102, player, false, getCastle().getTaxRate());
					break;
				case 9:
					L2Multisell.getInstance().separateAndSend(103, player, false, getCastle().getTaxRate());
					break;
				case 8:
					int point = Olympiad.getInstance().getNoblePoints(player.getObjectId());
					if (point >= 0) {
						reply = new NpcHtmlMessage(getObjectId());
						String msg = "<html><body>Your Grand Olympiad Score from the previous period is " +
								String.valueOf(point) + " point(s). <br><a action=\"bypass -h npc_" +
								String.valueOf(getObjectId()) + "_OlympiadDesc 2a\">Return</a></body></html>";
						reply.setHtml(msg);
						player.sendPacket(reply);
					}
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;

			}
		}
		else if (command.startsWith("OlyBuff"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			String[] params = command.split(" ");
			int skillId = Integer.parseInt(params[1]);
			int skillLvl;

			// Oly buff whitelist prevents bypass exploiters -.-
			HashMap<Integer, Integer> buffList = new HashMap<Integer, Integer>();
			buffList.put(1086, 2); // Haste Lv2
			buffList.put(1204, 2); // Wind Walk Lv2
			buffList.put(1059, 3); // Empower Lv3
			buffList.put(1085, 3); // Acumen Lv3
			buffList.put(1078, 6); // Concentration Lv6
			buffList.put(1068, 3); // Might Lv3
			buffList.put(1240, 3); // Guidance Lv3
			buffList.put(1077, 3); // Focus Lv3 
			buffList.put(1242, 3); // Death Whisper Lv3
			buffList.put(1062, 2); // Berserk Spirit Lv2

			// Lets check on our oly buff whitelist
			if (!buffList.containsKey(skillId))
				return;

			// Get skilllevel from the hashmap
			skillLvl = buffList.get(skillId);

			L2Skill skill;
			skill = SkillTable.getInstance().getInfo(skillId, skillLvl);

			setTarget(player);

			if (player.olyBuff > 0)
			{
				skill.getEffects(player, player);
				//this.doCast(skill);
				player.olyBuff--;
			}

			if (player.olyBuff > 0)
			{
				html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_buffs.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_nobuffs.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				this.deleteMe();
			}
		}
		else if (command.startsWith("Olympiad"))
		{
			int val = Integer.parseInt(command.substring(9, 10));

			NpcHtmlMessage reply = new NpcHtmlMessage(getObjectId());
			TextBuilder replyMSG = new TextBuilder("<html><body>");

			switch (val)
			{
				case 1:
					FastMap<Integer, String> matches = Olympiad.getInstance().getMatchList();
					replyMSG.append("<br>Grand Olympiad Competition View <br> Warning: "
							+ "If you choose to watch an Olympiad game, any summoning of Servitors " + "or Pets will be canceled. <br><br>");

					for (int i = 0; i < Olympiad.getStadiumCount(); i++)
					{
						int arenaID = i + 1;
						String title = "";
						if (matches.containsKey(i))
						{
							title = matches.get(i);
						}
						else
						{
							title = "&$906;"; // initial state
						}
						replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 3_" + i + "\">" + "Arena " + arenaID + "&nbsp;&nbsp;&nbsp;"
								+ title + "</a><br>");
					}

					replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
					replyMSG.append("<table width=270 border=0 cellpadding=0 cellspacing=0>");
					replyMSG.append("<tr><td width=90 height=20 align=center>");
					replyMSG.append("<button value=\"Back\" action=\"bypass -h npc_" + getObjectId()
							+ "_Chat 0\" width=80 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
					replyMSG.append("</td></tr></table></body></html>");

					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 2:
					// For example >> Olympiad 1_88
					int classId = Integer.parseInt(command.substring(11));
					if ((classId >= 88 && classId <= 118) || (classId >= 131 && classId <= 134) || classId == 136)
					{
						replyMSG.append("<center>Grand Olympiad Ranking");
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");

						FastList<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
						if (!names.isEmpty())
						{
							replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");

							int index = 1;

							for (String name : names)
							{
								replyMSG.append("<tr>");
								replyMSG.append("<td fixwidth=100 align=center>" + index++ + "</td>");
								replyMSG.append("<td fixwidth=170 align=center>" + name + "</td>");
								replyMSG.append("</tr>");
							}

							replyMSG.append("</table>");
						}

						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
						replyMSG.append("<table width=270 border=0 cellpadding=0 cellspacing=0>");
						replyMSG.append("<tr><td width=90 height=20 align=center>");
						replyMSG.append("<button value=\"Back\" action=\"bypass -h npc_" + getObjectId()
								+ "_OlympiadDesc 3a\" width=80 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table></center>");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 3:
					int id = Integer.parseInt(command.substring(11));
					Olympiad.addSpectator(id, player, true);
					break;
				case 4:
					player.sendPacket(new ExHeroList());
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void showChatWindow(L2PcInstance player, int val, String suffix)
	{
		String filename = Olympiad.OLYMPIAD_HTML_PATH;

		filename += "noble_desc" + val;
		filename += (suffix != null) ? suffix + ".htm" : ".htm";

		if (filename.equals(Olympiad.OLYMPIAD_HTML_PATH + "noble_desc0.htm"))
			filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";

		showChatWindow(player, filename);
	}
}