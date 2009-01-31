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
public class DoomWraith extends L2DefaultTransformation
{
	public DoomWraith()
	{
		// id, colRadius, colHeight
		super(2, 8.0, 22.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Rolling Attack
		addSkill(player, 586, 2);
		// Curse of Darkness
		addSkill(player, 588, 2);
		// Dig Attack
		addSkill(player, 587, 2);
		// Darkness Energy Drain
		addSkill(player, 589, 2);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Rolling Attack
		removeSkill(player, 586);
		// Curse of Darkness
		removeSkill(player, 588);
		// Dig Attack
		removeSkill(player, 587);
		// Darkness Energy Drain"
		removeSkill(player, 589);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DoomWraith());
	}
}
