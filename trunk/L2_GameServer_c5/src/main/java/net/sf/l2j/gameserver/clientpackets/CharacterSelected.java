/*
 * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.CharSelected;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelected extends ClientBasePacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
	private final static Log _log = LogFactory.getLog(CharacterSelected.class.getName());

	// cd
	private final int _charSlot;
	
	@SuppressWarnings("unused")
	private final int _unk1; 	// new in C4
	@SuppressWarnings("unused")
	private final int _unk2;	// new in C4
	@SuppressWarnings("unused")
	private final int _unk3;	// new in C4
	@SuppressWarnings("unused")
	private final int _unk4;	// new in C4

	/**
	 * @param decrypt
	 */
	public CharacterSelected(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}

	void runImpl()
	{
		// if there is a playback.dat file in the current directory, it will
		// be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to
		// be a  [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		//playLogFile(getConnection()); // try to play log file

		// HAVE TO CREATE THE L2PCINSTANCE HERE TO SET AS ACTIVE
		if (_log.isDebugEnabled()) _log.debug("selected slot:" + _charSlot);

        if(!getClient().getAccountName(_charSlot).equalsIgnoreCase(getClient().getLoginName()))
        {
            _log.fatal("HACKER: Account " + getClient().getLoginName() + " tried to login with char of account "+getClient().getAccountName(_charSlot));
            getClient().getConnection().close();
        }
        
		//loadup character from disk
		L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);
		if(cha == null)
		{
			_log.warn("Character could not be loaded (slot:"+_charSlot+")");
			sendPacket(new ActionFailed());
			return;
		}
        
		getClient().setActiveChar(cha);
        
		if(cha.getAccessLevel() < -1)
		{
			// Do not store chracter data...
			cha.deleteMe();
			return;
		}
		//weird but usefull, will send i..
		//cha.setAccessLevel(cha.getAccessLevel());
		CharSelected cs = new CharSelected(cha, getClient().getSessionId().playOkID1);
		sendPacket(cs);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__0D_CHARACTERSELECTED;
	}	
}
