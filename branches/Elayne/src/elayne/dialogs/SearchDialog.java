/**
 * 
 */
package elayne.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

/**
 * @author polbat02
 */
public class SearchDialog extends Dialog
{

	public static final int SEARCH_BY_ACCOUNT = 3;
	public static final int SEARCH_BY_NAME = 1;
	public static final int SEARCH_BY_OBJECT_ID = 4;
	public static final int SEARCH_BY_TITLE = 2;
	public static final int SEARCH_BY_LAST_ACTIVE = 8;
	public static final int SEARCH_CLAN_BY_ID = 6;
	public static final int SEARCH_CLAN_BY_NAME = 5;
	public static final int SEARCH_FOR_HEROES = 7;
	private Button clanCaseSensitive;
	private Text clanSearchInputText;
	private TabItem clanTab;
	private Text playerSearchInputText;
	private TabItem playerTab;
	private Button searchClanByName;
	private Button searchClanByObjectId;
	private Button searchForHeroPlayers;
	private Button searchForNobles;
	private String searchInput;
	private Button searchPlayerByAccount;
	private Button searchPlayerByName;
	private Button searchPlayerByObjectId;
	private Button searchPlayerByTitle;
	private Button searchPlayerByLastActive;
	private int searchType;

	/**
	 * @param parentShell
	 */
	public SearchDialog(IShellProvider parentShell)
	{
		super(parentShell);
	}

	/**
	 * @param parentShell
	 */
	public SearchDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	protected void buttonPressed(int buttonId)
	{
		if (buttonId == IDialogConstants.OK_ID)
		{
			if (clanTab.getControl().isVisible())
			{
				if (searchClanByName.getSelection())
					searchType = SEARCH_CLAN_BY_NAME;
				else if (searchClanByObjectId.getSelection())
					searchType = SEARCH_CLAN_BY_ID;
				searchInput = clanSearchInputText.getText();
			}
			if (playerTab.getControl().isVisible())
			{
				if (searchForHeroPlayers.getSelection())
					searchType = SEARCH_FOR_HEROES;
				else
				{
					if (searchPlayerByName.getSelection())
						searchType = SEARCH_BY_NAME;
					else if (searchPlayerByTitle.getSelection())
						searchType = SEARCH_BY_TITLE;
					else if (searchPlayerByAccount.getSelection())
						searchType = SEARCH_BY_ACCOUNT;
					else if (searchPlayerByObjectId.getSelection())
						searchType = SEARCH_BY_OBJECT_ID;
					else if (searchPlayerByLastActive.getSelection())
						searchType = SEARCH_BY_LAST_ACTIVE;
					searchInput = playerSearchInputText.getText();

					if (playerSearchInputText.getText().equals(""))
					{
						MessageDialog.openError(getShell(), "Invalid Search", "The search field must not be left blank.");
						return;
					}
					if (playerSearchInputText.getText().length() < 3 && searchType != SEARCH_BY_LAST_ACTIVE)
					{
						MessageDialog.openError(getShell(), "Invalid Search", "The search field must have at least 3 characters");
						return;
					}
				}
			}
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Search Panel");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "&Search", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.DIALOG_TRIM);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridLayout playerTabLayout = new GridLayout(1, false);
		tabFolder.setLayout(playerTabLayout);

		playerTab = new TabItem(tabFolder, SWT.NONE);
		playerTab.setText("Player Search");
		playerTab.setToolTipText("Search online or offline players the server.");
		playerTab.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.HUMAN_MALE).createImage());

		clanTab = new TabItem(tabFolder, SWT.NONE);
		clanTab.setText("Clan Search");
		clanTab.setToolTipText("Search for a clan on the server.");
		clanTab.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CLAN_GROUP).createImage());

		fillPlayerSearch(playerTab, tabFolder);
		fillClanSearch(clanTab, tabFolder);

		return parent;
	}

	private void fillClanSearch(TabItem item, TabFolder tabFolder)
	{
		Composite composite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		item.setControl(composite);

		Label searchInputLabel = new Label(composite, SWT.NONE);
		searchInputLabel.setText("&Search Clan (containing text):");
		searchInputLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));

		clanSearchInputText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1);
		gridData.widthHint = convertHeightInCharsToPixels(15);
		clanSearchInputText.setLayoutData(gridData);

		clanCaseSensitive = new Button(composite, SWT.CHECK);
		clanCaseSensitive.setText("Case sensitive  [NOT WORKING YET]");
		clanCaseSensitive.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));

		Group limitToGroup = new Group(composite, SWT.SHADOW_IN);
		limitToGroup.setText("Specific Search Options");
		limitToGroup.setLayout(new GridLayout(2, false));
		limitToGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));

		searchClanByName = new Button(limitToGroup, SWT.RADIO);
		searchClanByName.setText("Search Clan by name");
		searchClanByName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		searchClanByName.setSelection(true);

		searchClanByObjectId = new Button(limitToGroup, SWT.RADIO);
		searchClanByObjectId.setText("Search Clan by object id");
		searchClanByObjectId.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
	}

	private void fillPlayerSearch(TabItem item, TabFolder tabFolder)
	{
		Composite composite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		item.setControl(composite);

		Label searchInputLabel = new Label(composite, SWT.NONE);
		searchInputLabel.setText("&Search Player (containing text):");
		searchInputLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BOTTOM, true, false, 2, 1));

		playerSearchInputText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = convertHeightInCharsToPixels(15);
		playerSearchInputText.setLayoutData(gridData);

		Group otherOptionsGroup = new Group(composite, SWT.SHADOW_IN);
		otherOptionsGroup.setText("Other Search Options");
		otherOptionsGroup.setLayout(new GridLayout(2, false));
		otherOptionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		searchForHeroPlayers = new Button(otherOptionsGroup, SWT.CHECK);
		searchForHeroPlayers.setText("Hero players");
		searchForHeroPlayers.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));

		searchForNobles = new Button(otherOptionsGroup, SWT.CHECK);
		searchForNobles.setText("Noble players");
		searchForNobles.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));

		Group limitToGroup = new Group(composite, SWT.SHADOW_IN);
		limitToGroup.setText("Specific Search Options");
		limitToGroup.setLayout(new GridLayout(2, false));
		limitToGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

		searchPlayerByName = new Button(limitToGroup, SWT.RADIO);
		searchPlayerByName.setText("Search player by name");
		searchPlayerByName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		// Default search
		searchPlayerByName.setSelection(true);

		searchPlayerByTitle = new Button(limitToGroup, SWT.RADIO);
		searchPlayerByTitle.setText("Search player by title");
		searchPlayerByTitle.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));

		searchPlayerByAccount = new Button(limitToGroup, SWT.RADIO);
		searchPlayerByAccount.setText("Search player by account");
		searchPlayerByAccount.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));

		searchPlayerByObjectId = new Button(limitToGroup, SWT.RADIO);
		searchPlayerByObjectId.setText("Search player by object id");
		searchPlayerByObjectId.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));

		searchPlayerByLastActive = new Button(limitToGroup, SWT.RADIO);
		searchPlayerByLastActive.setText("Not connected since (Month input)");
		searchPlayerByLastActive.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
	}

	public String getSearchInput()
	{
		return searchInput;
	}

	public int getSearchType()
	{
		return searchType;
	}
}
