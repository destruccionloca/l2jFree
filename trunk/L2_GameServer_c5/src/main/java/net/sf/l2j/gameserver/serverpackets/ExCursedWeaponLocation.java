package net.sf.l2j.gameserver.serverpackets;

import javolution.util.FastList;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * Format: (ch) d[ddddd]
 *
 * @author  -Wooden-
 */
public class ExCursedWeaponLocation extends ServerBasePacket
{
	private static final String _S__FE_46_EXCURSEDWEAPONLOCATION = "[S] FE:46 ExCursedWeaponLocation";
	private FastList<CursedWeaponInfo> _cursedWeaponInfo;
	
	public ExCursedWeaponLocation(FastList<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
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
		writeH(0x46);
		
		writeD(_cursedWeaponInfo.size());
		for(CursedWeaponInfo w : _cursedWeaponInfo)
		{
			writeD(w.id);
			writeD(w.unk);
			
			writeD(w.pos.getX());
			writeD(w.pos.getY());
			writeD(w.pos.getZ());
		}
	}

	/**
	 * @see net.sf.l2j.gameserver.network.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_46_EXCURSEDWEAPONLOCATION;
	}
	
	public static class CursedWeaponInfo
	{
		public Point3D pos;
		public int id;
		public int unk;
		
		public CursedWeaponInfo(Point3D p, int ID)
		{
			pos = p;
			id = ID;
			unk = 1;
		}
		
	}
}
