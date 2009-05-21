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
public class Ranku extends L2DefaultTransformation
{
	public Ranku()
	{
		// id, colRadius, colHeight
		super(309, 13.0, 28.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Ranku Dark Explosion
		addSkill(player, 732, 1);
		// Ranku Stun Attack
		addSkill(player, 733, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Ranku Dark Explosion
		removeSkill(player, 732);
		// Ranku Stun Attack
		removeSkill(player, 733);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Ranku());
	}
}