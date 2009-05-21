package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This is currently only a test of the java script engine
 * 
 * @author durgus
 *
 */
public class Akamanah extends L2Transformation
{
	public Akamanah()
	{
		// id, colRadius, colHeight
		super(302, 10.0, 32.73);
	}

	public void onTransform(L2PcInstance player)
	{
		// Set charachter name to transformed name
		player.getAppearance().setVisibleName("Akamanah");
		player.getAppearance().setVisibleTitle("");

		// Void Burst, Void Flow
		addSkill(player, 3630, 1);
		addSkill(player, 3631, 1);
	}

	public void onUntransform(L2PcInstance player)
	{
		// set character back to true name.
		player.getAppearance().setVisibleName(null);
		player.getAppearance().setVisibleTitle(null);

		// Void Burst, Void Flow
		removeSkill(player, 3630);
		removeSkill(player, 3631);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Akamanah());
	}
}