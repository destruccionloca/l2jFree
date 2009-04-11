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
public class DragonBomberNormal extends L2DefaultTransformation
{
	public DragonBomberNormal()
	{
		// id, colRadius, colHeight
		super(217, 8.0, 22.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 580, 3); // Death Blow
		addSkill(player, 581, 3); // Sand Cloud
		addSkill(player, 582, 3); // Scope Bleed
		addSkill(player, 583, 3); // Assimilation
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 580); // Death Blow
		removeSkill(player, 581); // Sand Cloud
		removeSkill(player, 582); // Scope Bleed
		removeSkill(player, 583); // Assimilation
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DragonBomberNormal());
	}
}
