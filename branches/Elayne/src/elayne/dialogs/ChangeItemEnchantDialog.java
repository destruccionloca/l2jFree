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

import elayne.actions.RequestItemEnchant;

/**
 * This class represents a new Dialog that is prompted by the
 * {@link RequestItemEnchant} action. It displays a dialog that asks for a
 * new enchant that will be given to a certain item.
 * @author SqueezeD
 */
public class ChangeItemEnchantDialog extends Dialog
{
	/** The old enchant */
	private int actualEnchant;
	/** The new enchant */
	private int changeLevel;
	/**
	 * The spinner that is used by the user to place in the new enchant for a
	 * certain item
	 */
	private Spinner spinner;

	/**
	 * Defines a new instance of {@link ChangeItemEnchantDialog}.
	 * @param parentShell
	 * @param actualEnchant
	 */
	public ChangeItemEnchantDialog(Shell parentShell, int actualEnchant)
	{
		super(parentShell);
		this.actualEnchant = actualEnchant;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Change Item enchant");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "&Change Enchant", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label confirmText = new Label(composite, SWT.NONE);
		confirmText.setText("Insert the new enchant number:");
		confirmText.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("Enchant:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		spinner = new Spinner(composite, SWT.BORDER);
		spinner.setMinimum(0);
		spinner.setMaximum(Integer.MAX_VALUE);
		spinner.setSelection(actualEnchant);
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
