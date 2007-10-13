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

import net.sf.l2j.gameserver.characters.dao.impl.CharRecommendationsDAOMock;
import net.sf.l2j.gameserver.characters.model.recommendation.CharRecommendationStatus;
import junit.framework.TestCase;

public class TestCharRecommendationService extends TestCase
{
    CharRecommendationService charRecommendationService = new CharRecommendationService();
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        charRecommendationService.setCharRecommendationsDAO(new CharRecommendationsDAOMock());
    }
    
    public void testInitCharRecommendations ()
    {
        CharRecommendationStatus charRecommendationStatus = new CharRecommendationStatus();
        charRecommendationStatus.setRecomHave(10);
        charRecommendationService.initCharRecommendationStatus(new Byte("19").byteValue(), charRecommendationStatus);
        assertEquals(3, charRecommendationStatus.getRecomLeft());
        assertEquals(9, charRecommendationStatus.getRecomHave());
        charRecommendationService.initCharRecommendationStatus(new Byte("39").byteValue(), charRecommendationStatus);
        assertEquals(6, charRecommendationStatus.getRecomLeft());
        assertEquals(7, charRecommendationStatus.getRecomHave());
        charRecommendationService.initCharRecommendationStatus(new Byte("59").byteValue(), charRecommendationStatus);
        assertEquals(9, charRecommendationStatus.getRecomLeft());
        assertEquals(5, charRecommendationStatus.getRecomHave());
        
        assertTrue(charRecommendationStatus.getLastRecomUpdate()!=0);
    }
    
    public void testInitCharRecommendationsWithRecomHaveLowerThanZero ()
    {
        CharRecommendationStatus charRecommendationStatus = new CharRecommendationStatus();
        charRecommendationStatus.setRecomHave(0);
        charRecommendationService.initCharRecommendationStatus(new Byte("39").byteValue(), charRecommendationStatus);
        assertEquals(6, charRecommendationStatus.getRecomLeft());
        assertEquals(0, charRecommendationStatus.getRecomHave());
    }    
    
    public void testNeedToRestartRecom ()
    {
        long recomUpdate = Calendar.getInstance().getTimeInMillis();
        CharRecommendationStatus charRecommendationStatus = new CharRecommendationStatus();
        charRecommendationStatus.setRecomHave(0);
        charRecommendationStatus.setLastRecomUpdate(recomUpdate);
        
        assertFalse (charRecommendationService.needToResetRecommendations(charRecommendationStatus, new Byte("9").byteValue()));
        Calendar datePassed = Calendar.getInstance();
        datePassed.setTimeInMillis(recomUpdate);
        datePassed.add(Calendar.DAY_OF_MONTH,-2);
        charRecommendationStatus.setLastRecomUpdate(datePassed.getTimeInMillis());
        assertTrue (charRecommendationService.needToResetRecommendations(charRecommendationStatus, new Byte("19").byteValue()));
    }        
    
}
