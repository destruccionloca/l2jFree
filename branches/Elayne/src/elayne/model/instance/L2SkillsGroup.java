package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

public class L2SkillsGroup extends L2GroupEntry
{

	private L2GroupEntry _subclassGroup;

	public L2SkillsGroup(L2PcInstance parent, String name, L2GroupEntry subclassGroup)
	{
		super(parent, name);
		_subclassGroup = subclassGroup;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SKILLS_GROUP);
	}

	@Override
	public L2PcInstance getParent()
	{
		return (L2PcInstance) parent;
	}

	public L2GroupEntry getSubClassGroup()
	{
		return _subclassGroup;
	}

	public void restore()
	{
		getParent().setPlayerSkills(0, this);
		if (getParent().getPlayerSkillsByClass(0).size() > 0)
		{
			for (L2SkillEntry skill : getParent().getPlayerSkillsByClass(0))
			{
				addEntry(skill);
			}
		}

		if (getEntries().length != 0)
		{
			getParent().addEntry(this);
		}
		else
		{
			getParent().removeEntry(this);
			System.out.println("The player" + getParent().getName() + " has no skills.");
		}
	}
}
