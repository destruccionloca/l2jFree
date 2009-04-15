package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class LightPurpleManedHorse extends L2DefaultTransformation
{
	public LightPurpleManedHorse()
	{
		// id, duration (secs), colRadius, colHeight
		super(106, 1800, 12.0, 32.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
	}

	public void removeSkills(L2PcInstance player)
	{
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new LightPurpleManedHorse());
	}
}