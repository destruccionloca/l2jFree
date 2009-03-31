package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class VanguardShilienKnight extends L2DefaultTransformation
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

		// Switch Stance
		addSkill(player, 838, 1);
		// Decrease Bow/Crossbow Attack Speed
		addSkill(player, 5491, 1);		
		
		// give transformation skills
		transformedSkills(player);
	}
	
	public void onUntransform(L2PcInstance player)
	{
		// Switch Stance
		removeSkill(player, 838);
		// Decrease Bow/Crossbow Attack Speed
		removeSkill(player, 5491);
		
		// remove transformation skills
		removeSkills(player);
	}	

	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 815, level); // Blade Hurricane
			addSkill(player, 817, level); // Double Strike
		}
		player.addTransformAllowedSkill(new int[]{28,18,22,33,401,278,289,279})		
	}

	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 815); // Blade Hurricane
		removeSkill(player, 817); // Double Strike
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardShilienKnight());
	}
}