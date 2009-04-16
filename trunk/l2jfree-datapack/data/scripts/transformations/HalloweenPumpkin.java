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
public class HalloweenPumpkin extends L2DefaultTransformation
{
	public HalloweenPumpkin()
	{
		// id, colRadius, colHeight
		super(108, 15.5, 29.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player,619, 1); // Transfrom Dispel
		addSkill(player,5437, 2); // Dissonance
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player,619); // Transfrom Dispel
		removeSkill(player,5437); // Dissonance
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new HalloweenPumpkin());
	}
}
