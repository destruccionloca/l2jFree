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

import com.l2jfree.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jfree.gameserver.model.L2Manor;

import javolution.util.FastList;


/**
 * Format: ch cddd[ddddcdcdcd]
 * c - id (0xFE)
 * h - sub id (0x1D)
 * 
 * c
 * d - manor id
 * d
 * d - size
 * [
 * d - crop id
 * d - residual buy
 * d - start buy
 * d - buy price
 * c - reward type
 * d - seed level
 * c - reward 1 items
 * d - reward 1 item id
 * c - reward 2 items
 * d - reward 2 item id
 * ]
 * 
 * @author l3x
 */

public class ExShowCropInfo extends L2GameServerPacket
{
	private static final String _S__FE_1C_EXSHOWSEEDINFO = "[S] FE:1D ExShowCropInfo";
	private FastList<CropProcure> _crops;
	private int _manorId;

    public ExShowCropInfo(int manorId, FastList<CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
		if (_crops == null)
		{
			_crops = new FastList<CropProcure>();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);     // Id
		writeH(0x24);     // SubId
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_crops.size());
		for (CropProcure crop : _crops) {
			writeD(crop.getId());          // Crop id
			writeD(crop.getAmount());      // Buy residual
			writeD(crop.getStartAmount()); // Buy
			writeD(crop.getPrice());       // Buy price
			writeC(crop.getReward());      // Reward
			writeD(L2Manor.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			writeC(1); // rewrad 1 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(),1));    // Rewrad 1 Type Item Id
			writeC(1); // rewrad 2 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(),2));    // Rewrad 2 Type Item Id
		}
	}

	@Override
	public String getType()
	{
		return _S__FE_1C_EXSHOWSEEDINFO;
	}
}
