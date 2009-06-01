package transformations;

import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Description: <br>
 * This will handle the transformation, giving the skills, and removing them, when the player logs out and is transformed these skills
 * do not save. 
 * When the player logs back in, there will be a call from the enterworld packet that will add all their skills.
 * The enterworld packet will transform a player.
 * 
 * FIXME: move missing methods from L2Jserver!
 *
 * @author Kerberos, Respawner
 */
public class AurabirdFalcon extends L2DefaultTransformation
{
	public AurabirdFalcon()
	{
		// id, colRadius, colHeight
		super(8, 38.0, 14.0);
	}

	@Override
	public void onTransform(L2PcInstance player)
	{
		// FIXME: super();
		player.setIsFlyingMounted(true);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		// FIXME: super();

		if (player.getLevel() >= 75)
			addSkill(player, 885, 1);

		int level = player.getLevel() - 74;
		if (level > 0)
		{
			addSkill(player, 884, level);
			addSkill(player, 886, level);
			addSkill(player, 888, level);
			addSkill(player, 891, level);
			addSkill(player, 911, level);
		}
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		/* FIXME
		removeSkill(player, 885, 1);

		int level = player.getLevel() - 74;
		if (level > 0)
		{
			removeSkill(player, 884, level);
			removeSkill(player, 886, level);
			removeSkill(player, 888, level);
			removeSkill(player, 891, level);
			removeSkill(player, 911, level);
		}
		*/
	}

	@Override
	public void onUntransform(L2PcInstance player)
	{
		// FIXME: super();
		player.setIsFlyingMounted(false);
	}

	public static void main(String[] args)
	{
		// FIXME: remove when fixed
		// TransformationManager.getInstance().registerTransformation(new AurabirdFalcon());
	}
}