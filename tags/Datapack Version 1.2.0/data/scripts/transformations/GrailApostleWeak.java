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
public class GrailApostleWeak extends L2DefaultTransformation
{
	public GrailApostleWeak()
	{
		// id, colRadius, colHeight
		super(203, 8.0, 30.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Spear
		addSkill(player, 559, 2);
		// Power Slash
		addSkill(player, 560, 2);
		// Bless of Angel
		addSkill(player, 561, 2);
		// Wind of Angel
		addSkill(player, 562, 2);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Spear
		removeSkill(player, 559);
		// Power Slash
		removeSkill(player, 560);
		// Bless of Angel
		removeSkill(player, 561);
		// Wind of Angel
		removeSkill(player, 562);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new GrailApostleWeak());
	}
}
