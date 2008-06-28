package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class Venom extends L2DefaultTransformation
{
	public Venom()
	{
		// id, colRadius, colHeight
		super(307, 11.0, 25.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
	}

	public void removeSkills(L2PcInstance player)
	{
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Venom());
	}
}