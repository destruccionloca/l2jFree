/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.serverpackets;

import java.util.List;

/**
 * Format: ch ddd [ddd]
 * 
 * @author KenM
 */
public class ExGetBossRecord extends L2GameServerPacket {
	private static final String _S__FE_33_EXGETBOSSRECORD = "[S] FE:33 ExGetBossRecord";

	private List<BossRecordInfo> _bossRecordInfo;

	private int _ranking;

	private int _totalPoints;

	public ExGetBossRecord(int ranking, int totalScore, List<BossRecordInfo> bossRecordInfo) {
		_ranking = ranking; // char ranking
		_totalPoints = totalScore; // char total points
		_bossRecordInfo = bossRecordInfo;
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl() {
		writeC(0xFE);
		writeH(0x33);
		writeD(_ranking); // char ranking
		writeD(_totalPoints); // char total points

		writeD(_bossRecordInfo.size()); // list size
		for (BossRecordInfo w : _bossRecordInfo) {
			writeD(w._bossId);
			writeD(w._points);
			writeD(w._unk1);// don`t know
		}
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType() {
		return _S__FE_33_EXGETBOSSRECORD;
	}

	public static class BossRecordInfo {
		public int _bossId;

		public int _points;

		public int _unk1;

		public BossRecordInfo(int bossId, int points, int unk1) {
			_bossId = bossId;
			_points = points;
			_unk1 = unk1;
		}
	}
}
