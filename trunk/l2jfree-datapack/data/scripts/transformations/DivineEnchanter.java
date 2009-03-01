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
public class DivineEnchanter extends L2DefaultTransformation
{
	public DivineEnchanter()
	{
		// id, colRadius, colHeight
		super(257, 12.0, 24.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		addSkill(player, 704, 1); // Divine Enchanter Water Spirit
		addSkill(player, 705, 1); // Divine Enchanter Fire Spirit
		addSkill(player, 706, 1); // Divine Enchanter Wind Spirit
		addSkill(player, 707, 1); // Divine Enchanter Hero Spirit
		addSkill(player, 708, 1); // Divine Enchanter Mass Binding
		addSkill(player, 709, 1); // Sacrifice Enchanter
		*/
	}

	public void removeSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		removeSkill(player, 704); // Divine Enchanter Water Spirit
		removeSkill(player, 705); // Divine Enchanter Fire Spirit
		removeSkill(player, 706); // Divine Enchanter Wind Spirit
		removeSkill(player, 707); // Divine Enchanter Hero Spirit
		removeSkill(player, 708); // Divine Enchanter Mass Binding
		removeSkill(player, 709); // Sacrifice Enchanter
		*/
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineEnchanter());
	}
}