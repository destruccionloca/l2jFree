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
		super(3, 13.0, 19.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		int level = 1;
		if (player.getLevel() >= 76)
		{
			level = 3;
		}
		else if (player.getLevel() >= 73)
		{
			level = 2;
		}
		addSkill(player, 738, level); // Heretic Heal
		addSkill(player, 739, level); // Heretic Battle Heal
		addSkill(player, 740, level); // Heretic Resurrection
		addSkill(player, 741, level); // Heretic Heal Side Effect
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 738); // Heretic Heal
		removeSkill(player, 739); // Heretic Battle Heal
		removeSkill(player, 740); // Heretic Resurrection
		removeSkill(player, 741); // Heretic Heal Side Effect
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Heretic());
	}
}