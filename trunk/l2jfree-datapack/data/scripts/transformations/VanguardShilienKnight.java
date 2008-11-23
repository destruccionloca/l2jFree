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
						// Aggression
						case 28:
							// Aura of Hate
						case 18:
							// Summon Vampiric Cubic
						case 22:
							// Summon Phantom Cubic
						case 33:
							// Judgment
						case 401:
							// Summon Viper Cubic
						case 278:
							// 	Life Leech
						case 289:
							// Lightning Strike
						case 279:
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
			// Full Swing
			addSkill(player, 814, level);
			// Power Divide aka Cleave
			addSkill(player, 816, level);
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
		// Double Strike
		removeSkill(player, 814);
		// Blade Hurricane
		removeSkill(player, 816);
		// Switch Stance
		removeSkill(player, 838);
		// Send a Server->Client packet StatusUpdate to the L2PcInstance.
		player.sendSkillList();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardShilienKnight());
	}
}