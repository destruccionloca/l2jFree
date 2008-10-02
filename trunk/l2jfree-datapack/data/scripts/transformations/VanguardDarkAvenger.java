package transformations;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class VanguardDarkAvenger extends L2Transformation
{
	public VanguardDarkAvenger()
	{
		// id, duration (secs), colRadius, colHeight
		super(313, Integer.MAX_VALUE, 11.0, 25.0);
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
					// Aggression
					case 28:
						// Aura of Hate
					case 18:
						// Summon Dark Panther
					case 283:
						// Horror
					case 65:
						// Judgment
					case 401:
						// Reflect Damage
					case 86:
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
			// Double Strike
			player.addSkill(SkillTable.getInstance().getInfo(817, player.getLevel() - 43), false);
			// Blade Hurricane
			player.addSkill(SkillTable.getInstance().getInfo(815, player.getLevel() - 43), false);
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
		// Double Strike
		player.removeSkill(SkillTable.getInstance().getInfo(817, player.getLevel() - 43), false);
		// Blade Hurricane
		player.removeSkill(SkillTable.getInstance().getInfo(815, player.getLevel() - 43), false);
		// Switch Stance
		player.removeSkill(SkillTable.getInstance().getInfo(838, 1), false);
		// Send a Server->Client packet StatusUpdate to the L2PcInstance.
		player.sendSkillList();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardDarkAvenger());
	}
}