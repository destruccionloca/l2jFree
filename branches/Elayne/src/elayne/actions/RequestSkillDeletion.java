package elayne.actions;

import java.sql.PreparedStatement;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.model.instance.L2PcInstance;
import elayne.model.instance.L2SkillEntry;
import elayne.util.connector.ServerDB;

public class RequestSkillDeletion extends ElayneAction
{

	private static final String ID = "requestSkillDeletion";

	public RequestSkillDeletion(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Delete Skill");
		setToolTipText("Delete a skill from a player");
		ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.DELETE_SKILL);
		setImageDescriptor(image);
	}

	private void removeSkill(int charObjId, int skillId, int class_index)
	{
		final String SQL = "DELETE FROM `character_skills` WHERE (`charId`=?) AND (`skill_id`=?) AND (`class_index`=?)  ";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, charObjId);
			statement.setInt(2, skillId);
			statement.setInt(3, class_index);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		L2SkillEntry pse = ((L2SkillEntry) obj);
		if (sendConfirmationMessage("Remove Skill", "Are you sure that you want to delete the skill " + pse.getName() + " from this player?"))
		{
			if (pse.getParent() == null)
			{
				System.out.println("RequestDeleteSkill: Error: the L2SkillGroup of the skill is null.");
				return;
			}

			int class_index = pse.getClassIndex();
			L2PcInstance player = pse.getParent().getParent();
			if (player == null)
				return;
			if (player.getPlayerSkillsByClass(class_index) == null)
				return;
			if (!player.getPlayerSkillsByClass(class_index).isEmpty())
			{
				if (player.isOnline())
				{
					sendMessage("Can't delete a skill from an Online Player");
					return;
				}
				removeSkill(player.getObjectId(), pse.getSkillId(), class_index);
				pse.getParent().removeEntry(pse);

				// Remove the skill from this L2PcInstance
				List<L2SkillEntry> skills = player.getPlayerSkillsByClass(class_index);
				skills.remove(pse);
				if (skills.contains(pse))
					System.out.println("The player Still contains the skill! oO!");
				sendMessage("Skill removed correctly.");
				treeViewer.refresh();
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;

			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2SkillEntry);
		}
		// Not enable the action.
		else
			setEnabled(false);
	}
}
