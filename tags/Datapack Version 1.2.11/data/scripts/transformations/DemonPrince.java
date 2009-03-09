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
 * @author Ahmed
 *
 */
public class DemonPrince extends L2DefaultTransformation
{
	public DemonPrince()
	{
		// id, colRadius, colHeight
		super(311, 33.0, 49.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 735, 1); // Devil Spinning Weapon
		addSkill(player, 736, 1); // Devil Seed
		addSkill(player, 737, 1); // Devil Ultimate Defense
	}

	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 735); // Devil Spinning Weapon
		removeSkill(player, 736); // Devil Seed
		removeSkill(player, 737); // Devil Ultimate Defense
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DemonPrince());
	}
}