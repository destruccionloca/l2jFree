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
package com.l2jfree.gameserver.handler.itemhandlers;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class ItemSkills implements IItemHandler
{
	private static final int[]	ITEM_IDS	=
											{
			65,
			725,
			726,
			728,
			733,
			734,
			735,
			1374,
			1375,
			1539,
			1540,
			4411,
			4412,
			4413,
			4414,
			4415,
			4417,
			5010,
			5234,
			5562,
			5563,
			5564,
			5565,
			5566,
			5583,
			5584,
			5585,
			5586,
			5587,
			5589,
			5591,
			5592,
			6035,
			6036,
			6403,
			6406,
			6407,
			6652,
			6654,
			6655,
			6903,
			7061,
			7062,
			8154,
			8155,
			8156,
			8157,
			8202,
			8555,
			8594,
			8595,
			8596,
			8597,
			8598,
			8599,
			8600,
			8601,
			8602,
			8603,
			8604,
			8605,
			8606,
			8607,
			8608,
			8609,
			8610,
			8611,
			8612,
			8613,
			8614,
			8952,
			8953,
			8954,
			8955,
			8956,
			9688,
			9689,
			9997,
			9998,
			9999,
			10000,
			10001,
			10002,
			10155,
			10157,
			10260,
			10261,
			10262,
			10263,
			10264,
			10265,
			10266,
			10267,
			10268,
			10269,
			10270,
			10409,
			10432,
			10433,
			10549,
			10550,
			10551,
			10552,
			10553,
			10554,
			10555,
			10556,
			10557,
			10558,
			10559,
			10560,
			10561,
			10562,
			10563,
			10564,
			10565,
			10566,
			10567,
			10568,
			10569,
			10570,
			10571,
			10572,
			10573,
			10574,
			10575,
			10576,
			10577,
			10578,
			10579,
			10580,
			10581,
			10582,
			10583,
			10584,
			10585,
			10586,
			10587,
			10588,
			10589,
			10591,
			10592,
			10593,
			10594,
			10595,
			10608,
			10609,
			10610,
			10655,
			10656,
			10657,
			12768,
			12769,
			12770,
			12771,
			13032,
			13261,
			13262,
			13263,
			13264,
			13265,
			13266,
			13267,
			13268,
			13269,
			13386,
			13387,
			13388,
			13552,
			13553,
			13554,
			13728,
			13844,
			14170,
			14171,
			14172,
			14173,
			14174,
			14175,
			14176,
			14177,
			14178,
			14179,
			14180,
			14181,
			14182,
			14183,
			14184,
			14185,
			14186,
			14187,
			14188,
			14189,
			14190,
			14191,
			14192,
			14193,
			14194,
			14195,
			14196,
			14197,
			14198,
			14199,
			14200,
			14201,
			14202,
			14203,
			14204,
			14205,
			14206,
			14207,
			14208,
			14209,
			14210,
			14211,
			14212,
			14213,
			14214,
			14215,
			14216,
			14217,
			14218,
			14219,
			14220,
			14221,
			14222,
			14223,
			14224,
			14225,
			14226,
			14227,
			20353,
			20364,
			20365,
			20366,
			20367,
			20368,
			20369,
			20370,
			20371,
			22022,
			22023,
			22024,
			22025,
			22026,
			22037,
			22039,
			22040,
			22041,
			22042,
			22043,
			22044,
			22045,
			22046,
			22047,
			22048,
			22049,
			22050,
			22051,
			22052,
			22053,
			22089,
			22090,
			22091,
			22092,
			22093,
			22094,
			22095,
			22096,
			22097,
			22098,
			22099,
			22100,
			22101,
			22102,
			22103,
			22104,
			22105,
			22106,
			22107,
			22108,
			22109,
			22110,
			22111,
			22112,
			22113,
			22114,
			22115,
			22116,
			22117,
			22118,
			22119,
			22120,
			22121,
			22122,
			22123,
			22124,
			22125,
			22126,
			22127,
			22128,
			22129,
			22130,
			22131,
			22132,
			22133,
			22134,
			22135,
			22136,
			22137,
			22138,
			22139,
			22140,
			22141,
			22142,
			22143,
			22149,
			22150,
			22151,
			22152,
			22153							};

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.actor.L2Playable, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return; // prevent Class cast exception
		L2PcInstance activeChar = (L2PcInstance) playable;
		int skillId = 0;
		int skillLvl = 1;
		int itemId = item.getItemId();
		switch (itemId)
		{
		case 6403:
			skillId = 2023;
			break;
		case 6406:
			skillId = 2024;
			break;
		case 6407:
			skillId = 2025;
			break;
		case 13268:
			skillId = 2604;
			break;
		case 13269:
			skillId = 2605;
			break;
		case 20353:
			skillId = 22042;
			break;
		case 20364:
			skillId = 22045;
			break;
		case 20365:
			skillId = 22046;
			break;
		case 20366:
			skillId = 22047;
			break;
		case 20367:
			skillId = 22048;
			break;
		case 20368:
			skillId = 22049;
			break;
		case 20369:
			skillId = 22050;
			break;
		case 20370:
			skillId = 22051;
			break;
		case 20371:
			skillId = 22052;
			break;
		case 22022:
			skillId = 26022;
			break;
		case 22023:
			skillId = 26023;
			break;
		case 22024:
			skillId = 26024;
			break;
		case 22025:
			skillId = 26025;
			break;
		case 22026:
			skillId = 26026;
			break;
		case 22089:
		case 22090:
		case 22091:
		case 22092:
		case 22093:
			skillId = 26067;
			skillLvl = itemId - 22088;
			break;
		case 22094:
		case 22095:
		case 22096:
		case 22097:
		case 22098:
			skillId = 26068;
			skillLvl = itemId - 22093;
			break;
		case 22099:
		case 22100:
		case 22101:
		case 22102:
		case 22103:
			skillId = 26069;
			skillLvl = itemId - 22098;
			break;
		case 22104:
		case 22105:
		case 22106:
		case 22107:
		case 22108:
			skillId = 26070;
			skillLvl = itemId - 22103;
			break;
		case 22109:
		case 22110:
		case 22111:
		case 22112:
		case 22113:
			skillId = 26068;
			skillLvl = itemId - 22103;
			break;
		case 22114:
		case 22115:
		case 22116:
		case 22117:
		case 22118:
			skillId = 26069;
			skillLvl = itemId - 22108;
			break;
		case 22119:
		case 22120:
		case 22121:
		case 22122:
		case 22123:
			skillId = 26070;
			skillLvl = itemId - 22113;
			break;
		case 22124:
		case 22125:
		case 22126:
		case 22127:
		case 22128:
		case 22129:
		case 22130:
		case 22131:
		case 22132:
		case 22133:
		case 22134:
		case 22135:
		case 22136:
		case 22137:
		case 22138:
		case 22139:
		case 22140:
			skillId = 26071;
			skillLvl = itemId - 22123;
			break;
		case 22141:
		case 22142:
		case 22143:
			skillId = 26072;
			skillLvl = itemId - 22140;
			break;
		case 22149:
		case 22150:
		case 22151:
		case 22152:
		case 22153:
			skillId = 26073;
			skillLvl = itemId - 22148;
			break;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
		if (skill != null)
			activeChar.useMagic(skill, false, false);
	}

	/**
	 * @see com.l2jfree.gameserver.handler.IItemHandler#getItemIds()
	 */
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
