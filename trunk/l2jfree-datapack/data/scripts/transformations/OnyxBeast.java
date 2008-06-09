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
public class OnyxBeast extends L2DefaultTransformation
{
	public OnyxBeast()
	{
		// id, colRadius, colHeight
		super(1, 14.0, 15.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Power Claw
		addSkill(player, 584, 1);
		// Fast Moving
		addSkill(player, 585, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Power Claw
		removeSkill(player, 584);
		// Fast Moving
		removeSkill(player, 585);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new OnyxBeast());
	}
}
