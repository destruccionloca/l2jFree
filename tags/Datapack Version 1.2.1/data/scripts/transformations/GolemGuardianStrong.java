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
public class GolemGuardianStrong extends L2DefaultTransformation
{
	public GolemGuardianStrong()
	{
		// id, colRadius, colHeight
		super(210, 8.0, 23.5);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Double Slasher
		addSkill(player, 572, 4);
		// Earthquake
		addSkill(player, 573, 4);
		// Bomb Installation
		addSkill(player, 574, 4);
		// Steel Cutter
		addSkill(player, 575, 4);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Double Slasher
		removeSkill(player, 572);
		// Earthquake
		removeSkill(player, 573);
		// Bomb Installation
		removeSkill(player, 574);
		// Steel Cutter
		removeSkill(player, 575);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new GolemGuardianStrong());
	}
}
