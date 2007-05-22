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
package net.sf.l2j.gameserver.datatables;


/**
 * Interface for jmx administration
 * 
 * This MBean gives the ability to manipulate skills tree table
 * For example : get the number of skills in the tree, the number of fishing skills
 * or reload them etc...
 */
public interface SkillTreeTableMBean
{
    public int getNbFishingSkillTrees();
    public int getNbSkillTrees();
    public int getNbDwarfCraftSkillTrees();
    public int getNbEnchantSkillTrees();
    public void reload ();
}
