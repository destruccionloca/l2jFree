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
package com.l2jfree.gameserver.network.clientpackets;

import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.2 $ $Date: 2005/03/27 15:29:29 $
 */
public class CharacterRestore extends L2GameClientPacket
{
	private static final String _C__62_CHARACTERRESTORE = "[C] 62 CharacterRestore";
	//private final static Log _log = LogFactory.getLog(CharacterRestore.class.getName());

	// cd
	@SuppressWarnings("unused")
    private int _charSlot;

	/**
	 * @param decrypt
	 */
    @Override
    protected void readImpl()
    {
        _charSlot = readD();
    }


    @Override
    protected void runImpl()
	{
	    try 
	    {
		getClient().markRestoredChar(_charSlot);
	    } catch (Exception e){}
		CharSelectionInfo cl = new CharSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__62_CHARACTERRESTORE;
	}
}
