package elayne.model;

import org.eclipse.jface.viewers.TreeViewer;

import elayne.model.instance.L2GroupEntry;
import elayne.model.instance.L2RegularGroup;

/**
 * Defines the start point of any {@link TreeViewer} in ELAYNE. This class is
 * used to define root points in tree viewers. This root entries are not shown
 * in the treeViewer but they are vital for the display of the viewer.
 * @author polbat02
 */
public class L2RootSession
{
	/** The root group itself */
	private L2GroupEntry _rootGroup;

	/**
	 * Constructor
	 */
	public L2RootSession()
	{}

	/**
	 * Return a clean root (even if it's null will return null).
	 * @return
	 */
	public L2GroupEntry getCleanRoot()
	{
		return _rootGroup;
	}

	/**
	 * Returns the current root group, which if null is defined as new.
	 * @return
	 */
	public L2GroupEntry getRoot()
	{
		if (_rootGroup == null)
			_rootGroup = new L2RegularGroup(null, "Root Group");
		return _rootGroup;
	}
}
