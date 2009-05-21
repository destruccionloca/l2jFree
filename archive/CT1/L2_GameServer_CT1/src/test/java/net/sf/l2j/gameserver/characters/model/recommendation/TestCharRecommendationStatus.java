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

import junit.framework.TestCase;

public class TestCharRecommendationStatus extends TestCase
{
    public void testInitCharRecomStatus ()
    {
        CharRecommendationStatus charRecommendationStatus = new CharRecommendationStatus();
        assertEquals(0, charRecommendationStatus.getRecomChars().size());
        assertEquals(0, charRecommendationStatus.getRecomHave());
        assertEquals(0, charRecommendationStatus.getRecomLeft());
    }
    
    public void testAddRecom ()
    {
        CharRecommendationStatus charRecommendationStatus = new CharRecommendationStatus();
        charRecommendationStatus.incRecomHave();
        assertEquals(1, charRecommendationStatus.getRecomHave());
        charRecommendationStatus.incRecomHave(255);
        assertEquals(1, charRecommendationStatus.getRecomHave());
        charRecommendationStatus.incRecomHave(254);
        assertEquals(255, charRecommendationStatus.getRecomHave());
        charRecommendationStatus.setRecomHave(256);
        assertEquals(255, charRecommendationStatus.getRecomHave());
        charRecommendationStatus.setRecomHave(-1);
        assertEquals(0, charRecommendationStatus.getRecomHave());
    } 
    
    public void testDecRecomLeft ()
    {
        CharRecommendationStatus charRecommendationStatus = new CharRecommendationStatus();
        charRecommendationStatus.decRecomLeft();
        assertEquals(0, charRecommendationStatus.getRecomLeft());
        charRecommendationStatus.setRecomLeft(5);
        assertEquals(5, charRecommendationStatus.getRecomLeft());
        charRecommendationStatus.decRecomLeft();
        assertEquals(4, charRecommendationStatus.getRecomLeft());
    }    
        
    
    
}
