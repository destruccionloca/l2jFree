package transformations;

import net.sf.l2j.gameserver.model.L2Transformation;
import net.sf.l2j.gameserver.instancemanager.TransformationManager;

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
		// id, duration (secs), colRadius, colHeight
		// "infinite" duration - ended manually
		super(301, Integer.MAX_VALUE, 9.0, 30.0);
	}

	public void onTransform()
	{
		this.getPlayer().getAppearance().setDisplayName(false);
	}

	public void onUntransform()
	{
		this.getPlayer().getAppearance().setDisplayName(true);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Zariche());
	}
}
