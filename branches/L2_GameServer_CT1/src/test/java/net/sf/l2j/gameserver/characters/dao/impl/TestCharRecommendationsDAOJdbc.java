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
package net.sf.l2j.gameserver.characters.dao.impl;

import java.util.Set;

import net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO;
import net.sf.l2j.gameserver.characters.model.recommendation.CharRecommendation;
import net.sf.l2j.tools.L2Registry;
import net.sf.l2j.tools.db.spring.ADAOTestWithSpringAndDerby;

public class TestCharRecommendationsDAOJdbc extends ADAOTestWithSpringAndDerby
{
    
    /**
     * DAO to test
     */
    private ICharRecommendationsDAO __charRecommendationsDAOJdbc = null;


    /**
     * @return the __charRecommendationsDAOJdbc
     */
    public ICharRecommendationsDAO getCharRecommendationsDAOJdbc()
    {
        return __charRecommendationsDAOJdbc;
    }

    /**
     * @param recommendationsDAOJdbc the __charRecommendationsDAOJdbc to set
     */
    public void setCharRecommendationsDAOJdbc(CharRecommendationsDAOJdbc recommendationsDAOJdbc)
    {
        __charRecommendationsDAOJdbc = recommendationsDAOJdbc;
    }
    
    public void setUp() throws Exception
    {
        super.setUp();
        __charRecommendationsDAOJdbc = (ICharRecommendationsDAO)getBean("CharRecommendationsDAO");
        
        // o Init the registry
        // In this test, we replace the L2Registry by the registry loaded in the test 
        // ------------------
        L2Registry.getInstance();
        L2Registry.setApplicationContext(__applicationContext);

        assertNotNull(__charRecommendationsDAOJdbc);
    }
    
    
    
    public void testGetAllRecommendations ()
    {
        Set<CharRecommendation> recommendations = __charRecommendationsDAOJdbc.getRecommendations(268666622);
        assertEquals(2,recommendations.size());
    }
    
    public void testRemoveRecommendations ()
    {
        __charRecommendationsDAOJdbc.removeAllRecommendations(268666622);
        Set<CharRecommendation> recommendations = __charRecommendationsDAOJdbc.getRecommendations(268666622);
        assertEquals(0,recommendations.size());
    }
    
    public void testAddRecommendations ()
    {
        __charRecommendationsDAOJdbc.addRecommendation(268666622, 268526842);
        Set<CharRecommendation> recommendations = __charRecommendationsDAOJdbc.getRecommendations(268666622);
        assertEquals(3,recommendations.size());
    }    

    @Override
    public String getDtdName()
    {
        return "/l2jdb.dtd";
    }

    @Override
    public String getInitialDataSetName()
    {
        return "net/sf/l2j/gameserver/characters/dao/impl/char_recommendations.xml";
    }

    @Override
    public String getRootDirName()
    {
        return null;
    }

}
