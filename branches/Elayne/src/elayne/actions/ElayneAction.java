package elayne.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import elayne.rmi.RemoteAdministrationClient;

/**
 * The standard abstract implementation of an action in ELAYNE.<br>
 * Classes extending this class will automatically register a selection service
 * that will be removed upon action disposal. The selection service can be used
 * for many things, but in ELAYNE we will mainly be using it to enable or
 * disable a certain action.<br>
 * <br>
 * Classes extending {@link ElayneAction} will also have to define a run method
 * that will be triggered every time an action (enabled) is called.<br>
 * Classes extending this class should be named the following way: (in different
 * stages)<br>
 * 1- What the action does: (main action, not other actions required to perform
 * that action)<br>
 * a) Request for actions requested to the server, it doesn't matter if it's a
 * database action or a RMI related action. A class who's name starts with
 * request ALWAYS has to interact with the server somehow.<br>
 * b) Show: Classes who's name start with Show will try to show something in the
 * parent composite (program window).<br>
 * c) Clear: Classes who's name start with Clear clear some attempt to clear
 * something from the parent composite.<br>
 * 2- The subject to whom the action applies: What will the action modify. I.E:
 * players, clans, window, etc.<br>
 * 3- The action that will be taken. I.E: kick, ban, search, etc...<br>
 * 4- Extra optional information...<br>
 * @author polbat02
 */
public abstract class ElayneAction extends Action implements ISelectionListener, IWorkbenchAction
{
	/**
	 * This is the current selection of the treeViewer or, if no treeViewer is
	 * defined, the selection of some object in the window.
	 */
	protected IStructuredSelection _selection;

	/**
	 * The viewer that triggers the actions.<br>
	 * This {@link TreeViewer} is refreshed on demand and calls this action on
	 * every defined action like {@link DoubleClickEvent} and others...
	 */
	protected TreeViewer _treeViewer;

	/**
	 * This {@link IWorkbenchWindow} is the window on which the selection
	 * service will be registered and the window on which most of the actions
	 * will take place.
	 */
	protected IWorkbenchWindow _window;

	/**
	 * Defines a new action saving the window. Also registers a selection
	 * service (this) into the window.
	 * @param window on which the action will have effect.
	 */
	public ElayneAction(IWorkbenchWindow window)
	{
		_window = window;
		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * Defines a new action saving the window and the treeViewer. Also registers
	 * a selection service (this) into the window.
	 * @param window on which the action will have effect.
	 * @param treeViewer that will be modified by the action (or not).
	 */
	public ElayneAction(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		_window = window;
		_treeViewer = treeViewer;
		window.getSelectionService().addSelectionListener(this);
	}

	public void dispose()
	{
		_window.getSelectionService().removeSelectionListener(this);
		_window = null;
		_treeViewer = null;
		_selection = null;
	}

	/**
	 * Return the selection in the Tree viewer.
	 * @return
	 */
	public IStructuredSelection getSelection()
	{
		return _selection;
	}

	/**
	 * Returns the tree viewer on which we'll be working on.
	 * @return
	 */
	public TreeViewer getTreeViewer()
	{
		return _treeViewer;
	}

	/**
	 * Return the Active Window.
	 * @return
	 */
	public IWorkbenchWindow getWindow()
	{
		return _window;
	}

	/**
	 * Check if the RMI service is connected to the Live L2J Server.
	 * @return true if {@link RemoteAdministrationClient} is connected to the
	 * server.
	 */
	protected boolean isRMIConnected()
	{
		return RemoteAdministrationClient.getInstance().isConnected();
	}

	@Override
	public abstract void run();

	public abstract void selectionChanged(IWorkbenchPart part, ISelection incoming);

	/**
	 * Open up a new Confirmation message Shell for any Action.
	 * @param title
	 * @param message
	 */
	protected boolean sendConfirmationMessage(String title, String message)
	{
		return MessageDialog.openConfirm(_window.getShell(), title, message);
	}

	/**
	 * Open up a new Confirmation message Shell for any Action. Labels are
	 * Yes/No
	 * @param title
	 * @param message
	 */
	protected boolean sendConfirmationMessageWithNoLabel(String title, String message)
	{
		MessageDialog dialog = new MessageDialog(_window.getShell(), title, null, message, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
		return dialog.open() == 0;
	}

	/**
	 * Prompt an error message to the client.
	 * @param errorMessage
	 */
	protected void sendError(String errorMessage)
	{
		MessageDialog.openError(_window.getShell(), "Error", errorMessage);
	}

	/**
	 * Prompt an information message for any Action.
	 * @param message
	 */
	protected void sendMessage(String message)
	{
		MessageDialog.openInformation(_window.getShell(), "Information", message);
	}

	/**
	 * Sets a new Id for this new action.
	 * @param id
	 */
	protected void setNewId(String id)
	{
		setId("elayne.actions." + id);
	}
}
