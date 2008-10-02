package transformations;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class InquisitorShilienElder extends L2Transformation
{
	public InquisitorShilienElder()
	{
		// id, duration (secs), colRadius, colHeight
		super(318, Integer.MAX_VALUE, 11.0, 25.0);
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
		// give transformation skills
		transformedSkills(player);
	}

	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			// Divine Punishment
			player.addSkill(SkillTable.getInstance().getInfo(1523, player.getLevel() - 43), false);
			// Divine Flash
			player.addSkill(SkillTable.getInstance().getInfo(1528, player.getLevel() - 43), false);
			// Surrender to the Holy
			player.addSkill(SkillTable.getInstance().getInfo(1524, player.getLevel() - 43), false);
			// Divine Curse
			player.addSkill(SkillTable.getInstance().getInfo(1525, player.getLevel() - 43), false);
			// Switch Stance
			player.addSkill(SkillTable.getInstance().getInfo(838, 1), false);
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
		player.removeSkill(SkillTable.getInstance().getInfo(1523, player.getLevel() - 43), false);
		// Divine Flash
		player.removeSkill(SkillTable.getInstance().getInfo(1528, player.getLevel() - 43), false);
		// Surrender to the Holy
		player.removeSkill(SkillTable.getInstance().getInfo(1524, player.getLevel() - 43), false);
		// Divine Curse
		player.removeSkill(SkillTable.getInstance().getInfo(1525, player.getLevel() - 43), false);
		// Switch Stance
		player.removeSkill(SkillTable.getInstance().getInfo(838, 1), false);
		// Send a Server->Client packet StatusUpdate to the L2PcInstance.
		player.sendSkillList();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InquisitorShilienElder());
	}
}