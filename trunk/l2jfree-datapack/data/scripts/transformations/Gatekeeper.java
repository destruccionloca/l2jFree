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
 * @author Psychokiller1888
 *
 */
public class Gatekeeper extends L2DefaultTransformation
{
	public Gatekeeper()
	{
		// id, colRadius, colHeight
		super(107, 8.0, 24.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Transfrom Dispel
		addSkill(player,619, 1);
		// Transform Alternative Gatekeeper
		addSkill(player,5655, 1);
		// Gatekeeper Aura Flare
		addSkill(player,5656, 85);
		// Gatekeeper Prominence
		addSkill(player,5657, 85);
		// Gatekeeper Flame Strike
		addSkill(player,5658, 85);
		// Gatekeeper Berserker Spirit
		addSkill(player,5659, 2);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Transfrom Dispel
		removeSkill(player,619);
		// Transform Alternative Gatekeeper
		removeSkill(player,5655);
		// Gatekeeper Aura Flare
		removeSkill(player,5656);
		// Gatekeeper Prominence
		removeSkill(player,5657);
		// Gatekeeper Flame Strike
		removeSkill(player,5658);
		// Gatekeeper Berserker Spirit
		removeSkill(player,5659);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Gatekeeper());
	}
}
