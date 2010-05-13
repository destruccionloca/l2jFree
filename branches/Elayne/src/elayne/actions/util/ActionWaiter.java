package elayne.actions.util;

import org.eclipse.jface.action.Action;

import elayne.util.ThreadPoolManager;

/**
 * This class is used to make a certain action wait so it can be used again in a
 * certain amount of time.<br>
 * This is intended to be used with actions that require a connection to a
 * DataBase to avoid server Flooding.<br>
 * More constructors can be added to add new actions to this waiter.<br>
 * @author polbat02
 * @since v.1.0.0
 */
public class ActionWaiter
{
	/** The action that will need to wait on command. */
	private Action action;

	/**
	 * A temporary tool tip used to inform the user that a certain action is
	 * waiting for a certain amount of time.
	 */
	private String toolTip;

	/** Returns the status of the instantiated action */
	private boolean isEnabled = true;

	/** Constructor */
	public ActionWaiter(Action action)
	{
		this.action = action;
	}

	/**
	 * This void adds all the functionality to the class:<br>
	 * 1- Unable the action.<br>
	 * 2- Instantiate the tool tip to be used again later.<br>
	 * 3- Add a need tool tip to advise the user that the action cannot be used
	 * at the moment.<br>
	 * 4- Instantiate the action for the beginning of the Run Action.<br>
	 * 5- Schedule a new Runnable to Enable the action after the given seconds.<br>
	 * @param seconds that the action will need to wait until it can be used
	 * again.
	 * @param action that will need to wait until it can be used again.
	 */
	public void actionWait(int seconds)
	{
		final long time = seconds * 1000L;
		action.setEnabled(false);
		isEnabled = false;
		System.out.println("Action " + action.getText() + " disabled for " + seconds + " seconds.");
		// Save the tool tip text to set it again on enable.
		toolTip = action.getToolTipText();
		// Set a new temporal tool tip text.
		action.setToolTipText("Action temporally disabled to avoid server flooding.");
		// Schedule a timer to enable the action again.
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run() // Enable the action here.
			{
				enableAction();
			}
		}, time);
	}

	/**
	 * Enable the instantiated action and set the former tool tip as the new
	 * tool tip again.
	 */
	public void enableAction()
	{
		action.setEnabled(true);
		isEnabled = true;
		action.setToolTipText(toolTip);
		System.out.println("ActionWaiter: Action " + action.getId() + " Enabled.");
	}

	/** Returns the current status of the action */
	public boolean isEnabled()
	{
		return isEnabled;
	}
}
