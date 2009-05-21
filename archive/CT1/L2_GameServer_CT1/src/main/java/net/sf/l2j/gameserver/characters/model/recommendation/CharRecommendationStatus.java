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
package net.sf.l2j.gameserver.characters.model.recommendation;

import java.util.List;

import javolution.util.FastList;

public class CharRecommendationStatus
{
    
    /** The number of recommandation obtained by the L2PcInstance */
    private int _recomHave=0; // how much I was recommended by others

    /** The number of recommandation that the L2PcInstance can give */
    private int _recomLeft=0; // how many recomendations I can give to others

    /** Date when recom points were updated last time */
    private long _lastRecomUpdate;

    /** List with the recomendations that I've give */
    private List<Integer> _recomChars = new FastList<Integer>();

    /**
     * @return the _lastRecomUpdate
     */
    public long getLastRecomUpdate()
    {
        return _lastRecomUpdate;
    }

    /**
     * @param recomUpdate the _lastRecomUpdate to set
     */
    public void setLastRecomUpdate(long recomUpdate)
    {
        _lastRecomUpdate = recomUpdate;
    }

    /**
     * @return the _recomChars
     */
    public List<Integer> getRecomChars()
    {
        return _recomChars;
    }

    /**
     * @param chars the _recomChars to set
     */
    public void setRecomChars(List<Integer> chars)
    {
        _recomChars = chars;
    }

    /**
     * @return the _recomHave
     */
    public int getRecomHave()
    {
        return _recomHave;
    }

    /**
     * @param value the _recomHave to set
     */
    public void setRecomHave(int value)
    {
        if (value > 255) _recomHave = 255;
        else if (value < 0) _recomHave = 0;
        else _recomHave = value;

    }
    
    /**
     * @param have the _recomHave to add
     */
    public void incRecomHave(int have)
    {
        if (_recomHave + have <= 255)
        {
            _recomHave+= have;
        }
    }    
    
    /**
     * Increment the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
     */
    public void incRecomHave()
    {
        if (_recomHave < 255) _recomHave++;
    }    

    /**
     * @return the _recomLeft
     */
    public int getRecomLeft()
    {
        return _recomLeft;
    }

    /**
     * @param left the _recomLeft to set
     */
    public void setRecomLeft(int left)
    {
        _recomLeft = left;
    }
    
    /**
     * Increment the number of recommandation that the L2PcInstance can give.<BR><BR>
     */
    public void decRecomLeft()
    {
        if (_recomLeft > 0) _recomLeft--;
    }    

}
