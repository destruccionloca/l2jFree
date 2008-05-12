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
import org.eclipse.swt.widgets.Text;

/**
 * @author polbat02
 */
public class AnnouncementDialog extends Dialog
{
	private Text announcement;
	private String newAnnouncement;

	public AnnouncementDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Perform announcement");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "&Perform Announcement", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label confirmText = new Label(composite, SWT.NONE);
		confirmText.setText("Insert an announcement that will be broadcasted to all online players.");
		confirmText.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("Announcement:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		announcement = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);

		gridData.widthHint = convertHeightInCharsToPixels(20);
		announcement.setLayoutData(gridData);

		return composite;
	}

	public String getNewAnnouncement()
	{
		return newAnnouncement;
	}

	@Override
	protected void okPressed()
	{
		setNewAnnouncement();
		super.okPressed();
	}

	private void setNewAnnouncement()
	{
		newAnnouncement = announcement.getText();
	}
}
