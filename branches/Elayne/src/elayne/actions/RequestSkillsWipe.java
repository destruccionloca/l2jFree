package elayne.actions;

import java.sql.PreparedStatement;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.model.L2Character;
import elayne.model.instance.L2PcInstance;
import elayne.model.instance.L2SkillEntry;
import elayne.util.connector.ServerDB;

public class RequestSkillsWipe extends ElayneAction
{

	private static final String ID = "requestSkillsWipe";

	public RequestSkillsWipe(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Wipe Skills");
		setToolTipText("Wipes all the skills from the selected playrer's class.");
		ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.WIPE_SKILLS);
		setImageDescriptor(image);
	}

	@Override
	public void run()
	{
		Object obj = _selection.getFirstElement();
		L2SkillEntry pse = ((L2SkillEntry) obj);
		if (sendConfirmationMessage("Wipe Skills", "Are you sure that you want to wipe this player's skills (from this class only)?"))
		{
			if (pse.getParent() == null)
			{
				System.out.println("RequestWipeSkills: Error: the L2PcInstance of the skill is null.");
				return;
			}
			int class_index = pse.getClassIndex();
			if (!pse.getParent().getParent().getPlayerSkillsByClass(class_index).isEmpty())
			{
				L2PcInstance player = pse.getParent().getParent();
				if (player.isOnline())
				{
					sendMessage("Can't delete a skill from an Online Player");
					return;
				}
				wipeSkills(player.getObjectId(), class_index);
				for (L2Character entry : pse.getParent().getEntries())
				{
					entry.getParent().removeEntry(entry);
				}
				// Remove the whole skills group.
				pse.getParent().getParent().removeEntry(pse.getParent());

				// Remove all the skills from this
				// L2PcInstance
				player.getPlayerSkillsByClass(class_index).clear();

				_treeViewer.refresh();
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			_selection = (IStructuredSelection) incoming;

			setEnabled(_selection.size() == 1 && _selection.getFirstElement() instanceof L2SkillEntry);
		}
		// Not enable the action.
		else
			setEnabled(false);
	}

	private void wipeSkills(int charObjId, int class_index)
	{
		final String SQL = "DELETE FROM `character_skills` WHERE (`charId`=?) AND (`class_index`=?)  ";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, charObjId);
			statement.setInt(2, class_index);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
