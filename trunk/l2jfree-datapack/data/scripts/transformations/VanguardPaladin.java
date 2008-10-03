package transformations;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class VanguardPaladin extends L2Transformation
{
	public VanguardPaladin()
	{
		// id, duration (secs), colRadius, colHeight
		super(312, Integer.MAX_VALUE, 8.0, 23.0);
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
						// Angelic Icon
					case 406:
						// Tribunal
					case 400:
						// Holy Blade
					case 196:
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
		TransformationManager.getInstance().registerTransformation(new VanguardPaladin());
	}
}