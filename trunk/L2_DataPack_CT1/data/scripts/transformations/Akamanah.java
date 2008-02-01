package transformations;

import net.sf.l2j.gameserver.model.L2Transformation;
import net.sf.l2j.gameserver.instancemanager.TransformationManager;

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
		// id, duration (secs), colRadius, colHeight
		super(302, Integer.MAX_VALUE, 10.0, 32.73);
	}

	public void onTransform()
	{
		// Store real values
		this.getPlayer().getAppearance().setDisplayName(false);
	}

	public void onUntransform()
	{
		this.getPlayer().getAppearance().setDisplayName(true);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Akamanah());
	}
}