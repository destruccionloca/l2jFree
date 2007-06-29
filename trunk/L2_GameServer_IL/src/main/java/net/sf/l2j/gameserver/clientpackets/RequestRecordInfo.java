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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.DoorInfo;
import net.sf.l2j.gameserver.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.GetOnVehicle;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.PetItemList;
import net.sf.l2j.gameserver.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.serverpackets.SpawnItem;
import net.sf.l2j.gameserver.serverpackets.SpawnItemPoly;
import net.sf.l2j.gameserver.serverpackets.StaticObject;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.serverpackets.VehicleInfo;

public class RequestRecordInfo extends L2GameClientPacket
{
    private static final String _0__CF_REQUEST_RECORD_INFO = "[0] CF RequestRecordInfo";

    /** urgent messages, execute immediatly */
    public TaskPriority getPriority() { return TaskPriority.PR_NORMAL; }

    protected void readImpl()
    {
        // trigger
    }

    protected void runImpl()
    {
        L2PcInstance _activeChar = getClient().getActiveChar();

        if (_activeChar == null)
            return;

        _activeChar.getKnownList().updateKnownObjects();
        _activeChar.sendPacket(new UserInfo(_activeChar));

        for (L2Object object : _activeChar.getKnownList().getKnownObjects().values())
        {
            if (object == null)
                continue;

            if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
                _activeChar.sendPacket(new SpawnItemPoly(object));
            else
            {
                if (object instanceof L2ItemInstance)
                    _activeChar.sendPacket(new SpawnItem((L2ItemInstance) object));
                else if (object instanceof L2DoorInstance)
                {
                    _activeChar.sendPacket(new DoorInfo((L2DoorInstance) object));
                    _activeChar.sendPacket(new DoorStatusUpdate((L2DoorInstance) object));
                }
                else if (object instanceof L2BoatInstance)
                {
                    if(!_activeChar.isInBoat() && object != _activeChar.getBoat())
                    {
                        _activeChar.sendPacket(new VehicleInfo((L2BoatInstance) object));
                        ((L2BoatInstance) object).sendVehicleDeparture(_activeChar);
                    }
                }
                else if (object instanceof L2StaticObjectInstance)
                    _activeChar.sendPacket(new StaticObject((L2StaticObjectInstance) object));
                else if (object instanceof L2NpcInstance)
                    _activeChar.sendPacket(new NpcInfo((L2NpcInstance) object, _activeChar));
                else if (object instanceof L2Summon)
                {
                    L2Summon summon = (L2Summon) object;

                    // Check if the L2PcInstance is the owner of the Pet
                    if (_activeChar.equals(summon.getOwner()))
                    {
                        _activeChar.sendPacket(new PetInfo(summon));

                        if (summon instanceof L2PetInstance)
                            _activeChar.sendPacket(new PetItemList((L2PetInstance) summon));
                    }
                    else
                        _activeChar.sendPacket(new NpcInfo(summon, _activeChar));
                }
                else if (object instanceof L2PcInstance)
                {
                    L2PcInstance otherPlayer = (L2PcInstance) object;

                    if (otherPlayer.isInBoat())
                    {
						otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
						_activeChar.sendPacket(new CharInfo(otherPlayer));
						int relation = otherPlayer.getRelation(_activeChar);
						if (otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != null && otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != relation)
							_activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
						_activeChar.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
                    }
                    else
					{
						_activeChar.sendPacket(new CharInfo(otherPlayer));
						int relation = otherPlayer.getRelation(_activeChar);
						if (otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != null && otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != relation)
							_activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
					}
                }

                if (object instanceof L2Character)
                {
                    // Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
                    L2Character obj = (L2Character) object;
                    obj.getAI().describeStateToPlayer(_activeChar);
                }
            }
        }
    }

    public String getType()
    {
        return _0__CF_REQUEST_RECORD_INFO;
    }
}
