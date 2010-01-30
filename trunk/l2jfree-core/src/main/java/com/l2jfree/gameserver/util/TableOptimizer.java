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
package com.l2jfree.gameserver.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;

public final class TableOptimizer
{
	private final static Log _log = LogFactory.getLog(TableOptimizer.class);

	public static final void optimize()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("OPTIMIZE TABLE auction_bid,auction_watch,auction,auto_announcements," +
					"castle_doorupgrade,castle_functions,castle_hired_guards,castle_manor_procure," +
					"castle_manor_production,castle_zoneupgrade,character_birthdays,character_blocks," +
					"character_effects,character_friends,character_hennas,character_instance_time," +
					"character_macroses,character_mail,character_quest_global_data,character_quests," +
					"character_raid_points,character_recipebook,character_recommend_data,character_recommends," +
					"character_shortcuts,character_skill_reuses,character_skills,character_subclass_certification," +
					"character_subclasses,character_tpbookmark,characters,clan_data,clan_notices,clan_privs," +
					"clan_skills,clan_subpledges,clan_wars,clanhall_functions,clanhall_sieges,clanhall," +
					"couples,ctf_teams,ctf,cursed_weapons,dm,fort_doorupgrade,fort_functions,fort," +
					"fortsiege_clans,forums,games,gm_audit,heroes,item_attributes,items,itemsonground," +
					"obj_restrictions,olympiad_data,olympiad_nobles_eom,olympiad_nobles,petitions,pets," +
					"posts,quest_global_data,record,seven_signs_festival,seven_signs_status,seven_signs," +
					"siege_clans,topic,tvt_teams,tvt,VIPinfo");
			if (_log.isDebugEnabled())
				while (rs.next())
					_log.debug("TableOptimizer: " + rs.getString("Table") + " " + rs.getString("Msg_type") + " - " + rs.getString("Msg_text"));
			st.close();
			_log.info("TableOptimizer: Database tables have been optimized.");
		}
		catch (Exception e)
		{
			_log.warn("TableOptimizer: Cannot optimize database tables!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
