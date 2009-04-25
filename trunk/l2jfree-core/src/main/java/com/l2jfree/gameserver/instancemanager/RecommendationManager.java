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
package com.l2jfree.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Savormix
 * @since 2009-04-20
 */
public final class RecommendationManager
{
	private static final String ADD_RECOMMENDATION_INFO = "INSERT INTO character_recommend_data (charId,lastUpdate) VALUES (?,?)";
	private static final String UPDATE_RECOMMENDATION_INFO = "UPDATE character_recommend_data SET evaluationAble = ?,evaluationPoints = ?,lastUpdate=? WHERE charId=?";
	private static final String REMOVE_RECOMMENDATION_INFO = "DELETE FROM character_recommend_data WHERE charId = ?";
	private static final String RESTORE_RECOMMENDATION_INFO = "SELECT evaluationAble,evaluationPoints,lastUpdate FROM character_recommend_data WHERE charId=?";
	private static final String ADD_RECOMMENDATION_RESTRICTION = "INSERT INTO character_recommends VALUES (?,?)";
	private static final String REMOVE_RECOMMENDATION_RESTRICTIONS = "TRUNCATE TABLE character_recommends";
	private static final String REMOVE_RECOMMENDATION_RESTRICTIONS_PLAYER = "DELETE FROM character_recommends WHERE charId=? OR target_id=?";
	private static final String RESTORE_RECOMMENDATION_RESTRICTIONS = "SELECT target_id FROM character_recommends WHERE charId=?";

	private static final Log _log = LogFactory.getLog(RecommendationManager.class.getName());
	private static final long DAY = 24 * 3600 * 1000;

	private long nextUpdate;

	public static RecommendationManager _instance = null;

	/** @return the only instance of this manager */
	public static RecommendationManager getInstance()
	{
		if (_instance == null)
			_instance = new RecommendationManager();
		return _instance;
	}

	private RecommendationManager()
	{
		Calendar update = Calendar.getInstance();
		if (update.get(Calendar.HOUR_OF_DAY) >= 13)
			update.add(Calendar.DAY_OF_MONTH, 1);
		update.set(Calendar.HOUR_OF_DAY, 13);
		nextUpdate = update.getTimeInMillis();
		ThreadPoolManager.getInstance().schedule(new RecommendationUpdater(), nextUpdate - System.currentTimeMillis());
		_log.info("RecommendationManager: initialized.");
	}

	/**
	 * <B>Tries to recommend a player</B>.<BR>
	 * Sends a system message both on failure and success. Adds a session restriction
	 * <I>(and updates the database if saving evaluation restrictions)</I>.
	 * @param evaluator Player giving the evaluation
	 * @param evaluated Player being evaluated
	 */
	public void recommend(L2PcInstance evaluator, L2PcInstance evaluated)
	{
		if (evaluator == null)
			return;

		SystemMessageId smi = null;
		if (evaluator.getLevel() < 10)
			smi = SystemMessageId.ONLY_LEVEL_SUP_10_CAN_RECOMMEND;
		else if (evaluator == evaluated)
			smi = SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF;
		else if (evaluator.getEvaluations() <= 0)
			smi = SystemMessageId.NO_MORE_RECOMMENDATIONS_TO_HAVE;
		else if (evaluated.getEvalPoints() >= 255)
			smi = SystemMessageId.YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION;
		else if (!evaluator.canEvaluate(evaluated))
			smi = SystemMessageId.THAT_CHARACTER_IS_RECOMMENDED;
		if (smi != null)
		{
			evaluator.sendPacket(smi);
			return;
		}

		Connection con = null;
		PreparedStatement ps = null;
		try
		{
			if (Config.ALT_RECOMMEND)
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				ps = con.prepareStatement(ADD_RECOMMENDATION_RESTRICTION);
				ps.setInt(1, evaluator.getObjectId());
				ps.setInt(2, evaluated.getObjectId());
				ps.executeUpdate();
				ps.close();
			}
			//ALWAYS. It's the same on retail!
			evaluator.addEvalRestriction(evaluated.getObjectId());
			update(evaluator, evaluator.getEvaluations() - 1, evaluator.getEvalPoints());
			update(evaluated, evaluated.getEvaluations(), evaluated.getEvalPoints() + 1);
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS);
			sm.addPcName(evaluated);
			sm.addNumber(evaluator.getEvaluations());
			evaluator.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_S1);
			sm.addPcName(evaluator);
			evaluated.sendPacket(sm);
			evaluated.broadcastUserInfo();
		}
		catch (SQLException e)
		{
			_log.error(evaluator.getName() + " failed evaluating player " + evaluated.getName() + "!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * <B>Create an entry in `character_recommend_data`</B>.<BR>
	 * Called just after character creation, but may be also called when restoring player's
	 * evaluation data and the entry is missing.
	 * @param player The newly created player
	 */
	public void onCreate(L2PcInstance player)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement ps = con.prepareStatement(ADD_RECOMMENDATION_INFO);
			ps.setInt(1, player.getObjectId());
			ps.setLong(2, nextUpdate - DAY);
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			_log.error("Failed creating recommendation data for " + player.getName() + "!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Called whenever the character is loaded (<I>{@link L2PcInstance#load(int)}</I> is called.
	 * <LI>Restore player's evaluation data, <I>create entry if necessary</I></LI>
	 * <LI>Restore player's evaluated player data (<I>if enabled in config</I>)</LI>
	 * <LI>Update player's evaluation count and points*</LI><BR>
	 * <I>* - for each 24 hours since the last evaluation data update for this player,
	 * player loses 1-3 points</I> 
	 * @param player The loaded L2PcInstance
	 */
	public void onJoin(L2PcInstance player)
	{
		Connection con = null;
		PreparedStatement ps = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			ps = con.prepareStatement(RESTORE_RECOMMENDATION_INFO);
			ps.setInt(1, player.getObjectId());
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
			{
				_log.warn("Player " + player.getName() + " did not have recommendation data, creating default entry!");
				onCreate(player);
				rs = ps.executeQuery();
			}
			int points = rs.getInt("evaluationPoints");
			long lastUpdate = rs.getLong("lastUpdate");
			while (lastUpdate < (nextUpdate - DAY))
			{
				points = getNewEvalPointsQuick(points, getDailyLostPoints(player.getLevel()));
				lastUpdate += DAY;
			}
			update(player, rs.getInt("evaluationAble"), points);
			rs.close();
			ps.close();
			if (Config.ALT_RECOMMEND)
			{
				ps = con.prepareStatement(RESTORE_RECOMMENDATION_RESTRICTIONS);
				ps.setInt(1, player.getObjectId());
				rs = ps.executeQuery();
				while (rs.next())
					player.addEvalRestriction(rs.getInt(1));
				rs.close();
				ps.close();
			}
		}
		catch (SQLException e)
		{
			_log.error("Failed loading recommendation data for player " + player.getName() + "!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Called to clean up player's evaluation data when a character is being deleted.
	 * @param charId character's ID
	 * @param con An open SQL connection
	 * @param ps Reference
	 */
	public void onDelete(int charId, Connection con, PreparedStatement ps)
	{
		try
		{
			ps = con.prepareStatement(REMOVE_RECOMMENDATION_INFO);
			ps.setInt(1, charId);
			ps.executeUpdate();
			ps.close();
			ps = con.prepareStatement(REMOVE_RECOMMENDATION_RESTRICTIONS_PLAYER);
			ps.setInt(1, charId);
			ps.setInt(2, charId);
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			_log.error("Failed deleting recommendation data for player " + charId + "!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Set the new player's evaluation points and save to database.
	 * @param player Player being evaluated
	 * @param evalPoints Evaluation point count
	 */
	public void onGmEvaluation(L2PcInstance player, int evalPoints)
	{
		update(player, player.getEvaluations(), evalPoints);
	}

	private void update(L2PcInstance player, int recomLeft, int evalPoints)
	{
		Connection con = null;
		PreparedStatement ps = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			ps = con.prepareStatement(UPDATE_RECOMMENDATION_INFO);
			ps.setInt(1, recomLeft);
			ps.setInt(2, evalPoints);
			ps.setLong(3, nextUpdate - DAY);
			ps.setInt(4, player.getObjectId());
			ps.executeUpdate();
			ps.close();
			player.setEvaluationCount(recomLeft);
			player.setEvalPoints(evalPoints);
		}
		catch (SQLException e)
		{
			_log.error("Failed updating player's (" + player.getName() +  ") recommendations!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private int getDailyRecommendations(int level)
	{
		if (level >= 40)
			return 9;
		else if (level >= 20)
			return 6;
		else
			return 3;
	}

	private int getDailyLostPoints(int level)
	{
		return getDailyRecommendations(level) / 3;
	}

	private int getNewEvalPoints(L2PcInstance player)
	{
		return getNewEvalPointsQuick(player.getEvalPoints(), getDailyLostPoints(player.getLevel()));
	}

	private int getNewEvalPointsQuick(int current, int lost)
	{
		if ((lost = (current - lost)) > 0)
			return lost;
		else
			return 0;
	}

	/**
	 * Updates online player evaluation data each day at 1PM.
	 * Deletes all evaluation restrictions from the database.
	 * @author Savormix
	 */
	private class RecommendationUpdater implements Runnable
	{
		@Override
		public void run()
		{
			Connection con = null;
			PreparedStatement ps = null;
			int rec, pts;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					ps = con.prepareStatement(UPDATE_RECOMMENDATION_INFO);
					rec = getDailyRecommendations(player.getLevel());
					pts = getNewEvalPoints(player);
					ps.setInt(1, rec);
					ps.setInt(2, pts);
					ps.setLong(3, nextUpdate);
					ps.setInt(4, player.getObjectId());
					ps.executeUpdate();
					ps.close();
					player.setEvaluationCount(rec);
					player.setEvalPoints(pts);
					player.cleanEvalRestrictions();
				}
				ps = con.prepareStatement(REMOVE_RECOMMENDATION_RESTRICTIONS);
				ps.executeUpdate();
				ps.close();
			}
			catch (SQLException e)
			{
				_log.error("Failed updating recommendations!", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			Calendar update = Calendar.getInstance();
			if (update.get(Calendar.HOUR_OF_DAY) >= 13)
				update.add(Calendar.DAY_OF_MONTH, 1);
			update.set(Calendar.HOUR_OF_DAY, 13);
			nextUpdate = update.getTimeInMillis();
			ThreadPoolManager.getInstance().schedule(this, nextUpdate - System.currentTimeMillis());
		}
	}
}
