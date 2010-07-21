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
package com.l2jfree.gameserver.model.zone;

import org.w3c.dom.Node;

import com.l2jfree.gameserver.model.actor.L2Character;

/**
 * When a player is in this zone, the danger effect icon is shown.
 * 
 * @author Savormix
 * @since 2009.04.19
 */
public class L2DangerZone extends L2DynamicZone
{
	private int _hpDamage;
	private int _mpDamage;
	
	@Override
	protected void parseSettings(Node n) throws Exception
	{
		Node hpDamage = n.getAttributes().getNamedItem("hpDamage");
		Node mpDamage = n.getAttributes().getNamedItem("mpDamage");
		
		_hpDamage = (hpDamage != null) ? Integer.parseInt(hpDamage.getNodeValue()) : 0;
		_mpDamage = (mpDamage != null) ? Integer.parseInt(mpDamage.getNodeValue()) : 0;
		
		super.parseSettings(n);
	}
	
	/**
	 * <B>Get HP damage over time</B> (<I>per cycle</I>)<BR>
	 * <BR>
	 * <U>The interval is not necessarily one second</U>. Default interval is 3000 ms.
	 * 
	 * @return HP amount to be subtracted
	 * @see #getMPDamagePerSecond()
	 */
	public final int getHPDamagePerSecond()
	{
		return _hpDamage;
	}
	
	/**
	 * <B>Get MP damage over time</B> (<I>per cycle</I>)<BR>
	 * <BR>
	 * <U>The interval is not necessarily one second</U>. Default interval is 3000 ms.
	 * 
	 * @return HP amount to be subtracted
	 * @see #getHPDamagePerSecond()
	 */
	public final int getMPDamagePerSecond()
	{
		return _mpDamage;
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_DANGER, true);
		
		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_DANGER, false);
		
		super.onExit(character);
	}
}
