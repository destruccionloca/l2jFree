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
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.FortressManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2SiegeZone extends EntityZone
{
	@Override
	protected void register()
	{
		if (_castleId > 0)
		{
			_entity = CastleManager.getInstance().getCastleById(_castleId);
		}
		else if(_fortressId > 0)
		{
			_entity = FortressManager.getInstance().getFortressById(_fortressId);
		}
		_entity.registerSiegeZone(this);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_entity instanceof Castle)
		{
			if (((Castle)_entity).getSiege().getIsInProgress())
			{
				character.setInsideZone(FLAG_PVP, true);
				character.setInsideZone(FLAG_SIEGE, true);

				if (character instanceof L2PcInstance)
					((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			}
		}

		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (_entity instanceof Castle)
		{
			if (((Castle)_entity).getSiege().getIsInProgress())
			{
				character.setInsideZone(FLAG_PVP, false);
				character.setInsideZone(FLAG_SIEGE, false);

				if (character instanceof L2PcInstance)
				{
					((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));

					// Set pvp flag
					if (((L2PcInstance)character).getPvpFlag() == 0)
						((L2PcInstance)character).startPvPFlag();
				}
			}
			if (character instanceof L2SiegeSummonInstance)
			{
				((L2SiegeSummonInstance)character).unSummon(((L2SiegeSummonInstance)character).getOwner());
			}
		}

		super.onExit(character);
	}
}
