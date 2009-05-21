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
public class DivineWarrior extends L2DefaultTransformation
{
	public DivineWarrior()
	{
		// id, colRadius, colHeight
		super(253, 12.0, 30.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		addSkill(player, 675, 1); // Cross Slash
		addSkill(player, 676, 1); // Sonic Blaster
		addSkill(player, 677, 1); // Transfixition of Earth
		addSkill(player, 678, 1); // Divine Warrior War Cry
		addSkill(player, 679, 1); // Sacrifice Warrior
		addSkill(player, 798, 1); // Divine Warrior Assault Attack
		*/
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		removeSkill(player, 675); // Cross Slash
		removeSkill(player, 676); // Sonic Blaster
		removeSkill(player, 677); // Transfixition of Earth
		removeSkill(player, 678); // Divine Warrior War Cry
		removeSkill(player, 679); // Sacrifice Warrior
		removeSkill(player, 798); // Divine Warrior Assault Attack
		*/
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineWarrior());
	}
}