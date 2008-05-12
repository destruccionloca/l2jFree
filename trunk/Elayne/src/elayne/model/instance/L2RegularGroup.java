package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

/**
 * This class represents a regular group that can hold any entry that's not
 * specific. This group is used for things that are not used enough times or do
 * not have enough importance as to have their own instance.<br>
 * This {@link L2GroupEntry} defines 2 constructors: one of them defines a
 * simple group sharing a common image, the folder image. The other one, defines
 * a regular group with one specific {@link ImageDescriptor} that will be used
 * to represent the group visually.
 * @author polbat02
 */
public class L2RegularGroup extends L2GroupEntry
{
	private String picLoc;

	/**
	 * Constructor: Defines a new {@link L2RegularGroup}.
	 * @param parent -> The parent group of this group.
	 * @param name -> The name that will represent this group.
	 */
	public L2RegularGroup(L2GroupEntry parent, String name)
	{
		super(parent, name);
	}

	/**
	 * Constructor: Defines a new {@link L2RegularGroup}.
	 * @param parent -> The parent group of this group.
	 * @param name -> The name that will represent this group.
	 * @param picLoc -> The location of the picture that will represent this
	 * group visually.
	 */
	public L2RegularGroup(L2GroupEntry parent, String name, String picLoc)
	{
		super(parent, name);
		this.picLoc = picLoc;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		if (picLoc != null)
		{
			ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, picLoc);
			if (image != null && image.getImageData() != null)
				return image;
		}
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.FOLDER);
	}

}
