package net.sf.l2j.gameserver.serverpackets;

import javolution.util.FastList;

/**
 * Format: (ch) d[d]
 *
 * @author  -Wooden-
 */
public class ExCursedWeaponList extends ServerBasePacket
{
	private static final String _S__FE_45_EXCURSEDWEAPONLIST = "[S] FE:45 ExCursedWeaponList";
	private FastList<Integer> _cursedWeaponIds;
	
	public ExCursedWeaponList(FastList<Integer> cursedWeaponIds)
	{
		_cursedWeaponIds = cursedWeaponIds;
	}
	
	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		// no long running task		
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x45);
		
		writeD(_cursedWeaponIds.size());
		for(Integer i : _cursedWeaponIds)
		{
			writeD(i.intValue());
		}
	}

	/**
	 * @see net.sf.l2j.gameserver.network.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_45_EXCURSEDWEAPONLIST;
	}
	
}
