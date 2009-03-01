package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This is currently only a test of the java script engine
 *
 * @author KenM
 *
 */
public class Zariche extends L2Transformation
{
	public Zariche()
	{
		// id, colRadius, colHeight
		super(301, 9.0, 31.0);
	}

	public void onTransform(L2PcInstance player)
	{
		// Set charachter name to transformed name
		player.getAppearance().setVisibleName("Zariche");
		player.getAppearance().setVisibleTitle("");

		addSkill(player, 3630, 1); // Void Burst
		addSkill(player, 3631, 1); // Void Flow
	}

	public void onUntransform(L2PcInstance player)
	{
		// set character back to true name.
		player.getAppearance().setVisibleName(null);
		player.getAppearance().setVisibleTitle(null);

		removeSkill(player, 3630); // Void Burst
		removeSkill(player, 3631); // Void Flow
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Zariche());
	}
}