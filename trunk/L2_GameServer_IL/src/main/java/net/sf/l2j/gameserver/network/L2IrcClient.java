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
package net.sf.l2j.gameserver.network;

import java.io.IOException;
import java.security.cert.X509Certificate;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.schwering.irc.lib.ssl.SSLTrustManager;

/** 
 * @author evill33t
 */
public class L2IrcClient extends Thread {

	private final static Log _log = LogFactory.getLog(L2IrcClient.class.getName());
	private static Log _logChat = LogFactory.getLog("irc");
	
	private IRCConnection conn;
	private String channel;
	private String nickname;
	
	public L2IrcClient(String host, int port, String pass, String nick, String user, String name, boolean ssl, String Chan)
	{
		if (!ssl) 
		{
			conn = new IRCConnection(host, new int[] { port }, pass, nick, user, name);
		} 
		else 
		{
			conn = new SSLIRCConnection(host, new int[] { port }, pass, nick, user, name);
			((SSLIRCConnection)conn).addTrustManager(new TrustManager());
		}
		channel = Chan;
		nickname = nick;
		conn.addIRCEventListener(new Listener());
		conn.setEncoding("UTF-8");
		conn.setPong(true);
		conn.setDaemon(false);
		conn.setColors(false);
		start();
	}
	
	public void connect() throws IOException 
	{
		if(!conn.isConnected())
		{
			conn.connect();
			conn.send("JOIN " + channel);
			setDaemon(true);
		}
	}

	public void disconnect()
	{
		if(conn.isConnected())
		{
			conn.close();
			conn.setDaemon(false);
		}
	}
	
	public void send(String Text)
	{
		if(checkConnection())
			conn.send(Text);
	}
	
	public void send(String target,String Text)
	{
		if(checkConnection())
			conn.doPrivmsg(target, Text);
	}

	public void sendChan(String Text)
	{
		if(checkConnection())
		{
			conn.doPrivmsg(channel, Text);
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+channel +"> text");
		}
	}

	public boolean checkConnection()
	{
		if(!conn.isConnected())
		{
			try 
			{
				conn.close();
				connect();
			} 
			catch (Exception exc) 
			{
	          exc.printStackTrace();
	        }
		}
		return conn.isConnected();
	}
	
	public class TrustManager implements SSLTrustManager {
		private X509Certificate[] chain;
		public X509Certificate[] getAcceptedIssuers() {
			return chain != null ? chain : new X509Certificate[0];
		}
		public boolean isTrusted(X509Certificate[] chain) {
				this.chain = chain;
				return true;
		}
		
	}
	
	/**
	 * Treats IRC events.
	 */
	public class Listener implements IRCEventListener {

		private boolean isconnected;
		
		public void onRegistered() 
		{
			_log.info("IRC: Connected");

			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Connected");
			
			if(!Config.IRC_LOGIN_COMMAND.trim().equals(""))
				send(Config.IRC_LOGIN_COMMAND.trim());
			
			if(Config.IRC_NICKSERV)
				send(Config.IRC_NICKSERV_NAME,Config.IRC_NICKSERV_COMMAND);
			
			isconnected = true;
		}
		
		public void onDisconnected() 
		{
			_log.info("IRC: Disconnected");

			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Disconnected");
			
			isconnected = false;
			conn.close();			
		}
		
		public void onError(String msg) 
		{
			_log.info("IRC: Error: "+ msg);

			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Error: "+ msg);
		}
		
		public void onError(int num, String msg) 
		{
			_log.info("IRC: Error #"+ num +": "+ msg);
			
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Error #"+ num +": "+ msg);
			
			// nickname already in use
			if(num==433)
			{
				Integer random = Rnd.get(999);
				send("NICK "+nickname+random);
				send("JOIN "+channel);
			}
		}
		
		public void onInvite(String chan, IRCUser u, String nickPass) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+ chan +"> "+ u.getNick() +" invites "+ nickPass);
		}
		
		public void onJoin(String chan, IRCUser u) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+chan +"> "+ u.getNick() +" joins");			
		}
		
		public void onKick(String chan, IRCUser u, String nickPass, String msg) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+chan +"> "+ u.getNick() +" kicks "+ nickPass);
		}
		
		public void onMode(IRCUser u, String nickPass, String mode) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC Mode: "+ u.getNick() +" sets modes "+ mode +" "+ nickPass);
		}
		
		public void onMode(String chan, IRCUser u, IRCModeParser mp) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+chan +"> "+ u.getNick() +" sets mode: "+ mp.getLine());
		}
		
		public void onNick(IRCUser u, String nickNew) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC Nick: "+ u.getNick() +" is now known as "+ nickNew);
		}
		
		public void onNotice(String target, IRCUser u, String msg) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("Irc "+target +"> "+ u.getNick() +" (notice): "+ msg);
		}
		
		public void onPart(String chan, IRCUser u, String msg) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+chan +"> "+ u.getNick() +" parts");
		}
		
		public void onPrivmsg(String chan, IRCUser u, String msg) 
		{
			
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+chan +"> "+ u.getNick() +": "+ msg);

			if(chan.equals(channel))
			{
				if(Config.IRC_TO_GAME_TYPE.equals("global") || Config.IRC_TO_GAME_TYPE.equals("special"))
				{
					if(Config.IRC_TO_GAME_TYPE.equals("global") || 
							(Config.IRC_TO_GAME_TYPE.equals("special") && 
									msg.substring(0,1).equals(Config.IRC_TO_GAME_SPECIAL_CHAR) && 
									msg.length()>=2)
							)
						{
						
						String sendmsg;
						if(Config.IRC_TO_GAME_TYPE.equals("special"))
							sendmsg = msg.substring(1,msg.length());
						else
							sendmsg = msg;

						Integer ChatType = 1;
						
						if(Config.IRC_TO_GAME_DISPLAY.equals("trade"))
							ChatType = 8;
						if(Config.IRC_TO_GAME_DISPLAY.equals("hero"))
							ChatType = 17;
						
						CreatureSay cs = new CreatureSay(0, ChatType, "[IRC] "+u.getNick(), sendmsg);

		            	for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		            	{
		                    player.sendPacket(cs);
		            	}
					}
				}
			}
			
			if(msg.equals("!online"))
			{
				sendChan("Online Players: " + L2World.getInstance().getAllPlayersCount() + " / " + LoginServerThread.getInstance().getMaxPlayer());
			}

			if(msg.equals("!gmlist"))
			{
				if(GmListTable.getInstance().getAllGms(false).size() == 0)
					sendChan("There are not any GMs that are providing customer service currently");
				else
					for (L2PcInstance gm : GmListTable.getInstance().getAllGms(false))
						sendChan(gm.getName());
			}
			
		}
		
		public void onQuit(IRCUser u, String msg) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC Quit: "+ u.getNick());
		}
		
		public void onReply(int num, String value, String msg) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC REPLY #"+ num +": "+ value +" "+ msg);
		}
		
		public void onTopic(String chan, IRCUser u, String topic) 
		{
			if(Config.IRC_LOG_CHAT)
				_logChat.info("IRC: "+chan +"> "+ u.getNick() +" changes topic into: "+ topic);
		}
		
		public void onPing(String p) 
		{
			if(_log.isDebugEnabled())
				_log.info("IRC: Ping Pong");
			
			// keep connection alive
			conn.doPong(p);
		}
		
		public void unknown(String a, String b, String c, String d) 
		{
			_log.warn("IRC UNKNOWN: "+ a +" b "+ c +" "+ d);
		}
		
		public boolean isConnected()
		{
			return isconnected;
		}
	}
}
