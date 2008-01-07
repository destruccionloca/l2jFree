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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;
import net.sf.l2j.gameserver.communitybbs.services.forum.ForumService;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ShowBoard;


public class TopicBBSManager extends BaseBBSManager
{
	private ForumService __forumService;

	private TopicBBSManager()
	{
	}

	/**
	 * @param service
	 */
	public void setForumService(ForumService service)
	{
		__forumService = service;		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5,
							L2PcInstance activeChar)
	{
		if (ar1.equals("crea"))
		{
			Forums f = __forumService.getForumById(Integer.parseInt(ar2));
			if (f == null)
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + ar2
					+ " is not implemented yet</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				Topic t = new Topic ();
				t.setTopicForumId(Integer.parseInt(ar2));
				t.setTopicName(ar5);
				t.setTopicDate(new BigDecimal (Calendar.getInstance().getTimeInMillis()));
				t.setTopicOwnername(activeChar.getName());
				t.setTopicOwnerid(activeChar.getObjectId());
				t.setTopicType(ForumService.MEMO);
				t.setTopicReply(0);
				__forumService.createTopic(t);
				Posts p = new Posts();
				p.setPostOwnerName(activeChar.getName());
				p.setPostOwnerid(activeChar.getObjectId());
				p.setPostDate(new BigDecimal(Calendar.getInstance().getTimeInMillis()));
				p.setPostTopicId(t.getTopicId());
				p.setPostTxt(ar4);
				__forumService.createPost(p);
				parsecmd("_bbsmemo", activeChar);
			}

		}
		else if (ar1.equals("del"))
		{
			Forums f = __forumService.getForumById(Integer.parseInt(ar2));
			if (f == null)
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + ar2
					+ " does not exist !</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				Topic t = __forumService.getTopicById(Integer.parseInt(ar3));
				if (t == null)
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the topic: " + ar3
						+ " does not exist !</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				else
				{
					__forumService.deleteTopic(t);
					parsecmd("_bbsmemo", activeChar);
				}
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + ar1
				+ " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsmemo"))
		{
			Forums memo = __forumService.getMemoForAccountAndCreateIfNotAvailable(activeChar.getAccountName(), activeChar.getObjectId());
			showTopics(memo,activeChar,0,memo.getForumId());
		}
		else if (command.startsWith("_bbstopics;read"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			String index = null;
			if (st.hasMoreTokens())
			{
				index = st.nextToken();
			}
			int ind = 0;
			if (index == null)
			{
				ind = 1;
			}
			else
			{
				ind = Integer.parseInt(index);
			}
			showTopics(__forumService.getForumById(idf), activeChar, ind, idf);
		}
		else if (command.startsWith("_bbstopics;crea"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			showNewTopic(__forumService.getForumById(idf), activeChar, idf);
		}
		else if (command.startsWith("_bbstopics;del"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idt = Integer.parseInt(st.nextToken());
			Forums f = __forumService.getForumById(idf);
			if (f == null)
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf
					+ " does not exist !</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				Topic t = __forumService.getTopicById(idt);
				if (t == null)
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the topic: " + idt
						+ " does not exist !</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				else
				{
					__forumService.deleteTopic(t);
					parsecmd("_bbsmemo", activeChar);
				}
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command
				+ " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	/**
	 * @param forumByID
	 * @param activeChar
	 * @param idf
	 */
	private void showNewTopic(Forums forum, L2PcInstance activeChar, int idf)
	{
		if (forum == null)
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf
				+ " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if (forum.getForumType() == ForumService.MEMO)
		{
			ShowMemoNewTopics(forum, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + forum.getForumName()
				+ " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	/**
	 * @param forum
	 * @param activeChar
	 */
	private void ShowMemoNewTopics(Forums forum, L2PcInstance activeChar)
	{
        TextBuilder html = new TextBuilder("<html>");
		html.append("<body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr>");
		html.append("</table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29>&$413;</td>");
		html.append("<td FIXWIDTH=540><edit var = \"Title\" width=540 height=13></td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr></table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td>");
		html.append("<td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("</table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29>&nbsp;</td>");
		html.append("<td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Topic crea "
			+ forum.getForumId()
			+ " Title Content Title\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>");
		html.append("<td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td>");
		html.append("<td align=center FIXWIDTH=400>&nbsp;</td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr></table>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		send1001(html.toString(), activeChar);
		send1002(activeChar);
	}

	/**
	 * @param memo
	 */
	private void showTopics(Forums forum, L2PcInstance activeChar, int index, int idf)
	{
		if (forum == null)
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf
				+ " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if (forum.getForumType() == ForumService.MEMO)
		{
			ShowMemoTopics(forum, activeChar, index);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + forum.getForumName()
				+ " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	/**
	 * @param forum
	 * @param activeChar
	 * @param index
	 */
	private void ShowMemoTopics(Forums forum, L2PcInstance activeChar, int index)
	{
        TextBuilder html = new TextBuilder("<html><body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610>");
		html.append("<tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=415 align=center>&$413;</td>");
		html.append("<td FIXWIDTH=120 align=center></td>");
		html.append("<td FIXWIDTH=70 align=center>&$418;</td>");
		html.append("</tr>");
		html.append("</table>");
		
		// print 12 topics per page : page index = index
		List<Topic> topics = __forumService.getPaginatedTopicsByForumId(forum.getForumId(), 12, index);
        if ( topics != null )
        {
    		for (Topic t : topics)
            {
    			if (t != null)
    			{
    				html.append("<table border=0 cellspacing=0 cellpadding=5 WIDTH=610>");
    				html.append("<tr>");
    				html.append("<td FIXWIDTH=5></td>");
    				html.append("<td FIXWIDTH=415><a action=\"bypass _bbsposts;read;" + forum.getForumId()
    					+ ";" + t.getTopicId() + "\">" + t.getTopicName() + "</a></td>");
    				html.append("<td FIXWIDTH=120 align=center></td>");
    				html.append("<td FIXWIDTH=70 align=center>"
    					+ DateFormat.getInstance().format(new Date(t.getTopicDate().longValue())) + "</td>");
    				html.append("</tr>");
    				html.append("</table>");
    				html.append("<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
    			}
    		}
        }

		html.append("<br>");
		html.append("<table width=610 cellspace=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=50>");
		html.append("<button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">");
		html.append("</td>");
		html.append("<td width=510 align=center>");
		html.append("<table border=0><tr>");

		if (index == 1)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			html.append("<td><button action=\"bypass _bbstopics;read;" + forum.getForumId() + ";"
				+ (index - 1)
				+ "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		int nbp;
		nbp = __forumService.getTopicNumberForForum(forum) / 8 ; 
		if (nbp * 8 != ClanTable.getInstance().getClans().length)
		{
			nbp++;
		}
		for (int i = 1; i <= nbp; i++)
		{
			if (i == index)
			{
				html.append("<td> " + i + " </td>");
			}
			else
			{
				html.append("<td><a action=\"bypass _bbstopics;read;" + forum.getForumId() + ";" + i + "\"> "
					+ i + " </a></td>");
			}
		}
		if (index == nbp)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			html.append("<td><button action=\"bypass _bbstopics;read;" + forum.getForumId() + ";"
				+ (index + 1)
				+ "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}

		html.append("</tr></table> </td> ");
		html.append("<td align=right><button value = \"&$421;\" action=\"bypass _bbstopics;crea;"
			+ forum.getForumId()
			+ "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td></tr>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr> ");
		html.append("<td></td>");
		html.append("<td align=center><table border=0><tr><td></td><td><edit var = \"Search\" width=130 height=11></td>");
		html.append("<td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td> </tr></table> </td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<br>");
		html.append("<br>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		separateAndSend(html.toString(), activeChar);
	}
}
