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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;
import net.sf.l2j.gameserver.communitybbs.services.forum.ForumService;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ShowBoard;

public class PostBBSManager extends BaseBBSManager
{

	private ForumService __forumService;
	
    /**
     * Constructor private to avoid direct initialization
     */
	private PostBBSManager()
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
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if(command.startsWith("_bbsposts;read;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
			String index = null;
			if(st.hasMoreTokens())
			{
			 index = st.nextToken();
			}
			int ind = 0;
			if(index == null)
			{
				ind = 1;
			}
			else
			{
				ind = Integer.parseInt(index);
			}			
				
			showPost(__forumService.getTopicById(idp),__forumService.getForumById(idf),activeChar,ind);		
		}
		else if(command.startsWith("_bbsposts;edit;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idt = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
			showEditPost(__forumService.getTopicById(idt),__forumService.getForumById(idf),activeChar,idp);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null,"102"));
			activeChar.sendPacket(new ShowBoard(null,"103"));  
		}		
	}
	/**
	 * @param topic
	 * @param forumByID
	 * @param activeChar
	 * @param idp
	 */
	private void showEditPost(Topic topic, Forums forum, L2PcInstance activeChar, int idp)
	{		
		Posts p = __forumService.getPostById(idp);
		if((forum == null)||(topic == null)||(p == null))
		{			
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error, this forum, topic or post does not exit !</center><br><br></body></html>","101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null,"102"));
			activeChar.sendPacket(new ShowBoard(null,"103"));  
		}	
		else
		{
			ShowHtmlEditPost(topic,activeChar,forum,p);
		}
	}

	/**
	 * @param posttByTopic
	 * @param forumByID
	 * @param activeChar
	 * @param ind
	 * @param idf
	 */
	private void showPost(Topic topic, Forums forum, L2PcInstance activeChar, int ind)
	{	
		if((forum == null)||(topic == null))
		{			
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error, this forum is not implemented yet</center><br><br></body></html>","101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null,"102"));
			activeChar.sendPacket(new ShowBoard(null,"103"));  
		}
		else if(forum.getForumType() == ForumService.MEMO)
		{
			ShowMemoPost(topic,activeChar,forum);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: "+forum.getForumName()+" is not implemented yet</center><br><br></body></html>","101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null,"102"));
			activeChar.sendPacket(new ShowBoard(null,"103"));  
		}
	}
	/**
	 * @param topic
	 * @param activeChar
	 * @param forum
	 * @param p
	 */
	private void ShowHtmlEditPost(Topic topic, L2PcInstance activeChar, Forums forum, Posts p)
	{		
        TextBuilder html = new TextBuilder("<html>");
		html.append("<body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">"+forum.getForumName()+" Form</a>");		
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
		html.append("<td FIXWIDTH=540>"+ topic.getTopicName() +"</td>");
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
		html.append("<td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Post "+forum.getForumId()+";"+topic.getTopicId()+";"+p.getPostId()+" _ Content Content Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>");
		html.append("<td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td>");
		html.append("<td align=center FIXWIDTH=400>&nbsp;</td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr></table>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		send1001(html.toString(),activeChar);    	  	    
    	send1002(activeChar,p.getPostTxt(),topic.getTopicName(),DateFormat.getInstance().format(new Date(topic.getTopicDate().longValue())));
	}
	
	/**
	 * @param topic
	 * @param activeChar
	 * @param forum 
	 */
	private void ShowMemoPost(Topic topic, L2PcInstance activeChar, Forums forum)
	{
        Posts firstPost = __forumService.getPostByIndexForTopic(topic,0);
		Locale locale = Locale.getDefault();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);	
        TextBuilder html = new TextBuilder("<html><body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 bgcolor=333333>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=55 align=right valign=top>&$413; : &nbsp;</td>");
		html.append("<td fixWIDTH=380 valign=top>"+topic.getTopicName()+"</td>");
		html.append("<td fixwidth=5></td>");
		html.append("<td fixwidth=50></td>");
		html.append("<td fixWIDTH=120></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td align=right><font color=\"AAAAAA\" >&$417; : &nbsp;</font></td>");
		html.append("<td><font color=\"AAAAAA\">"+topic.getTopicOwnername()+"</font></td>");
		html.append("<td></td>");
		html.append("<td><font color=\"AAAAAA\">&$418; :</font></td>");
		html.append("<td><font color=\"AAAAAA\">"+(firstPost.getPostDate() != null ? dateFormat.format(firstPost.getPostDate()) : "")+"</font></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td fixwidth=5></td>");
		String Mes = (firstPost.getPostTxt() != null ? firstPost.getPostTxt().replace(">","&gt;") : "" );
		Mes = Mes.replace("<","&lt;");
		Mes = Mes.replace("\n","<br1>");
		html.append("<td FIXWIDTH=600 align=left>"+ Mes +"</td>");
		html.append("<td fixqqwidth=5></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
		html.append("<img src=\"L2UI.squaregray\" width=\"610\" height=\"1\">");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
		html.append("<table border=0 cellspacing=0 cellpadding=0 FIXWIDTH=610>");
		html.append("<tr>");
		html.append("<td width=50>");
		html.append("<button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">");
		html.append("</td>");
		html.append("<td width=560 align=right><table border=0 cellspacing=0><tr>");
		html.append("<td FIXWIDTH=300></td><td><button value = \"&$424;\" action=\"bypass _bbsposts;edit;"+ forum.getForumId() +";"+ topic.getTopicId() +";"+firstPost.getPostId()+"\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;");
		html.append("<td><button value = \"&$425;\" action=\"bypass _bbstopics;del;"+forum.getForumId()+";"+ topic.getTopicId() +"\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;");
		html.append("<td><button value = \"&$421;\" action=\"bypass _bbstopics;crea;"+forum.getForumId()+"\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;");
		html.append("</tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>"); 
		html.append("<br>");
		html.append("<br>");
		html.append("<br></center>");
		html.append("</body>");
		html.append("</html>");
		separateAndSend(html.toString(),activeChar);
	}
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		
		
		StringTokenizer st = new StringTokenizer(ar1, ";");
		int idf = Integer.parseInt(st.nextToken());
		int idt = Integer.parseInt(st.nextToken());
		int idp = Integer.parseInt(st.nextToken());
		
			Forums f = __forumService.getForumById(idf);
			if(f == null)
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: "+idf+" does not exist !</center><br><br></body></html>","101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null,"102"));
				activeChar.sendPacket(new ShowBoard(null,"103"));				
			}
			else
			{
				
				Topic t = __forumService.getTopicById(idt); 
				if(t == null)
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the topic: "+idt+" does not exist !</center><br><br></body></html>","101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null,"102"));
					activeChar.sendPacket(new ShowBoard(null,"103"));				
				}
				else
				{
					Posts cp = __forumService.getPostById(idp);
					
					if(cp == null)
					{
						
						ShowBoard sb = new ShowBoard("<html><body><br><br><center>the post: "+idp+" does not exist !</center><br><br></body></html>","101");
						activeChar.sendPacket(sb);
						activeChar.sendPacket(new ShowBoard(null,"102"));
						activeChar.sendPacket(new ShowBoard(null,"103"));
					}
					else
					{
						
						cp.setPostTxt(ar4);
						__forumService.modifyPost(cp);						
						parsecmd("_bbsposts;read;"+ f.getForumId() +";"+ t.getTopicId(),activeChar);
					}
				}
			}
				
		
		
	}
	
}
	
