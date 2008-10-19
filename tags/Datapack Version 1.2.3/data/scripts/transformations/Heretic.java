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
public class Heretic extends L2DefaultTransformation
{
	public Heretic()
	{
		// id, colRadius, colHeight
		super(3, 13.0, 29.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Heretic Heal
		addSkill(player, 738, 3);
		// Heretic Battle Heal
		addSkill(player, 739, 3);
		// Heretic Resurrection
		addSkill(player, 740, 3);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Heretic Heal
		removeSkill(player, 738);
		// Heretic Battle Heal
		removeSkill(player, 739);
		// Heretic Resurrection
		removeSkill(player, 740);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Heretic());
	}
}