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
package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.RaidPointsManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExGetBossRecord;
import net.sf.l2j.gameserver.network.serverpackets.ExGetBossRecord.BossRecordInfo;

/**
 * Format: (ch) d
 * @author  -Wooden-
 * 
 */
public class RequestGetBossRecord extends L2GameClientPacket
{
    private static final String _C__D0_18_REQUESTGETBOSSRECORD = "[C] D0:18 RequestGetBossRecord";
    private int _bossId;

    /**
     * @param buf
     * @param client
     */
    @Override
    protected void readImpl()
    {
        _bossId = readD(); // always 0?
    }

    /**
     * @see net.sf.l2j.gameserver.network.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
    	L2PcInstance activeChar = getClient().getActiveChar();
		int totalPoints = 0;
		int ranking = 0;
		if(_bossId != 0) System.out.print("[C] D0:18 RequestGetBossRecord _bossId=" + _bossId);
		if(activeChar == null)
			return;

		List<BossRecordInfo> list = new FastList<BossRecordInfo>();
		Map<Integer, Integer> points = RaidPointsManager.getInstance().getPointsByOwnerId(activeChar.getObjectId());
		if(points != null && !points.isEmpty())
			for(int bossId : points.keySet())
				switch(bossId)
				{
					case -1:
						ranking = points.get(bossId);
						break;
					case 0:
						totalPoints = points.get(bossId);
						break;
					default:
						list.add(new BossRecordInfo(bossId, points.get(bossId), 0));
				}

		activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
    }

    /**
     * @see net.sf.l2j.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__D0_18_REQUESTGETBOSSRECORD;
    }
}
