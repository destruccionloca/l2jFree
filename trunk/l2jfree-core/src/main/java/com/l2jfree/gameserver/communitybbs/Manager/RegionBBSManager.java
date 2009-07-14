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
package com.l2jfree.gameserver.communitybbs.Manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastMap;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameServer;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.datatables.RecordTable;
import com.l2jfree.gameserver.model.BlockList;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.base.Experience;
import com.l2jfree.gameserver.network.SystemChatChannelId;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.CreatureSay;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class RegionBBSManager extends BaseBBSManager
{
	private static Logger	_logChat	= Logger.getLogger("chat");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.l2jfree.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java
	 * .lang.String, com.l2jfree.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsloc"))
		{
			showOldCommunity(activeChar, 1);
		}
		else if (command.startsWith("_bbsloc;page;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int page = 0;
			try
			{
				page = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException nfe)
			{
			}

			showOldCommunity(activeChar, page);
		}
		else if (command.startsWith("_bbsloc;playerinfo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String name = st.nextToken();

			showOldCommunityPI(activeChar, name);
		}
		else
		{
			if (Config.COMMUNITY_TYPE == 1)
			{
				showOldCommunity(activeChar, 1);
			}
			else
			{
				notImplementedYet(activeChar, command);
			}
		}
	}

	/**
	 * @param activeChar
	 * @param name
	 */
	private void showOldCommunityPI(L2PcInstance activeChar, String name)
	{
		final TextBuilder htmlCode = TextBuilder.newInstance();
		htmlCode.append("<html><body><br>");
		htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
		L2PcInstance player = L2World.getInstance().getPlayer(name);

		if (player != null)
		{
			String sex = "Male";
			if (player.getAppearance().getSex())
			{
				sex = "Female";
			}
			String levelApprox = "low";
			if (player.getLevel() >= 60)
				levelApprox = "very high";
			else if (player.getLevel() >= 40)
				levelApprox = "high";
			else if (player.getLevel() >= 20)
				levelApprox = "medium";
			htmlCode.append("<table border=0><tr><td>").append(player.getName()).append(" (").append(sex).append(" ").append(player.getTemplate().getClassName()).append("):</td></tr>");
			htmlCode.append("<tr><td>Level: ").append(levelApprox).append("</td></tr>");
			htmlCode.append("<tr><td><br></td></tr>");

			if (activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId() || Config.SHOW_LEVEL_COMMUNITYBOARD))
			{
				long nextLevelExp = 0;
				long nextLevelExpNeeded = 0;
				if (player.getLevel() < (Experience.MAX_LEVEL - 1))
				{
					nextLevelExp = Experience.LEVEL[player.getLevel() + 1];
					nextLevelExpNeeded = nextLevelExp - player.getExp();
				}

				htmlCode.append("<tr><td>Level: ").append(player.getLevel()).append("</td></tr>");
				htmlCode.append("<tr><td>Experience: ").append(player.getExp()).append("/").append(nextLevelExp).append("</td></tr>");
				htmlCode.append("<tr><td>Experience needed for level up: ").append(nextLevelExpNeeded).append("</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
			}

			int uptime = (int) player.getUptime() / 1000;
			int h = uptime / 3600;
			int m = (uptime - (h * 3600)) / 60;
			int s = ((uptime - (h * 3600)) - (m * 60));

			htmlCode.append("<tr><td>Uptime: ").append(h).append("h ").append(m).append("m ").append(s).append("s</td></tr>");
			htmlCode.append("<tr><td><br></td></tr>");

			if (player.getClan() != null)
			{
				htmlCode.append("<tr><td>Clan: ").append(player.getClan().getName()).append("</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
			}

			htmlCode
					.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM ")
					.append(player.getName())
					.append(" pm pm pm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass _bbsloc\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
			htmlCode.append("</td></tr></table>");
			htmlCode.append("</body></html>");
			separateAndSend(htmlCode, activeChar);
		}
		else
		{
			separateAndSend("<html><body><br><br><center>No player with name " + name + "</center><br><br></body></html>", activeChar);
		}
	}

	/**
	 * @param activeChar
	 */
	private void showOldCommunity(L2PcInstance activeChar, int page)
	{
		separateAndSend(CommunityPageType.getType(activeChar).getPage(page), activeChar);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.l2jfree.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String,
	 * com.l2jfree.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;

		if (ar1.equals("PM"))
		{
			final TextBuilder htmlCode = TextBuilder.newInstance();
			htmlCode.append("<html><body><br>");
			htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

			try
			{

				L2PcInstance receiver = L2World.getInstance().getPlayer(ar2);
				if (receiver == null)
				{
					htmlCode.append("Player not found!<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;").append(ar2).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					htmlCode.append("</td></tr></table></body></html>");
					separateAndSend(htmlCode, activeChar);
					return;
				}
				if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
				{
					activeChar.sendMessage("Player is in jail.");
					return;
				}
				if (receiver.isChatBanned())
				{
					activeChar.sendMessage("Player is chat banned.");
					return;
				}
				if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
				{
					activeChar.sendMessage("You can not chat while in jail.");
					return;
				}

				if (Config.LOG_CHAT)
				{
					LogRecord record = new LogRecord(Level.INFO, ar3);
					record.setLoggerName("chat");
					record.setParameters(new Object[] { "TELL", "[" + activeChar.getName() + " to " + receiver.getName() + "]" });
					_logChat.log(record);
				}
				CreatureSay cs = new CreatureSay(activeChar.getObjectId(), SystemChatChannelId.Chat_Tell, activeChar.getName(), ar3);
				if (!BlockList.isBlocked(receiver, activeChar))
				{
					if (!receiver.getMessageRefusal())
					{
						receiver.sendPacket(cs);
						activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), SystemChatChannelId.Chat_Tell, "->" + receiver.getName(), ar3));
						htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;").append(receiver.getName()).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						htmlCode.append("</td></tr></table></body></html>");
						separateAndSend(htmlCode, activeChar);
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
						parsecmd("_bbsloc;playerinfo;" + receiver.getName(), activeChar);
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
					sm.addString(receiver.getName());
					activeChar.sendPacket(sm);
					sm = null;
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// ignore
			}
		}
		else
		{
			notImplementedYet(activeChar, ar1);
		}
	}


	/**
	 * @return
	 */
	public static RegionBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private RegionBBSManager()
	{
	}

	public synchronized void changeCommunityBoard()
	{
		CommunityPageType.PLAYER.clear();
		CommunityPageType.GM.clear();
	}
	
	private static final SimpleDateFormat format = new SimpleDateFormat("H:mm");
	private static final String tdClose = "</td>";
	private static final String tdOpen = "<td align=left valign=top>";
	private static final String trClose = "</tr>";
	private static final String trOpen = "<tr>";
	private static final String colSpacer = "<td FIXWIDTH=15></td>";
	
	private static enum CommunityPageType
	{
		PLAYER,
		GM;
		
		private static CommunityPageType getType(L2PcInstance activeChar)
		{
			return activeChar.isGM() ? GM : PLAYER;
		}
		
		private final List<L2PcInstance> _players = new ArrayList<L2PcInstance>();
		private final Map<Integer, String> _communityPages = new FastMap<Integer, String>();
		
		private synchronized void clear()
		{
			_players.clear();
			_communityPages.clear();
		}
		
		private synchronized String getPage(int page)
		{
			if (_players.isEmpty())
			{
				clear();
				
				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player == null)
						continue;
					
					if (player.isGM() && player.getAppearance().isInvisible())
						if (CommunityPageType.PLAYER == this)
							continue;
					
					_players.add(player);
				}
				
				Collections.sort(_players, new Comparator<L2PcInstance>() {
					private Integer getOrder(L2PcInstance player)
					{
						if (player.isGM())
							return 1;
						
						if (Config.SHOW_JAILED_PLAYERS)
							if (player.isInJail())
								return 2;
						
						if (Config.SHOW_CURSED_WEAPON_OWNER)
							if (player.isCursedWeaponEquipped())
								return 3;
						
						if (Config.SHOW_KARMA_PLAYERS)
							if (player.getKarma() > 0)
								return 4;
						
						if (Config.SHOW_CLAN_LEADER)
							if (player.isClanLeader() && player.getClan().getLevel() >= Config.SHOW_CLAN_LEADER_CLAN_LEVEL)
								return 5;
						
						if (player.isInOfflineMode())
							return 6;
						
						return 10;
					}
					
					public int compare(L2PcInstance p1, L2PcInstance p2)
					{
						final int value = getOrder(p1).compareTo(getOrder(p2));
						
						if (value != 0)
							return value;
						
						return p1.getName().compareToIgnoreCase(p2.getName());
					}
				});
			}
			
			final String communityPage = _communityPages.get(page);
			
			if (communityPage != null)
				return communityPage;
			
			final String generatedPage = generateHtml(page);
			
			_communityPages.put(page, generatedPage);
			
			return generatedPage;
		}
		
		private String generateHtml(int page)
		{
			if ((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD >= _players.size())
				return null;
			
			final int fromIndex = (page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD;
			final int toIndex = Math.min(_players.size(), fromIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD);
			
			final List<L2PcInstance> onlinePlayers = _players.subList(fromIndex, toIndex);
			
			final TextBuilder htmlCode = TextBuilder.newInstance();
			htmlCode.append("<html><body><br>");
			htmlCode.append("<table>");
			{
				htmlCode.append(trOpen);
				{
					final String gameTime = GameTimeController.getInstance().getFormattedGameTime();
					
					htmlCode.append(tdOpen).append("Server Time: ").append(format.format(new Date())).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("Game Time: ").append(gameTime).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("Server Restarted: ").append(GameServer.getStartedTime().getTime()).append(tdClose);
				}
				htmlCode.append(trClose);
			}
			htmlCode.append("</table>");
			htmlCode.append("<table>");
			{
				htmlCode.append(trOpen);
				{
					htmlCode.append(tdOpen).append("XP Rate: x").append(Config.RATE_XP).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("Party XP Rate: x").append(Config.RATE_PARTY_XP).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("XP Exponent: ").append(Config.ALT_GAME_EXPONENT_XP).append(tdClose);
				}
				htmlCode.append(trClose);
				htmlCode.append(trOpen);
				{
					htmlCode.append(tdOpen).append("SP Rate: x").append(Config.RATE_SP).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("Party SP Rate: x").append(Config.RATE_PARTY_SP).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("SP Exponent: ").append(Config.ALT_GAME_EXPONENT_SP).append(tdClose);
				}
				htmlCode.append(trClose);
				htmlCode.append(trOpen);
				{
					htmlCode.append(tdOpen).append("Drop Rate: ").append(Config.RATE_DROP_ITEMS).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("Spoil Rate: ").append(Config.RATE_DROP_SPOIL).append(tdClose);
					htmlCode.append(colSpacer);
					htmlCode.append(tdOpen).append("Adena Rate: ").append(Config.RATE_DROP_ADENA).append(tdClose);
				}
				htmlCode.append(trClose);
			}
			htmlCode.append("</table>");
			htmlCode.append("<table>");
			{
				htmlCode.append(trOpen);
				{
					htmlCode.append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>");
				}
				htmlCode.append(trClose);
				htmlCode.append(trOpen);
				{
					htmlCode.append(tdOpen).append(" Record of Player(s) Online:").append(RecordTable.getInstance().getMaxPlayer()).append(tdClose);
				}
				htmlCode.append(trClose);
				htmlCode.append(trOpen);
				{
					htmlCode.append(tdOpen).append(" On date : ").append(RecordTable.getInstance().getDateMaxPlayer())
						.append(tdClose);
				}
				htmlCode.append(trClose);
				if (CommunityPageType.GM == CommunityPageType.this)
				{
					htmlCode.append(trOpen);
					{
						htmlCode.append(tdOpen).append(L2World.getInstance().getAllVisibleObjectsCount()).append(" Object count").append(tdClose);
					}
					htmlCode.append(trClose);
				}
				htmlCode.append(trOpen);
				{
					htmlCode.append(tdOpen).append(_players.size()).append(" Player(s) Online").append(tdClose);
				}
				htmlCode.append(trClose);
				if (Config.SHOW_LEGEND)
				{
					htmlCode.append(trOpen);
					{
						htmlCode.append(tdOpen);
						htmlCode.append("<font color=\"LEVEL\">GM</font><font color=\"00FF00\">Clan Leader</font><font color=\"FF0000\">Cursedweapon</font><font color=\"FF00FF\">Karma</font><font color=\"999999\">Jailed</font>");
						htmlCode.append(tdClose);
					}
					htmlCode.append(trClose);
				}
			}
			htmlCode.append("</table>");
			
			if (Config.BBS_SHOW_PLAYERLIST)
			{
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr><td><table border=0>");
				
				int cell = 0;
				for (L2PcInstance player : onlinePlayers)
				{
					if (player == null)
						continue;
					
					cell++;
					
					if (cell == 1)
						htmlCode.append(trOpen);
					
					htmlCode.append("<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;").append(player.getName()).append("\">");
					
					if (player.isGM())
						htmlCode.append("<font color=\"LEVEL\">").append(player.getName()).append("</font>");
					else if (player.isInJail() && Config.SHOW_JAILED_PLAYERS)
						htmlCode.append("<font color=\"999999\">").append(player.getName()).append("</font>");
					else if (player.isCursedWeaponEquipped() && Config.SHOW_CURSED_WEAPON_OWNER)
						htmlCode.append("<font color=\"FF0000\">").append(player.getName()).append("</font>");
					else if (player.getKarma() > 0 && Config.SHOW_KARMA_PLAYERS)
						htmlCode.append("<font color=\"FF00FF\">").append(player.getName()).append("</font>");
					else if (player.getClan() != null && player.isClanLeader() && Config.SHOW_CLAN_LEADER && player.getClan().getLevel() >= Config.SHOW_CLAN_LEADER_CLAN_LEVEL)
						htmlCode.append("<font color=\"00FF00\">").append(player.getName()).append("</font>");
					else if (player.isInOfflineMode())
						htmlCode.append(player.getName()).append(" (Offline Mode)");
					else
						htmlCode.append(player.getName());
					
					htmlCode.append("</a></td>");
					
					if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
						htmlCode.append(colSpacer);
					
					if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
				if (cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					htmlCode.append(trClose);
				htmlCode.append("</table><br></td></tr>");
				
				htmlCode.append(trOpen);
				htmlCode.append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>");
				htmlCode.append(trClose);
				
				htmlCode.append("</table>");
			}
			
			if (_players.size() > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600>");
				
				htmlCode.append("<tr>");
				if (page == 1)
					htmlCode.append("<td align=right width=190><button value=\"Prev\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				else
					htmlCode.append("<td align=right width=190><button value=\"Prev\" action=\"bypass _bbsloc;page;").append(page - 1).append("\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				htmlCode.append("<td FIXWIDTH=10></td>");
				htmlCode.append("<td align=center valign=top width=200>Displaying ").append(
					(page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + 1).append(" - ").append(
					(page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + onlinePlayers.size()).append(
					" player(s)</td>");
				htmlCode.append("<td FIXWIDTH=10></td>");
				if (_players.size() <= (page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD))
					htmlCode.append("<td width=190><button value=\"Next\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				else
					htmlCode.append("<td width=190><button value=\"Next\" action=\"bypass _bbsloc;page;").append(page + 1).append("\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
			}
			
			htmlCode.append("</body></html>");
			
			try
			{
				return htmlCode.toString();
			}
			finally
			{
				TextBuilder.recycle(htmlCode);
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final RegionBBSManager _instance = new RegionBBSManager();
	}
}