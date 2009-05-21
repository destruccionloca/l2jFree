package elayne.model;

import org.eclipse.core.runtime.PlatformObject;

import elayne.model.instance.L2GroupEntry;

/**
 * Abstract mother class of ANY entry or object shown to the user.
 * @author polbat02
 */
public abstract class L2Character extends PlatformObject
{
	/** Returns the name of this Entry/Group. */
	public abstract String getName();

	/** Returns the parent of this Entry/ Group. May be null. */
	public abstract L2GroupEntry getParent();
}
