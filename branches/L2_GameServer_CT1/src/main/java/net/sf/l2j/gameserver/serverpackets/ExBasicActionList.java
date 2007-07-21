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

/**
 * Format: chd (d)
 * @author  evill33t
 */
public class ExBasicActionList extends L2GameServerPacket
{
	private static final String _S__FE_5E_EXBASICACTIONLIST = "";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x5e);
		writeD(0x22); // length
		
		// actions
		writeD(0x00); //sitstand
		writeD(0x01); //walkrun
		writeD(0x02); //attack
		writeD(0x03); //trade
		writeD(0x04); //targetnext
		writeD(0x05); //Pickup
		writeD(0x06); //assist
		writeD(0x0a); // vendor
		writeD(0x1c); //buy
		writeD(0x28); //recommend
		writeD(0x25); //manufacture
		writeD(0x37); //recstartstop
		writeD(0x39); //storefind
		writeD(0x3a); //challenge
		writeD(0x3b); //cancelchallenge
		// 38mountdismount
		// 51manufacture2

		// party
		writeD(0x07); //partyinvite
		writeD(0x08); //partyleave
		writeD(0x09); //partydismiss
		writeD(0x0b); //partymatching
		writeD(0x32); //leaderchange
		writeD(0x38); //invitechannel
		writeD(0x3c); //partychallenge

		// social
		writeD(0x0c); //socialhello
		writeD(0x0d); //socialvictory
		writeD(0x0e); //socialcharge
		writeD(0x18); //socialyes
		writeD(0x19); //socialno
		writeD(0x1a); //socialbow
		writeD(0x1d); //socialunaware
		writeD(0x1e); //socialwaiting
		writeD(0x1f); //sociallaugh
		writeD(0x21); //socialapplause
		writeD(0x22); //socialdance
		writeD(0x23); //socialsad
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_5E_EXBASICACTIONLIST;
	}

}
