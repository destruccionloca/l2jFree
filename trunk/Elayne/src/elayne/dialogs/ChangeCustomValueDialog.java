package elayne.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author polbat02
 */
public class ChangeCustomValueDialog extends Dialog
{
	private Text accountId;
	private boolean changeConfirmed;
	private Button confirmChange;
	private String newValue;
	private String value;

	public ChangeCustomValueDialog(Shell parentShell, String value)
	{
		super(parentShell);
		this.value = value;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Change " + value);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "&Change Name", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label confirmText = new Label(composite, SWT.NONE);
		confirmText.setText("Which " + value + " would you like to give to this Character?");
		confirmText.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&New " + value + ":");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		accountId = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);

		gridData.widthHint = convertHeightInCharsToPixels(20);
		accountId.setLayoutData(gridData);

		confirmChange = new Button(composite, SWT.CHECK);
		confirmChange.setText("Are you sure you want to change this player's " + value + "?");
		confirmChange.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));

		return composite;
	}

	public boolean getChangeConfirmmed()
	{
		return changeConfirmed;
	}

	public String getNewValue()
	{
		return newValue;
	}

	@Override
	protected void okPressed()
	{
		setNewValue();
		setChangeConfirmed();
		super.okPressed();
	}

	private void setChangeConfirmed()
	{
		changeConfirmed = confirmChange.getSelection();
	}

	private void setNewValue()
	{
		newValue = accountId.getText();
	}
}
