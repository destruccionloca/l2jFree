package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;


public class InquisitorElvenElder extends L2Transformation
{
	public InquisitorElvenElder()
	{
		// id, colRadius, colHeight
		super(317, 7.0, 24.0);
	}

	public void onTransform(L2PcInstance player)
	{
		// Update transformation ID into database and player instance variables.
		player.transformInsertInfo();
		if (player.transformId() > 0 && !player.isCursedWeaponEquipped())
		{
			// Disable all character skills.
			for (L2Skill sk : player.getAllSkills())
			{
				if (sk != null && !sk.isPassive())
				{
					switch (sk.getId())
					{
						case 1430: // Invocation
						case 1043: // Holy Weapon
						case 1303: // Wild Magic
						case 1400: // Turn Undead
						{
							// Those Skills wont be removed.
							break;
						}
						default:
						{
							player.removeSkill(sk, false);
							break;
						}
					}
				}
			}
			// give transformation skills
			transformedSkills(player);
			return;
		}
	}

	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			// Divine Punishment
			addSkill(player, 1523, level);
			// Divine Flash
			addSkill(player, 1528, level);
			// Surrender to the Holy
			addSkill(player, 1524, level);
			// Divine Curse
			addSkill(player, 1525, level);
			// Switch Stance
			addSkill(player, 838, 1);
			// Send a Server->Client packet StatusUpdate to the L2PcInstance.
			player.sendSkillList();
		}
	}

	public void onUntransform(L2PcInstance player)
	{
		// remove transformation skills
		removeSkills(player);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Divine Punishment
		removeSkill(player, 1523);
		// Divine Flash
		removeSkill(player, 1528);
		// Surrender to the Holy
		removeSkill(player, 1524);
		// Divine Curse
		removeSkill(player, 1525);
		// Switch Stance
		removeSkill(player, 838);
		// Send a Server->Client packet StatusUpdate to the L2PcInstance.
		player.sendSkillList();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InquisitorElvenElder());
	}
}