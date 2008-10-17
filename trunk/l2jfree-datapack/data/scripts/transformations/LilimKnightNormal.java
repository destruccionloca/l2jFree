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
public class LilimKnightNormal extends L2DefaultTransformation
{
	public LilimKnightNormal()
	{
		// id, colRadius, colHeight
		super(208, 8.0, 24.4);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Attack Buster
		addSkill(player, 568, 3);
		// Attack Storm
		addSkill(player, 569, 3);
		// Attack Rage
		addSkill(player, 570, 3);
		// Poison Dust
		addSkill(player, 571, 3);

		player.clearCharges();
	}

	public void removeSkills(L2PcInstance player)
	{
		// Attack Buster
		removeSkill(player, 568);
		// Attack Storm
		removeSkill(player, 569);
		// Attack Rage
		removeSkill(player, 570);
		// Poison Dust
		removeSkill(player, 571);

		player.clearCharges();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new LilimKnightNormal());
	}
}
