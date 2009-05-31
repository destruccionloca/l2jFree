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
 * @author Kerberos, Respawner
 */
public class AurabirdOwl extends L2DefaultTransformation
{
	public AurabirdOwl()
	{
		// id, colRadius, colHeight
		super(9, 40.0, 19.0);
	}

	@Override
	public void onTransform(L2PcInstance player)
	{
		super();
		player.setIsFlyingMounted(true);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		super();
		addSkills(player, 884, 885, 886, 887, 889, 892, 893, 895, 911, 932);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkills(player, 884, 885, 886, 887, 889, 892, 893, 895, 911, 932);
	}

	@Override
	public void onUntransform(L2PcInstance player)
	{
		super();
		player.setIsFlyingMounted(false);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new AurabirdOwl());
	}
}