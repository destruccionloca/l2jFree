package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class VanguardTempleKnight extends L2DefaultTransformation
{
	public VanguardTempleKnight()
	{
		// id, colRadius, colHeight
		super(314, 7.0, 24.0);
	}

	@Override
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
	
	@Override
	public void onUntransform(L2PcInstance player)
	{
		// Switch Stance
		removeSkill(player, 838);
		// Decrease Bow/Crossbow Attack Speed
		removeSkill(player, 5491);
		
		// remove transformation skills
		removeSkills(player);
	}	

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 293, level); // Two handed mastery
			addSkill(player, 814, level); // Full Swing
			addSkill(player, 816, level); // Power Divide
		}
		player.addTransformAllowedSkill(new int[]{28,18,10,67,449,400,197});
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 293); // Two handed mastery
		removeSkill(player, 814); // Full Swing
		removeSkill(player, 816); // Power Divide
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardTempleKnight());
	}
}