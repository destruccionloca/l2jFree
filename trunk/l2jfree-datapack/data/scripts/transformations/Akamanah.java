package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This is currently only a test of the java script engine
 *
 * @author durgus
 *
 */
public class Akamanah extends L2DefaultTransformation
{
	public Akamanah()
	{
		// id, colRadius, colHeight
		super(302, 10.0, 32.73);
	}

	@Override
	public void onTransform(L2PcInstance player)
	{
		// Set charachter name to transformed name
		player.getAppearance().setVisibleName("Akamanah");
		player.getAppearance().setVisibleTitle("");
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 3630, 1); // Void Burst
		addSkill(player, 3631, 1); // Void Flow
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 3630); // Void Burst
		removeSkill(player, 3631); // Void Flow
	}	

	@Override
	public void onUntransform(L2PcInstance player)
	{
		// set character back to true name.
		player.getAppearance().setVisibleName(null);
		player.getAppearance().setVisibleTitle(null);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Akamanah());
	}
}