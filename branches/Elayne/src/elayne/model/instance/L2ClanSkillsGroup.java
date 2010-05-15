package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

public class L2ClanSkillsGroup extends L2GroupEntry
{

	public L2ClanSkillsGroup(L2Clan parent, String name)
	{
		super(parent, name);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SKILLS_GROUP);
	}

	@Override
	public L2Clan getParent()
	{
		return (L2Clan) _parent;
	}
}
