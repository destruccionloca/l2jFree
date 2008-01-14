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

public class CharRecommendation
{
    private int charId;
    private int targetId;
    /**
     * @return the charId
     */
    public int getCharId()
    {
        return charId;
    }
    /**
     * @param charId the charId to set
     */
    public void setCharId(int charId)
    {
        this.charId = charId;
    }
    /**
     * @return the targetId
     */
    public int getTargetId()
    {
        return targetId;
    }
    /**
     * @param targetId the targetId to set
     */
    public void setTargetId(int targetId)
    {
        this.targetId = targetId;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (charId ^ (charId >>> 32));
        result = PRIME * result + (targetId ^ (targetId >>> 32));
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CharRecommendation other = (CharRecommendation) obj;
        if (charId != other.charId)
            return false;
        if (targetId != other.targetId)
            return false;
        return true;
    }
  
    

}
