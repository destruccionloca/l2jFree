package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Description: <br>
 * This will handle the transformation, giving the skills, and removing them, when the player logs out and is transformed these skills
 * do not save.
 * When the player logs back in, there will be a call from the enterworld packet that will add all their skills.
 * The enterworld packet will transform a player.
 *
 * @author durgus
 *
 */
public class InfernoDrakeStrong extends L2DefaultTransformation
{
	public InfernoDrakeStrong()
	{
		// id, colRadius, colHeight
		super(213, 8.0, 22.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 576, 4); // Paw Strike
		addSkill(player, 577, 4); // Fire Breath
		addSkill(player, 578, 4); // Blaze Quake
		addSkill(player, 579, 4); // Fire Armor
	}

	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 576); // Paw Strike
		removeSkill(player, 577); // Fire Breath
		removeSkill(player, 578); // Blaze Quake
		removeSkill(player, 579); // Fire Armor
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InfernoDrakeStrong());
	}
}
