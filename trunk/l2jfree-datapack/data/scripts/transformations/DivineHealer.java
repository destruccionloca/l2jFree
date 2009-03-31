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
public class DivineHealer extends L2DefaultTransformation
{
	public DivineHealer()
	{
		// id, colRadius, colHeight
		super(255, 12.0, 24.0);
	}
	
	public void transformedSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		addSkill(player, 698, 1); // Divine Healer Major Heal
		addSkill(player, 699, 1); // Divine Healer Battle Heal
		addSkill(player, 700, 1); // Divine Healer Group Heal
		addSkill(player, 701, 1); // Divine Healer Resurrection*/
		addSkill(player, 702, 1); // Divine Healer Clans
		//addSkill(player, 703, 1); // Sacrifice Healer
	}

	public void removeSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		removeSkill(player, 698); // Divine Healer Major Heal
		removeSkill(player, 699); // Divine Healer Battle Heal
		removeSkill(player, 700); // Divine Healer Group Heal
		removeSkill(player, 701); // Divine Healer Resurrection*/
		removeSkill(player, 702); // Divine Healer Clans
		//removeSkill(player, 703); // Sacrifice Healer
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineHealer());
	}
}