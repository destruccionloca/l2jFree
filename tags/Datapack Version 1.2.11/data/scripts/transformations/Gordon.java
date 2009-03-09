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
		addSkill(player, 728, 1); // Gordon Beast Attack
		addSkill(player, 729, 1); // Gordon Sword Stab
		addSkill(player, 730, 1); // Gordon Press
	}

	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 728); // Gordon Beast Attack
		removeSkill(player, 729); // Gordon Sword Stab
		removeSkill(player, 730); // Gordon Press
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Gordon());
	}
}