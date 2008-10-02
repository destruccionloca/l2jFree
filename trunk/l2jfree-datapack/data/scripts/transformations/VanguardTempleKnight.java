package transformations;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class VanguardTempleKnight extends L2Transformation
{
	public VanguardTempleKnight()
	{
		// id, duration (secs), colRadius, colHeight
		super(314, Integer.MAX_VALUE, 11.0, 25.0);
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
						// Summon Storm Cubic
					case 10:
						// Summon Life Cubic
					case 67:
						// Summon Attractive Cubic
					case 449:
						// 	Tribunal
					case 400:
						// Holy Armor
					case 197:
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
			// Power Divide
			player.addSkill(SkillTable.getInstance().getInfo(816, player.getLevel() - 43), false);
			// Full Swing
			player.addSkill(SkillTable.getInstance().getInfo(814, player.getLevel() - 43), false);
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
		// Power Divide
		player.removeSkill(SkillTable.getInstance().getInfo(816, player.getLevel() - 43), false);
		// Full Swing
		player.removeSkill(SkillTable.getInstance().getInfo(814, player.getLevel() - 43), false);
		// Switch Stance
		player.removeSkill(SkillTable.getInstance().getInfo(838, 1), false);
		// Send a Server->Client packet StatusUpdate to the L2PcInstance.
		player.sendSkillList();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardTempleKnight());
	}
}