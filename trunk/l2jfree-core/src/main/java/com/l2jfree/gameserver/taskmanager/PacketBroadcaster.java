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
package com.l2jfree.gameserver.taskmanager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.util.L2FastSet;

/**
 * @author NB4L1
 */
public final class PacketBroadcaster implements Runnable
{
	private static final Log _log = LogFactory.getLog(PacketBroadcaster.class);
	
	public static enum BroadcastMode
	{
		UPDATE_ABNORMAL_EFFECT {
			@Override
			protected void sendPacket(L2Character cha)
			{
				cha.updateAbnormalEffectImpl();
			}
		},
		UPDATE_EFFECT_ICONS {
			@Override
			protected void sendPacket(L2Character cha)
			{
				cha.updateEffectIconsImpl();
			}
		},
		BROADCAST_STATUS_UPDATE {
			@Override
			protected void sendPacket(L2Character cha)
			{
				cha.broadcastStatusUpdateImpl();
			}
		},
		BROADCAST_USER_INFO {
			@Override
			protected void sendPacket(L2Character cha)
			{
				if (cha instanceof L2PcInstance)
					((L2PcInstance)cha).broadcastUserInfoImpl();
			}
		},
		// TODO: more packets
		;
		
		private final byte _mask;
		
		private BroadcastMode()
		{
			_mask = (byte)(1 << ordinal());
		}
		
		public byte mask()
		{
			return _mask;
		}
		
		protected abstract void sendPacket(L2Character cha);
		
		protected final void trySendPacket(L2Character cha, byte mask)
		{
			if ((mask & mask()) == mask())
				sendPacket(cha);
		}
	}
	
	private static PacketBroadcaster _instance;
	
	public static PacketBroadcaster getInstance()
	{
		if (_instance == null)
			_instance = new PacketBroadcaster();
		
		return _instance;
	}
	
	private static final BroadcastMode[] VALUES = BroadcastMode.values();
	
	private final L2FastSet<L2Character> _set = new L2FastSet<L2Character>();
	
	private PacketBroadcaster()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 100, 100);
		
		_log.info("PacketBroadcaster: Initialized.");
	}
	
	public synchronized void add(L2Character cha)
	{
		_set.add(cha);
	}
	
	private synchronized L2Character removeFirst()
	{
		return _set.removeFirst();
	}
	
	public void run()
	{
		for (L2Character cha; (cha = removeFirst()) != null;)
			for (byte mask; (mask = cha.clearPacketBroadcastMask()) != 0;)
				for (BroadcastMode mode : VALUES)
					mode.trySendPacket(cha, mask);
	}
}
