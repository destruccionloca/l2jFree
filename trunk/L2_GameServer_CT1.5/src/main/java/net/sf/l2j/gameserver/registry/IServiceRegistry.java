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
package net.sf.l2j.gameserver.registry;

/**
 * 
 * Use to store constant name of service in registry
 * Store it avoid copy paste error and is better for refactoring 
 * when it's needed (or for search)
 */
public interface IServiceRegistry
{
    public static String FORUM = "ForumService";
    public static String VERSIONNING = "VersionningService";
    public static String RECIPE = "L2RecipeService";
    public static String EXTRACTABLE_ITEM = "ExtractableItemsService";
    public static String CHAR_RECOMMENDATIONS = "CharRecommendationService";
    public static String BOAT = "BoatService";
}   
