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

package net.sf.l2j.gameserver.characters.service;

import java.util.Calendar;
import java.util.Set;

import net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO;
import net.sf.l2j.gameserver.characters.model.recommendation.CharRecommendation;
import net.sf.l2j.gameserver.characters.model.recommendation.CharRecommendationStatus;


/**
 * Service used to manage recommendations
 * 
 * Recommendations are give from a player to another player.
 * A player can give only a limited amount of recommendations based on his level.
 * 
 */
public class CharRecommendationService
{
    /**
     * DAO for recommendations
     */
    private ICharRecommendationsDAO __iCharRecommendationsDAO = null;
    
    /**
     * Setter
     * @param iCharRecommendationsDAO to set
     */
    public void setCharRecommendationsDAO(ICharRecommendationsDAO iCharRecommendationsDAO)
    {
        __iCharRecommendationsDAO = iCharRecommendationsDAO;
    }

    /**
     * @param charId
     * @param targetId
     * @see net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO#addRecommendation(int, int)
     */
    public void addRecommendation(int charId, int targetId)
    {
        __iCharRecommendationsDAO.addRecommendation(charId, targetId);
    }

    /**
     * @param charId
     * @return
     * @see net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO#getRecommendations(int)
     */
    public Set<CharRecommendation> getRecommendations(int charId)
    {
        return __iCharRecommendationsDAO.getRecommendations(charId);
    }

    /**
     * @param charId
     * @see net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO#removeAllRecommendations(int)
     */
    public void removeAllRecommendations(int charId)
    {
        __iCharRecommendationsDAO.removeAllRecommendations(charId);
    }
    
    /**
     * Initialize character recommendations
     * If level is lower than 20, the character can give only 3 recommendations
     * If level is lower than 40, the character can give only 6 recommendations
     * Else, the character can give only 6 recommendations
     * Update last recom update
     * 
     * @param level
     * @param charRecommendationStatus
     */
    public void initCharRecommendationStatus(byte level, CharRecommendationStatus charRecommendationStatus)
    {
        if (level < 20) 
        {
            charRecommendationStatus.setRecomLeft(3);
            charRecommendationStatus.incRecomHave(-1);
        }
        else if (level < 40) 
        {
            charRecommendationStatus.setRecomLeft(6);
            charRecommendationStatus.incRecomHave(-2);
        }
        else
        {
            charRecommendationStatus.setRecomLeft(9);
            charRecommendationStatus.incRecomHave(-2);
        }
        if (charRecommendationStatus.getRecomHave() < 0)
        {
            charRecommendationStatus.setRecomHave(0);
        }
        
        // If we have to update last update time, but it's now before 13, we should set it to yesterday
        Calendar update = Calendar.getInstance();
        if(update.get(Calendar.HOUR_OF_DAY) < 13) update.add(Calendar.DAY_OF_MONTH,-1);
        update.set(Calendar.HOUR_OF_DAY,13);
        charRecommendationStatus.setLastRecomUpdate( update.getTimeInMillis());
    }

    /**
     * Check if there is a need to reload recommendations
     * 
     * @param charRecomStatus
     * @param level
     * @return true if level > 10 && last recom update + 1 month < now
     */
    public boolean needToResetRecommendations(CharRecommendationStatus charRecomStatus, byte level)
    {
        Calendar check = Calendar.getInstance();
        check.setTimeInMillis(charRecomStatus.getLastRecomUpdate());
        check.add(Calendar.DAY_OF_MONTH,1);
        
        Calendar now = Calendar.getInstance();
        
        if (level < 10 || check.after(now) )
        {
            return false;
        }
        return true;
    }
    

    
    
}
