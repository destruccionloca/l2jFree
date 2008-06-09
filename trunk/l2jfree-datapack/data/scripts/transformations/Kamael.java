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
 * @author KenM
 *
 */
public class Kamael extends L2DefaultTransformation
{
	public Kamael()
	{
		// id, colRadius, colHeight
		super(251, 9.0, 30.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Nail Attack
		addSkill(player, 539, 1);
		// Wing Assault
		addSkill(player, 540, 1);
		// Soul Sucking
		addSkill(player, 1471, 1);
		// Death Beam
		addSkill(player, 1472, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Nail Attack
		removeSkill(player, 539);
		// Wing Assault
		removeSkill(player, 540);
		// Soul Sucking
		removeSkill(player, 1471);
		// Death Beam
		removeSkill(player, 1472);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Kamael());
	}
}
