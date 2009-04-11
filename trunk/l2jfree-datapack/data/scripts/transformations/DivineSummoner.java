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
public class DivineSummoner extends L2DefaultTransformation
{
	public DivineSummoner()
	{
		// id, colRadius, colHeight
		super(258, 12.0, 24.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		addSkill(player, 710, 1); // Divine Summoner Summon Divine Beast
		addSkill(player, 711, 1); // Divine Summoner Transfer Pain
		addSkill(player, 712, 1); // Divine Summoner Final Servitor
		addSkill(player, 713, 1); // Divine Summoner Servitor Hill
		addSkill(player, 714, 1); // Sacrifice Summoner
		*/
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		removeSkill(player, 710); // Divine Summoner Summon Divine Beast
		removeSkill(player, 711); // Divine Summoner Transfer Pain
		removeSkill(player, 712); // Divine Summoner Final Servitor
		removeSkill(player, 713); // Divine Summoner Servitor Hill
		removeSkill(player, 714); // Sacrifice Summoner
		*/
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineSummoner());
	}
}