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

import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.Die;
import com.l2jfree.tools.random.Rnd;

/**
 * Handles all the vitamin (dimensional/BR_products) item packs since
 * they do not have any associated skills.
 * @author savormix
 */
public class WrappedPack implements IItemHandler
{
	private static final String LOG_PROCESS = "Vitamin Pack";
	private static final int[] WRAPPED_PACK_IDS = {
		13079, 13080, 13081, 13082, 13083, 13084, 13085, 13086, 13087, 13088, 13089, 13090,
		13091, 13092, 13093, 13094, 13095, 13096, 13097, 13098, 13103, 13104, 13105, 13106,
		13107, 13108, 13109, 13110, 13111, 13112, 13113, 13114, 13115, 13116, 13117, 13118,
		13119, 13120, 13121, 13122, 13123, 13124, 13125, 13126, 13225, 13226, 13227, 13228,
		13229, 13230, 13231, 13232, 13233, 13256, 13257, 13274, 13275, 13276, 13279, 13280,
		13281, 13282, 13283, 13284, 13285, 13286, 13287, 13288, 13289, 13290, 13291, 13292,
		13341, 13342, 13343, 13344, 13345, 13346, 13347, 13348, 13349, 13350, 13351, 13352,
		13353, 13354, 13355, 13356, 13357, 13358, 13359, 13360, 13361, 13362, 13363, 13364,
		13365, 13366, 13367, 13368, 13369, 13370, 13371, 13372, 13373, 13374, 13375, 13376,
		13377, 13378, 13379, 13380, 13381, 13384, 13385
	};

	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance) || item == null)
			return;

		L2PcInstance player = playable.getActingPlayer();
		if (!player.destroyItemByItemId("Consume", item.getItemId(), 1, player, true))
			return;

		int itemId = 0;
		int count = 1;
		switch (item.getItemId())
		{
		case 13079:
		case 13103:
		case 13123:
			itemId = 13015;
			break;
		case 13080:
		case 13104:
			itemId = 13016;
			count = 30;
			break;
		case 13081:
		case 13105:
			itemId = 13010;
			player.addItem(LOG_PROCESS, 13011, count, item, true);
			player.addItem(LOG_PROCESS, 13012, count, item, true);
			break;
		case 13082:
		case 13106:
			itemId = Die.FEATHER_OF_BLESSING_1;
			count = 3;
			break;
		case 13083:
		case 13107:
			itemId = 13023;
			break;
		case 13084:
		case 13108:
		case 13345:
			itemId = 12800;
			break;
		case 13085:
		case 13109:
			itemId = 13010;
			count = 5;
			player.addItem(LOG_PROCESS, 13011, count, item, true);
			player.addItem(LOG_PROCESS, 13012, count, item, true);
			break;
		case 13086:
		case 13110:
			itemId = 12782;
			break;
		case 13087:
		case 13111:
			itemId = 12783;
			break;
		case 13088:
		case 13112:
			itemId = 12786;
			break;
		case 13089:
		case 13113:
			itemId = 12787;
			break;
		case 13090:
		case 13114:
			itemId = 12788;
			break;
		case 13091:
		case 13115:
			itemId = 12789;
			break;
		case 13092:
		case 13116:
			itemId = 12790;
			break;
		case 13093:
		case 13117:
			itemId = 12791;
			break;
		case 13094:
		case 13118:
			itemId = 12792;
			break;
		case 13095:
		case 13119:
			itemId = Rnd.get(12793, 12794);
			break;
		case 13096:
		case 13120:
			itemId = Rnd.get(12795, 12797);
			break;
		case 13097:
		case 13121:
			itemId = 13022;
			break;
		case 13098:
		case 13124:
			itemId = 13021;
			break;
		case 13122:
			itemId = 13021;
			count = 3;
			break;
		case 13125: // Verified to be Scrolls
			itemId = 13016;
			count = 10;
			break;
		case 13126:
			itemId = 13016;
			player.addItem(LOG_PROCESS, 13015, count, item, true);
			player.addItem(LOG_PROCESS, 13021, count, item, true);
			count = 10;
			break;
		case 13225:
		case 13228:
			itemId = 13010;
			count = 15;
			break;
		case 13226:
		case 13229:
			itemId = 13011;
			count = 15;
			break;
		case 13227:
		case 13230:
			itemId = 13012;
			count = 15;
			break;
		case 13231:
			itemId = 13010;
			count = 5;
			break;
		case 13232:
			itemId = 13011;
			count = 5;
			break;
		case 13233:
			itemId = 13012;
			count = 5;
			break;
		case 13256:
			itemId = 13027;
			break;
		case 13257:
			itemId = 13382;
			break;
		case 13274: // Verified to be 5 hours
		case 13275:
		case 13276:
			itemId = 13273;
			break;
		case 13279:
			itemId = Die.FEATHER_OF_BLESSING_1;
			break;
		case 13280:
			itemId = 13254;
			break;
		case 13281:
			itemId = 13253;
			break;
		case 13282:
			itemId = 13239;
			break;
		case 13283:
			itemId = 13240;
			break;
		case 13284:
			itemId = 13241;
			break;
		case 13285:
			itemId = 13242;
			break;
		case 13286:
			itemId = 13243;
			break;
		case 13287:
			itemId = 13244;
			break;
		case 13288:
			itemId = 13245;
			break;
		case 13289:
			itemId = 13246;
			break;
		case 13290:
			itemId = 13247;
			break;
		case 13291:
			itemId = Rnd.get(13248, 13249);
			break;
		case 13292:
			itemId = Rnd.get(13250, 13252);
			break;
		case 13341: // Verified to be book
			itemId = 13301;
			break;
		case 13342: // Verified to be scrolls
			itemId = 13302;
			count = 30;
			break;
		case 13343:
			itemId = Die.FEATHER_OF_BLESSING_2;
			count = 3;
			break;
		case 13344:
			itemId = 13309;
			break;
		case 13346:
			itemId = 13297;
			count = 5;
			player.addItem(LOG_PROCESS, 13298, count, item, true);
			player.addItem(LOG_PROCESS, 13299, count, item, true);
			break;
		case 13347:
			itemId = 13310;
			break;
		case 13348:
			itemId = 13311;
			break;
		case 13349:
			itemId = 13312;
			break;
		case 13350:
			itemId = 13313;
			break;
		case 13351:
			itemId = 13314;
			break;
		case 13352:
			itemId = 13315;
			break;
		case 13353:
			itemId = 13316;
			break;
		case 13354:
			itemId = 13317;
			break;
		case 13355:
			itemId = 13318;
			break;
		case 13356:
			itemId = Rnd.get(13319, 13320);
			break;
		case 13357:
			itemId = Rnd.get(13321, 13323);
			break;
		case 13358:
			itemId = 13308;
			break;
		case 13359:
			itemId = 13307;
			count = 3;
			break;
		case 13360:
			itemId = 13307;
			break;
		case 13361:
			itemId = 13302;
			count = 10;
			break;
		case 13362:
			itemId = 13297;
			count = 15;
			break;
		case 13363:
			itemId = 13298;
			count = 15;
			break;
		case 13364:
			itemId = 13299;
			count = 15;
			break;
		case 13365:
			itemId = 13297;
			count = 5;
			break;
		case 13366:
			itemId = 13298;
			count = 5;
			break;
		case 13367:
			itemId = 13299;
			count = 5;
			break;
		case 13368:
			itemId = Die.FEATHER_OF_BLESSING_2;
			break;
		case 13369:
			itemId = 13340;
			break;
		case 13370:
			itemId = 13339;
			break;
		case 13371:
			itemId = 13325;
			break;
		case 13372:
			itemId = 13326;
			break;
		case 13373:
			itemId = 13327;
			break;
		case 13374:
			itemId = 13328;
			break;
		case 13375:
			itemId = 13329;
			break;
		case 13376:
			itemId = 13330;
			break;
		case 13377:
			itemId = 13331;
			break;
		case 13378:
			itemId = 13332;
			break;
		case 13379:
			itemId = 13333;
			break;
		case 13380:
			itemId = Rnd.get(13334, 13335);
			break;
		case 13381:
			itemId = Rnd.get(13336, 13338);
			break;
		case 13384:
		case 13385:
			itemId = 13383;
			break;
		default:
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			_log.warn("Missing handling for item; ID=" + item.getItemId());
			return;
		}

		if (itemId > 0)
			player.addItem(LOG_PROCESS, itemId, count, item, true);
		else
			_log.warn("Invalid handling for item; ID=" + item.getItemId());
	}

	@Override
	public int[] getItemIds()
	{
		return WRAPPED_PACK_IDS;
	}
}
