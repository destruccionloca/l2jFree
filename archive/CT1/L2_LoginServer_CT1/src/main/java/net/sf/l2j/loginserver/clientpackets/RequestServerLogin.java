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
package net.sf.l2j.loginserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.beans.SessionKey;
import net.sf.l2j.loginserver.manager.LoginManager;
import net.sf.l2j.loginserver.serverpackets.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.PlayFailReason;
import net.sf.l2j.loginserver.serverpackets.PlayOk;

/**
 * Fromat is ddc
 * d: first part of session id
 * d: second part of session id
 * c: server ID
 */
public class RequestServerLogin extends L2LoginClientPacket
{
    private int _skey1;
    private int _skey2;
    private int _serverId;
    
    /**
     * @return
     */
    public int getSessionKey1()
    {
        return _skey1;
    }

    /**
     * @return
     */
    public int getSessionKey2()
    {
        return _skey2;
    }

    /**
     * @return
     */
    public int getServerID()
    {
        return _serverId;
    }

    public boolean readImpl()
    {
        if (this.getAvaliableBytes() >= 9)
        {
            _skey1 = readD();
            _skey2 = readD();
            _serverId = readC();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @see com.l2jserver.mmocore.network.ReceivablePacket#run()
     */
    @Override
    public void run()
    {
        SessionKey sk = this.getClient().getSessionKey();
        
        // if we didnt showed the license we cant check these values
        if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
        {
            if (LoginManager.getInstance().isLoginPossible(this.getClient().getAccessLevel(), _serverId))
            {
                this.getClient().setJoinedGS(true); 
                this.getClient().sendPacket(new PlayOk(sk));
            }
            else
            {
                this.getClient().close(PlayFailReason.REASON_TOO_MANY_PLAYERS);
            }
        }
        else
        {
            this.getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
        }
    }
}
