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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;

import javolution.util.FastSet;
import net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO;
import net.sf.l2j.gameserver.characters.model.recommendation.CharRecommendation;
import net.sf.l2j.tools.dao.impl.BaseRootDAOJdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Jdbc Implementation for char recommendation 
 *
 */
public class CharRecommendationsDAOJdbc extends BaseRootDAOJdbc implements ICharRecommendationsDAO
{
    private static final Log _log = LogFactory.getLog(CharRecommendationsDAOJdbc.class);
    
    private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
    private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
    private static final String DELETE_CHAR_RECOMS = "DELETE FROM character_recommends WHERE char_id=?";
    

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO#addRecommendation(int, int)
     */
    public void addRecommendation(int charId, int targetId)
    {
        Connection con = null;
        try
        {
            con = getConnection(con);
            PreparedStatement statement = con.prepareStatement(ADD_CHAR_RECOM);
            statement.setInt(1, charId);
            statement.setInt(2, targetId);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("could not update char recommendations:"+e);
        }
        finally
        {
            closeConnectionQuietly(con);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO#getRecommendations(int)
     */
    public Set<CharRecommendation> getRecommendations(int charId)
    {
        Connection con = null;
        Set<CharRecommendation> recommendations = new FastSet<CharRecommendation>();
        try
        {
            con = getConnection(con);
            PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_RECOMS);
            statement.setInt(1, charId);
            ResultSet rset = statement.executeQuery();
            while (rset.next())
            {
                CharRecommendation recommendation = new CharRecommendation();
                recommendation.setCharId(charId);
                recommendation.setTargetId(rset.getInt("target_id"));
                recommendations.add(recommendation);
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("could not restore recommendations: "+e);
        }
        finally
        {
            closeConnectionQuietly(con);
        }
        return recommendations;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.characters.dao.ICharRecommendationsDAO#removeAllRecommendations(int)
     */
    public void removeAllRecommendations(int charId)
    {
        Connection con = null;
        try
        {
            con = getConnection(con);
            PreparedStatement statement = con.prepareStatement(DELETE_CHAR_RECOMS);
            statement.setInt(1, charId);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("could not clear char recommendations: "+e);
        }
        finally
        {
            closeConnectionQuietly(con);
        }

    }

}
