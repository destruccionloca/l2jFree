package transformations;

import net.sf.l2j.gameserver.model.L2Transformation;
import net.sf.l2j.gameserver.instancemanager.TransformationManager;

/**
 * This is currently only a test of the java script engine
 * 
 * @author durgus
 *
 */
public class DoomWraith extends L2Transformation
{
	public DoomWraith()
	{
		// id, duration (secs), colRadius, colHeight
                // Retail Like 30 min - Skatershi
		super(2, 1800, 13.0, 25.0);
	}

	public void onTransform()
	{
	}

	public void onUntransform()
	{
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DoomWraith());
	}
}
