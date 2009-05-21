package elayne.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import elayne.ations.RequestItemCountChange;

/**
 * This class represents a new Dialog that is prompted by the
 * {@link RequestItemCountChange} action. It displays a dialog that asks for a
 * new amount that will be given to a certain item.
 * @author polbat02
 */
public class ChangeItemCountDialog extends Dialog
{
	/** The old amount */
	private int actualAmount;
	/** The new amount */
	private int changeLevel;
	/**
	 * The spinner that is used by the user to place in the new amount for a
	 * certain item
	 */
	private Spinner spinner;

	/**
	 * Defines a new instance of {@link ChangeItemCountDialog}.
	 * @param parentShell
	 * @param actualAmount
	 */
	public ChangeItemCountDialog(Shell parentShell, int actualAmount)
	{
		super(parentShell);
		this.actualAmount = actualAmount;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Change Item count");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "&Change Amount", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label confirmText = new Label(composite, SWT.NONE);
		confirmText.setText("Insert the new amount number:");
		confirmText.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("Amount:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		spinner = new Spinner(composite, SWT.BORDER);
		spinner.setMinimum(0);
		spinner.setMaximum(Integer.MAX_VALUE);
		spinner.setSelection(actualAmount);
		spinner.setIncrement(1);
		spinner.setPageIncrement(100);
		spinner.pack();
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		gridData.widthHint = convertHeightInCharsToPixels(20);
		spinner.setLayoutData(gridData);

		return composite;
	}

	/**
	 * @return The new amount that the item will be granted.
	 */
	public int getNewChangeLevel()
	{
		return changeLevel;
	}

	@Override
	protected void okPressed()
	{
		setNewChangeLevel(spinner.getSelection());
		super.okPressed();
	}

	/**
	 * Sets the new amount for the item.
	 * @param selection -> The new amount that the item will be granted.
	 */
	private void setNewChangeLevel(int selection)
	{
		changeLevel = selection;
	}
}
