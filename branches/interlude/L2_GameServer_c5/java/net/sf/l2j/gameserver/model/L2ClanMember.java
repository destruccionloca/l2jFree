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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2ClanMember
{
	private int _objectId;
	private String _name;
	private int _level;
	private int _classId;
    private int _pledgeType;
    private int _rank;
    private String _apprentice;
	private L2PcInstance _player;
	
	public L2ClanMember(String name, int level, int classId, int objectId, int pledgeType, int rank, String apprentice)
	{
		_name = name;
		_level = level;
		_classId = classId;
		_objectId = objectId;
        _pledgeType = pledgeType;
        _rank = rank;
        _apprentice = apprentice;
	}
	
	public L2ClanMember(L2PcInstance player)
	{
		_player = player;
	}

		
	public void setPlayerInstance(L2PcInstance player)
	{
		if (player == null && _player != null)
		{
			// this is here to keep the data when the player logs off
			_name = _player.getName();
			_level = _player.getLevel();
			_classId = _player.getClassId().getId();
			_objectId = _player.getObjectId();
            _pledgeType = _player.getPledgeType();
            _rank = _player.getRank();
		}

		_player = player;
	}

	public L2PcInstance getPlayerInstance()
	{
		return _player;
	}
	
	public boolean isOnline()
	{
		return _player != null;
	}
	
	/**
	 * @return Returns the classId.
	 */
	public int getClassId()
	{
		if (_player != null)
		{
			return _player.getClassId().getId();
		}
        return _classId;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		if (_player != null)
		{
			return _player.getLevel();
		}
        return _level;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		if (_player != null)
		{
			return _player.getName();
		}
        return _name;
	}

	/**
	 * @return Returns the objectId.
	 */
	public int getObjectId()
	{
		if (_player != null)
		{
			return _player.getObjectId();
		}
        return _objectId;
	}
	
	public String getTitle()
	{
	    if (_player != null)
	    {
	        return _player.getTitle();
	    }
	    return " ";
	}
    public int getPledgeType()
    {
        if (_player != null)
        {
            return _player.getPledgeType();
        }
        return _pledgeType;
    }
    public int getRank()
    {
        if (_player != null)
        {
            return _player.getRank();
        }
        return _pledgeType;
    }
    public String getApprentice()
    {
        if (_player != null)
        {
            return _player.getApprentice();
        }
        return _apprentice;
    }
    public void setRank(int rank)
    {
        _rank = rank;
        updatePledgeRank();
    }
    public void updatePledgeRank()
    {
        java.sql.Connection con = null;
        
        try 
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET pledge_rank=? WHERE obj_id=?");
            statement.setInt(1, _rank);
            statement.setInt(2, getObjectId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            //_log.warning("could not set char power_grade:"+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

}
