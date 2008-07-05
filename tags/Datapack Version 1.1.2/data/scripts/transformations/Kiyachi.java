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
public class Kiyachi extends L2DefaultTransformation
{
	public Kiyachi()
	{
		// id, colRadius, colHeight
		super(310, 12.0, 29.8);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Kechi Double Cutter
		addSkill(player, 733, 1);
		// Kechi Air Blade
		addSkill(player, 734, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Kechi Double Cutter
		removeSkill(player, 733);
		// Kechi Air Blade
		removeSkill(player, 734);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Kiyachi());
	}
}