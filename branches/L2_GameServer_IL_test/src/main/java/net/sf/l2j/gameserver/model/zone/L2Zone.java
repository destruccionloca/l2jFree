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
package net.sf.l2j.gameserver.model.zone;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.BaseEntity;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneSettings;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * Abstract base class for any zone type
 * Handles basic operations
 *
 * @author  durgus
 */
public abstract class L2Zone
{
	protected int _id;
	protected L2ZoneForm _form;
	protected ZoneSettings _set;
	protected FastList<L2Character> _characterList;
	protected BaseEntity _entity;
	
	/** Parameters to affect specific characters */
	private boolean _checkAffected;
	
	private int _minLvl;
	private int _maxLvl;
	private int[] _race;
	private int[] _class;
	private char _classType;
	
	private FastMap<RestartType, FastList<Point3D> > _restarts;
	
	protected L2Zone(ZoneSettings set)
	{
		_set = set;
		_characterList = new FastList<L2Character>();
	}

	/**
	 * Set the zone for this L2Zone Instance
	 * @param zone
	 */
	public void setForm(L2ZoneForm form)
	{
		_form = form;
	}
	
	/**
	 * Returns this zones zone form
	 * @param zone
	 * @return
	 */
	public L2ZoneForm getForm()
	{
		return _form;
	}

	public void setEntity(BaseEntity entity)
	{
		_entity = entity;
	}
	
	public BaseEntity getEntity()
	{
		return _entity;
	}

	/**
	 * Set the settings for this zone
	 * @param settings
	 */
	public void setSettings(ZoneSettings settings)
	{
		_set = settings;
	}
	
	/**
	 * Returns the settings for this zone
	 * @param zone
	 * @return
	 */
	public ZoneSettings getSettings()
	{
		return _set;
	}
	
	/**
	 * Checks if the given coordinates are within the zone
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _form.isInsideZone(x, y, z);
	}
	
	/**
	 * Checks if the given obejct is inside the zone.
	 * 
	 * @param object
	 */
	public boolean isInsideZone(L2Object object)
	{
		return _form.isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public double getDistanceToZone(int x, int y)
	{
		return _form.getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(L2Object object)
	{
		return _form.getDistanceToZone(object.getX(), object.getY());
	}

	public void addRestartPoint(RestartType restartType, Point3D point)
	{
		if (_restarts == null)
			_restarts = new FastMap<RestartType, FastList<Point3D>>();
		
		if (_restarts.get(restartType) == null)
			_restarts.put(restartType, new FastList<Point3D>());
		
		_restarts.get(restartType).add(point);
	}

	public Location getRestartPoint(RestartType restartType)
	{
		if (restartType == RestartType.RestartRandom)
			return _form.getRandomLocation();
		else if (_restarts != null)
		{
			if (_restarts.get(restartType) != null)
			{
				Point3D point = _restarts.get(restartType).get(Rnd.nextInt(_restarts.get(restartType).size()));
				return new Location(point.getX(), point.getY(), point.getZ());
			}
		}
		return null;
	}

	public Location getSpawnLoc()
	{
		return getRestartPoint(RestartType.RestartNormal);
	}
	
	public void revalidateInZone(L2Character character)
	{
		// If the object is inside the zone...
		if (_form.isInsideZone(character.getX(), character.getY(), character.getZ()))
		{
			// Was the character not yet inside this zone?
			if (!_characterList.contains(character))
			{
				_characterList.add(character);
				onEnter(character);
			}
		}
		else
		{
			// Was the character inside this zone?
			if (_characterList.contains(character))
			{
				_characterList.remove(character);
				onExit(character);
			}
		}
	}
	
	/**
	 * Force fully removes a character from the zone
	 * Should use during teleport / logoff
	 * @param character
	 */
	public void removeCharacter(L2Character character)
	{
		if (_characterList.contains(character))
		{
			_characterList.remove(character);
			onExit(character);
		}
	}
	
	
	/**
	 * Will scan the zones char list for the character
	 * @param character
	 * @return
	 */
	public boolean isCharacterInZone(L2Character character)
	{
		return _characterList.contains(character);
	}
	
	protected abstract void onEnter(L2Character character);
	protected abstract void onExit(L2Character character);
}
