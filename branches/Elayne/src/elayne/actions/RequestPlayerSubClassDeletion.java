/**
 * 
 */
package elayne.actions;

import java.sql.PreparedStatement;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import elayne.model.instance.L2PcInstance;
import elayne.model.instance.L2SubClass;
import elayne.util.connector.ServerDB;

/**
 * @author polbat02
 */
public class RequestPlayerSubClassDeletion extends ElayneAction
{

	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_charId=? AND class_index=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE charId=? AND class_index=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE charId=? AND class_index=?";
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";
	private static final String ID = "requestPlayerSubClassDeletion";

	/**
	 * @param window
	 * @param treeViewer
	 */
	public RequestPlayerSubClassDeletion(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setId(ID);
		setText("Delete Sub Class");
		setToolTipText("Delete a Sub Class from a player");
	}

	/**
	 * 1. Completely erase any sub class linked to a given classIndex.<BR>
	 * @param int classIndex
	 * @param int newClassId
	 * @return boolean subclassAdded
	 */
	private boolean modifySubClass(int classIndex, int objectId)
	{
		java.sql.Connection con = null;

		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement;

			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_HENNAS);
			statement.setInt(1, objectId);
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();

			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
			statement.setInt(1, objectId);
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();

			// Remove all effects info stored for this sub-class.
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, objectId);
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();

			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SKILLS);
			statement.setInt(1, objectId);
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();

			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
			statement.setInt(1, objectId);
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#run()
	 */
	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		L2SubClass group = (L2SubClass) obj;
		if (group == null)
			return;
		L2PcInstance player = group.getParent().getParent();
		if (player == null)
			return;

		if (player.isOnline())
		{
			sendMessage("You can't Modify an online Player, sorry.");
			return;
		}
		if (sendConfirmationMessage("Remove Sub Class", "Are you sure that you want to remove the " + group.getName() + " sub class from the player " + player.getName() + "?"))
		{
			// DELETE THE SUB CLASS HERE!
			if (modifySubClass(group.getClassIndex(), player.getObjectId()))
			{
				player.getSubs().remove(group);
				System.out.println("RequestDeleteSubClass: Removed Sub Class " + group.getName());

				group.getParent().removeEntry(group);

				treeViewer.refresh();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2SubClass);
		}
		else
			setEnabled(false);
	}
}
