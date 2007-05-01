/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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
package net.sf.l2j.gameserver.communitybbs.dao.forum;

import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;

/**
 * Interface for Forums DAO
 * 
 */
public interface ForumsDAO
{
	public Forums getForumById (Integer id);
	
    public int createForums (Forums obj);
    
    public List <Forums> getAllForums();
    
    public List <Forums> getChildrens(Forums obj);
    
    public List <Forums> getChildrens(Integer forumId);
    
    public Forums getChildForumByName (Integer forumId, String name);

    public Set<Topic> getTopicsForForum (Forums obj);
    
    public void deleteForum (Forums obj);
}
