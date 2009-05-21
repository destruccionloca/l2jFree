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
package net.sf.l2j.gameserver.characters.dao;

import java.util.Set;

import net.sf.l2j.gameserver.characters.model.recommendation.CharRecommendation;

/**
 * DAO to manipulate recommendations
 * 
 */
public interface ICharRecommendationsDAO
{
    /**
     * Return all recommendations for player with a specified charId
     * @param charId
     * @return set of recommendations
     */
    public Set<CharRecommendation> getRecommendations(int charId);
    
    /**
     * Add a recommendation for charId to targetId 
     * @param charId
     * @param targetId
     */
    public void addRecommendation(int charId, int targetId);
    
    /**
     * remove all recommendations of the character 
     * @param charId
     */
    public void removeAllRecommendations(int charId);
}
