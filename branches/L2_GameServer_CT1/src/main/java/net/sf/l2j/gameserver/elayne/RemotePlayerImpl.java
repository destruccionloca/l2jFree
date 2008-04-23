/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.elayne;

import java.rmi.RemoteException;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 * @author polbat02
 */
public class RemotePlayerImpl implements IRemotePlayer
{

    private L2PcInstance player;

    public RemotePlayerImpl(L2PcInstance character)
    {
	this.player = character;
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemotePlayer#getAccessLevel()
     */
    @Override
    public int getAccessLevel() throws RemoteException
    {
	return player.getAccessLevel();
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemotePlayer#getAccount()
     */
    @Override
    public String getAccount() throws RemoteException
    {
	return player.getAccountName();
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemotePlayer#getLevel()
     */
    @Override
    public int getLevel() throws RemoteException
    {
	return player.getLevel();
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemotePlayer#getName()
     */
    @Override
    public String getName() throws RemoteException
    {
	return player.getName();
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemotePlayer#getObjectId()
     */
    @Override
    public int getObjectId() throws RemoteException
    {
	return player.getObjectId();
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemotePlayer#getSex()
     */
    @Override
    public int getSex() throws RemoteException
    {
	if (player.getAppearance().getSex())
	    return 1;
	return 0;
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemotePlayer#online()
     */
    @Override
    public int online() throws RemoteException
    {
	return player.isOnline();
    }

}
