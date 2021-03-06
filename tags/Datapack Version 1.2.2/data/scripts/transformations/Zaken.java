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
public class Zaken extends L2DefaultTransformation
{
	public Zaken()
	{
		// id, colRadius, colHeight
		super(305, 16.0, 32.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Zaken Energy Drain
		addSkill(player, 715, 4);
		// Zaken Hold
		addSkill(player, 716, 4);
		// Zaken Concentrated Attack
		addSkill(player, 717, 4);
		// Zaken Dancing Sword
		addSkill(player, 718, 4);
		// Zaken Vampiric Rage
		addSkill(player, 719, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Zaken Energy Drain
		removeSkill(player, 715);
		// Zaken Hold
		removeSkill(player, 716);
		// Zaken Concentrated Attack
		removeSkill(player, 717);
		// Zaken Dancing Sword
		removeSkill(player, 718);
		// Zaken Vampiric Rage
		removeSkill(player, 719);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Zaken());
	}
}