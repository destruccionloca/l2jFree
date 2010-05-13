package elayne.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import elayne.actions.RequestItemAward;
import elayne.datatables.ArmorTable;
import elayne.datatables.ItemTable;
import elayne.datatables.WeaponTable;
import elayne.templates.L2Armor;
import elayne.templates.L2Item;
import elayne.templates.L2Weapon;

/**
 * This class represents a new Dialog that is prompted by the
 * {@link RequestItemAward} action. It displays a dialog that asks for an item
 * to reward and the amount of items that will need to be added for that
 * particular item.
 * @author polbat02
 */
public class AwardItemDialog extends Dialog
{
	/** The new amount */
	private int amount;
	private int id;
	private Combo itemNameField;
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
	public AwardItemDialog(Shell parentShell)
	{
		super(parentShell);
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
		createButton(parent, IDialogConstants.OK_ID, "&Award Item", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label itemLabel = new Label(composite, SWT.NONE);
		itemLabel.setText("&Item Name:");
		itemLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		itemNameField = new Combo(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);

		gridData.widthHint = convertHeightInCharsToPixels(20);
		itemNameField.setLayoutData(gridData);

		Label confirmText = new Label(composite, SWT.NONE);
		confirmText.setText("Insert the new amount number:");
		confirmText.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		itemLabel = new Label(composite, SWT.NONE);
		itemLabel.setText("Amount:");
		itemLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		spinner = new Spinner(composite, SWT.BORDER);
		spinner.setMinimum(0);
		spinner.setMaximum(Integer.MAX_VALUE);
		spinner.setSelection(1);
		spinner.setIncrement(1);
		spinner.setPageIncrement(100);
		spinner.pack();
		gridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		gridData.widthHint = convertHeightInCharsToPixels(20);
		spinner.setLayoutData(gridData);

		initNameField("Adena");

		return composite;
	}

	/**
	 * @return The new amount that the item will be granted.
	 */
	public int getAmount()
	{
		return amount;
	}

	public int getItemId()
	{
		return id;
	}

	protected void initNameField(String defaultItem)
	{
		itemNameField.removeAll();
		itemNameField.setText("");

		// Add Weapons:
		for (L2Weapon weapon : WeaponTable.getInstance().getAllWeapons())
			itemNameField.add(weapon.getName());

		// Add Armors:
		for (L2Armor armor : ArmorTable.getInstance().getAllArmors())
			itemNameField.add(armor.getName());

		// Add Items:
		for (L2Item item : ItemTable.getInstance().getAllItems())
			itemNameField.add(item.getName());

		int index = Math.max(itemNameField.indexOf(defaultItem), 0);
		itemNameField.select(index);
	}

	@Override
	protected void okPressed()
	{
		String itemName = itemNameField.getText();
		int itemId = 0;

		int weaponId = WeaponTable.getInstance().getWeaponId(itemName);
		if (weaponId != 0)
			itemId = weaponId;
		else
		{
			int armorId = ArmorTable.getInstance().getArmorId(itemName);
			if (armorId != 0)
				itemId = armorId;
			else
			{
				int etcItemId = ItemTable.getInstance().getItemId(itemName);
				if (etcItemId != 0)
					itemId = etcItemId;
			}
		}

		if (itemId == 0)
		{
			MessageDialog.openError(getShell(), "No Id found", "No Id could be found for the given item name.");
			return;
		}

		setId(itemId);
		setAmount(spinner.getSelection());
		super.okPressed();
	}

	/**
	 * Sets the new amount for the item.
	 * @param selection -> The new amount that the item will be granted.
	 */
	private void setAmount(int selection)
	{
		amount = selection;
	}

	private void setId(int itemId)
	{
		id = itemId;
	}
}
