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

import javolution.text.TextBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.knownlist.StaticObjectKnownList;
import com.l2jfree.gameserver.model.actor.stat.StaticObjStat;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ChairSit;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.ShowTownMap;
import com.l2jfree.gameserver.network.serverpackets.StaticObject;
import com.l2jfree.gameserver.templates.chars.L2CharTemplate;

/**
 * @author godson
 */
public class L2StaticObjectInstance extends L2Character
{
	protected final static Log _log = LogFactory.getLog(L2StaticObjectInstance.class.getName());
	
	/** The interaction distance of the L2StaticObjectInstance */
	public static final int INTERACTION_DISTANCE = 150;
	
	private int _staticObjectId;
	private int _meshIndex = 0; // 0 - static objects, alternate static objects
	private int _type = -1; // 0 - Map signs, 1 - Throne , 2 - Arena signs
	private int _x;
	private int _y;
	private String _texture;
	
	private volatile L2PcInstance _occupier = null;
	
	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}
		
		@Override
		public L2StaticObjectInstance getActor()
		{
			return L2StaticObjectInstance.this;
		}
		
		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}
		
		@Override
		public void moveTo(int x, int y, int z)
		{
		}
		
		@Override
		public void stopMove(L2CharPosition pos)
		{
		}
		
		@Override
		public void doAttack(L2Character target)
		{
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
		}
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		return null;
	}
	
	/**
	 * @return Returns the StaticObjectId.
	 */
	public int getStaticObjectId()
	{
		return _staticObjectId;
	}
	
	/**
	 */
	public L2StaticObjectInstance(int objectId, L2CharTemplate template, int staticId)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		_staticObjectId = staticId;
	}
	
	@Override
	public final StaticObjectKnownList getKnownList()
	{
		if (_knownList == null)
			_knownList = new StaticObjectKnownList(this);
		
		return (StaticObjectKnownList)_knownList;
	}
	
	@Override
	public final StaticObjStat getStat()
	{
		if (_stat == null)
			_stat = new StaticObjStat(this);
		
		return (StaticObjStat)_stat;
	}
	
	public boolean isOccupied()
	{
		return (_occupier != null);
	}
	
	public void setOccupier(L2PcInstance actualPersonToSitOn)
	{
		_occupier = actualPersonToSitOn;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public void setType(int type)
	{
		_type = type;
	}
	
	public void setMap(String texture, int x, int y)
	{
		_texture = "town_map." + texture;
		_x = x;
		_y = y;
	}
	
	private int getMapX()
	{
		return _x;
	}
	
	private int getMapY()
	{
		return _y;
	}
	
	@Override
	public final int getLevel()
	{
		return 1;
	}
	
	/**
	 * This is called when a player interacts with this NPC
	 * 
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (_type < 0)
			_log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + getStaticObjectId());
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				
				// Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (_type == 2)
				{
					String filename = "data/html/signboard.htm";
					String content = HtmCache.getInstance().getHtm(filename);
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					
					if (content == null)
						html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>");
					else
						html.setHtml(content);
					
					player.sendPacket(html);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (_type == 0)
				{
					player.sendPacket(new ShowTownMap(_texture, getMapX(), getMapY()));
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player == null)
			return;
		
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			player.setTarget(this);
			
			StaticObject su = new StaticObject(this);
			
			player.sendPacket(su);
			
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td>X: " + getX() + "</td></tr>");
			html1.append("<tr><td>Y: " + getY() + "</td></tr>");
			html1.append("<tr><td>Z: " + getZ() + "</td></tr>");
			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Static Object ID: " + getStaticObjectId() + "</td></tr>");
			html1.append("<tr><td>Mesh Index: " + getMeshIndex() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			
			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table></body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else
		{
			// ATTACK the mob without moving?
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * <B>Check if the given player can sit on this throne</B> (<I>= if a CharSit packet can be
	 * broadcast</I>)<BR>
	 * Things done on check:
	 * <LI>Occupier cleanup</LI>
	 * <LI>Distance between player and throne</LI>
	 * <LI>Object's type</LI>
	 * <LI>Player's clan's castle status</LI>
	 * <LI>Throne's castle status and equality to player's clan's castle</LI>
	 * <LI>If configured, checked if a player is a clan's leader</LI>
	 * @param player Player who changes wait type
	 * @return whether the ChairSit was broadcasted
	 */
	public boolean onSit(L2PcInstance player)
	{
		// This check is added if player that sits on the chair was not
		// removed from the world properly
		if (_occupier != null && L2World.getInstance().findPlayer( // If the actual user isn't
				_occupier.getObjectId()) == null) // found in the world anymore
				_occupier = null; // release me

		if (player == null) return false;
		if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
			return false;

		Castle throneCastle = CastleManager.getInstance().getCastle(this);
		if (getType() != 1 || isOccupied() || throneCastle == null)
			return false;
		L2Clan c = player.getClan();
		if (c == null) return false;
		if (c.getHasCastle() != throneCastle.getCastleId())
			return false;
		if (Config.ALT_ONLY_CLANLEADER_CAN_SIT_ON_THRONE && !player.isClanLeader())
			return false;

		setOccupier(player);
		player.setObjectSittingOn(this);
		ChairSit cs = new ChairSit(player, getStaticObjectId());
		player.sitDown();
		player.broadcastPacket(cs);
		
		return true;
	}
	
	/**
	 * Set the meshIndex of the object<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li> default textures : 0</li>
	 * <li> alternate textures : 1 </li>
	 * <BR>
	 * <BR>
	 * 
	 * @param meshIndex
	 */
	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
		broadcastPacket(new StaticObject(this));
	}
	
	/**
	 * Return the meshIndex of the object.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li> default textures : 0</li>
	 * <li> alternate textures : 1 </li>
	 * <BR>
	 * <BR>
	 */
	public int getMeshIndex()
	{
		return _meshIndex;
	}
	
	@Override
	public void broadcastFullInfoImpl()
	{
	}
}
