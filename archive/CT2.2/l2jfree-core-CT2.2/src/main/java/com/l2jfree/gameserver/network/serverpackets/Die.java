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

import com.l2jfree.Config;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2SiegeClan;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.FortSiege;
import com.l2jfree.gameserver.model.entity.Siege;

/**
 * sample
 * 0b 
 * 952a1048     objectId 
 * 00000000 00000000 00000000 00000000 00000000 00000000

 * format  dddddd   rev 377
 * format  ddddddd   rev 417
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 18:46:18 $
 */
public class Die extends L2GameServerPacket
{
	private static final String _S__00_DIE = "[S] 00 Die [dddddddd]";
	private int _charObjId;
	private boolean _fallDown;
	private boolean _sweepable;
	private int _access;
	private L2Character _activeChar;
	private int _showVillage;
	private int _showClanhall;
	private int _showCastle;
	private int _showFlag;
	private int _showFortress;

	/**
	 * @param _characters
	 */
	public Die(L2Character cha)
	{
		_activeChar = cha;
		L2Clan clan = null;
		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)cha;
			_access = player.getAccessLevel();
			clan = player.getClan();
		}
		_charObjId = cha.getObjectId();
		_fallDown = cha.mustFallDownOnDeath();
		if (cha instanceof L2Attackable)
			_sweepable = ((L2Attackable)cha).isSweepActive();
		if(clan != null)
		{
			_showClanhall = clan.getHasHideout() <= 0 ? 0 : 1;
			_showCastle = clan.getHasCastle() <= 0 ? 0 : 1;
			_showFortress = clan.getHasFort() <= 0 ? 0 : 1;
			L2SiegeClan siegeClan = null;
			boolean isInDefense = false;
			Siege siege = SiegeManager.getInstance().getSiege(_activeChar);
			if(siege != null && siege.getIsInProgress())
			{
				siegeClan = siege.getAttackerClan(clan);
				if(siegeClan == null && siege.checkIsDefender(clan))
					isInDefense = true;
			}
			else
			{
				FortSiege fsiege = FortSiegeManager.getInstance().getSiege(_activeChar);
				if (fsiege != null && fsiege.getIsInProgress())
				{
					siegeClan = fsiege.getAttackerClan(clan);
					if(siegeClan == null && fsiege.checkIsDefender(clan))
						isInDefense = true;
				}
			}
			_showFlag = (siegeClan == null || isInDefense || siegeClan.getFlag().size() <= 0) ? 0 : 1;
		}
		_showVillage = 1;
	}

	@Override
	protected final void writeImpl()
	{
		if (!_fallDown)
			return;

		writeC(0x0);
		
		writeD(_charObjId); 
		writeD(_showVillage);
		writeD(_showClanhall);
		writeD(_showCastle);
		writeD(_showFlag);
		writeD(_sweepable ? 0x01 : 0x00);              // sweepable  (blue glow)
		writeD(_access >= Config.GM_FIXED ? 0x01: 0x00); // 6d 04 00 00 00 - to FIXED
		writeD(_showFortress);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__00_DIE;
	}
}
