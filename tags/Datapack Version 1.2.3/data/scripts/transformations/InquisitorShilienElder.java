package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class InquisitorShilienElder extends L2Transformation
{
	public InquisitorShilienElder()
	{
		// id, colRadius, colHeight
		super(318, 8.0, 25.0);
	}

	public void onTransform(L2PcInstance player)
	{
		// Disable all character skills.
		for (L2Skill sk : player.getAllSkills())
		{
			if (sk != null && !sk.isPassive())
			{
				switch (sk.getId())
				{
					// Invocation
					case 1430:
						// Holy Weapon
					case 1043:
						// Wild Magic
					case 1303:
						// Empower
					case 1059:
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
		if (player.transformId() > 0 && !player.isCursedWeaponEquipped())
		{
			// give transformation skills
			transformedSkills(player);
			return;
		}
	}

	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() >= 43)
		{
			int level = player.getLevel() - 42;
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
		TransformationManager.getInstance().registerTransformation(new InquisitorShilienElder());
	}
}