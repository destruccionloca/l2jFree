/**
 * 
 */
package net.sf.l2j.gameserver.network.serverpackets;

/**
 * @author Administrator
 *
 */
public class ExShowScreenMessage extends L2GameServerPacket
{
	private String _text;
	private int _time;
	private int _color = 0xaadd77;
	
	public ExShowScreenMessage (String text, int time)
	{
		_text = text;
		_time = time;
	}
	
	public ExShowScreenMessage (String text, int time, int color)
	{
		_text = text;
		_time = time;
		_color = color;
	}
	
	@Override
	public String getType()
	{
		return "ExShowScreenMessage";
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x39);
		
		writeD(0x00);						// type
		writeD(-1);							// num ?
		writeD(0x00);						// window type
		writeD(0x0a);						// font size
		writeD(0x00);						// font type
		writeD(_color);
		
		writeD(0);
		writeD(0);
		
		writeD(_time);
		
		writeD(1);
		
		writeS(_text);
	}

}
