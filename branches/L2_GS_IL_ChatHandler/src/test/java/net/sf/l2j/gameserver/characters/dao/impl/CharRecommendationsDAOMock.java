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

package net.sf.l2j.gameserver.characters.dao.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO;
import net.sf.l2j.gameserver.characters.model.recommendation.CharRecommendation;

/**
 * A basic implementation with hashmap to avoid using a database implementation in test
 */
public class CharRecommendationsDAOMock implements ICharRecommendationsDAO
{
    private Map<Integer,Set<CharRecommendation>> recommendations = new HashMap<Integer,Set<CharRecommendation>>();
    
    public CharRecommendationsDAOMock ()
    {
        CharRecommendation charRecommendation = new CharRecommendation();
        charRecommendation.setCharId(1);
        charRecommendation.setTargetId(2);
        
        Set<CharRecommendation> recommendation = new HashSet<CharRecommendation>();
        recommendation.add(charRecommendation);
        
        recommendations.put(1, recommendation);
    }
    
    
    public void addRecommendation(int charId, int targetId)
    {
        Set<CharRecommendation> recommendation = recommendations.get(charId);
        if ( recommendation == null )
        {
            throw new RuntimeException("Unable to add recommendations");
        }
        CharRecommendation charRecommendation = new CharRecommendation();
        charRecommendation.setCharId(charId);
        charRecommendation.setTargetId(targetId);
        recommendation.add(charRecommendation);        
    }

    public Set<CharRecommendation> getRecommendations(int charId)
    {
        return recommendations.get(charId);
    }

    public void removeAllRecommendations(int charId)
    {
        Set<CharRecommendation> recommendation = recommendations.get(charId);
        if ( recommendation == null )
        {
            throw new RuntimeException("Unable to remove recommendations");
        }
        recommendations.clear();
    }
}
