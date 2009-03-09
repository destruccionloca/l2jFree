package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class VanguardShilienKnight extends L2Transformation
{
	public VanguardShilienKnight()
	{
		// id, colRadius, colHeight
		super(315, 6.0, 23.0);
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
						case 28:  // Aggression
						case 18:  // Aura of Hate
						case 22:  // Summon Vampiric Cubic
						case 33:  // Summon Phantom Cubic
						case 401: // Judgment
						case 278: // Summon Viper Cubic
						case 289: // Life Leech
						case 279: // Lightning Strike
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
			addSkill(player, 814, level); // Full Swing
			addSkill(player, 816, level); // Power Divide aka Cleave
			addSkill(player, 838, 1); // Switch Stance
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
		removeSkill(player, 814); // Double Strike
		removeSkill(player, 816); // Blade Hurricane
		removeSkill(player, 838); // Switch Stance
		// Send a Server->Client packet StatusUpdate to the L2PcInstance.
		player.sendSkillList();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardShilienKnight());
	}
}