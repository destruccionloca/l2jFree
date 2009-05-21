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
public class Gordon extends L2DefaultTransformation
{
	public Gordon()
	{
		// id, colRadius, colHeight
		super(308, 43.0, 46.6);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Gordon Beast Attack
		addSkill(player, 728, 1);
		// Gordon Sword Stab
		addSkill(player, 729, 1);
		// Gordon Press
		addSkill(player, 730, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Gordon Beast Attack
		removeSkill(player, 728);
		// Gordon Sword Stab
		removeSkill(player, 729);
		// Gordon Press
		removeSkill(player, 730);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Gordon());
	}
}