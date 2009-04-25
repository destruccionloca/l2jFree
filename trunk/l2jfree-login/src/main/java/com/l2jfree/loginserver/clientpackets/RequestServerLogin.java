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
package com.l2jfree.loginserver.clientpackets;

import com.l2jfree.Config;
import com.l2jfree.loginserver.beans.SessionKey;
import com.l2jfree.loginserver.manager.LoginManager;
import com.l2jfree.loginserver.serverpackets.LoginFail;
import com.l2jfree.loginserver.serverpackets.PlayOk;
import com.l2jfree.loginserver.services.exception.MaintenanceException;
import com.l2jfree.loginserver.services.exception.MaturityException;

/**
 * Fromat is ddc
 * d: first part of session id
 * d: second part of session id
 * c: server ID
 */
public class RequestServerLogin extends L2LoginClientPacket
{
	private int	_skey1;
	private int	_skey2;
	private int	_serverId;

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

	@Override
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

		// if we didn't show the license we can't check these values
		if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			try
			{
				if (LoginManager.getInstance().isLoginPossible(getClient().getAge(), getClient().getAccessLevel(), _serverId))
				{
					this.getClient().setJoinedGS(true);
					this.getClient().sendPacket(new PlayOk(sk));
					LoginManager.getInstance().setAccountLastServerId(getClient().getAccount(), _serverId);
				}
				else
				{
					this.getClient().closeLoginGame(LoginFail.REASON_TOO_HIGH_TRAFFIC);
				}
			}
			catch (MaintenanceException e)
			{
				this.getClient().closeLoginGame(LoginFail.REASON_MAINTENANCE_UNDERGOING);
			}
			catch (MaturityException e)
			{
				this.getClient().closeLoginGame(LoginFail.REASON_AGE_LIMITATION);
			}
		}
		else
		{
			this.getClient().closeLogin(LoginFail.REASON_ACCESS_FAILED_TRY_AGAIN);
		}
	}
}
