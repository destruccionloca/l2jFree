package elayne.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import elayne.application.Activator;
import elayne.model.ConnectionDetails;
import elayne.preferences.LoginPreferencePage;

/**
 * Login dialog, which prompts for the user's account info, and has Login and
 * Cancel buttons.
 */
public class LoginDialog extends Dialog
{

	private static final String LAST_USER = "prefs_last_connection";

	private static final String PASSWORD = "password";

	private static final String SAVED = "saved-connections";

	@SuppressWarnings("unchecked")
	public static String[] parseCSL(String csl)
	{
		if (csl == null)
			return null;

		StringTokenizer tokens = new StringTokenizer(csl, ","); //$NON-NLS-1$
		ArrayList array = new ArrayList(10);

		while (tokens.hasMoreTokens())
		{
			array.add(tokens.nextToken().trim());
		}

		return (String[]) array.toArray(new String[array.size()]);
	}

	private ConnectionDetails connectionDetails;

	private Image[] images;

	private Text loginDbHostText;

	private Text loginDbNameText;

	private Text loginDbPasserText;

	private Text loginDbUserText;

	public Text passwordText;

	private Text rmiServerPortText;

	private Text rmiServerText;

	@SuppressWarnings("unchecked")
	public HashMap savedDetails = new HashMap();

	private Text serverDbHostText;

	private Text serverDbNameText;

	private Text serverDbPasserText;

	private Text serverDbUserText;

	public Combo userIdText;

	public LoginDialog(Shell parentShell)
	{
		super(parentShell);
		loadDescriptors();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void buttonPressed(int buttonId)
	{
		String userId = userIdText.getText();
		String password = passwordText.getText();
		connectionDetails = new ConnectionDetails(userId, password);
		savedDetails.put(userId, connectionDetails);
		if (buttonId == IDialogConstants.OK_ID || buttonId == IDialogConstants.CANCEL_ID)
			saveDescriptors();

		IEclipsePreferences prefs = new ConfigurationScope().getNode(Activator.PLUGIN_ID);

		prefs.put(LoginPreferencePage.LOGIN_DB_HOST, loginDbHostText.getText());
		prefs.put(LoginPreferencePage.LOGIN_DB, loginDbNameText.getText());
		prefs.put(LoginPreferencePage.LOGIN_DB_USER, loginDbUserText.getText());
		prefs.put(LoginPreferencePage.LOGIN_DB_PASS, loginDbPasserText.getText());

		prefs.put(LoginPreferencePage.SERVER_DB_HOST, serverDbHostText.getText());
		prefs.put(LoginPreferencePage.SERVER_DB, serverDbNameText.getText());
		prefs.put(LoginPreferencePage.SERVER_DB_USER, serverDbUserText.getText());
		prefs.put(LoginPreferencePage.SERVER_DB_PASS, serverDbPasserText.getText());
		prefs.put(LoginPreferencePage.RMI_SERVER_PASSWORD, rmiServerText.getText());
		prefs.put(LoginPreferencePage.RMI_SERVER_PORT, rmiServerPortText.getText());

		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}

		super.buttonPressed(buttonId);
	}

	@Override
	public boolean close()
	{
		if (images != null)
		{
			for (int i = 0; i < images.length; i++)
				images[i].dispose();
		}
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Elayne Login");

		IProduct product = Platform.getProduct();
		if (product != null)
		{
			String[] imageURLs = parseCSL(product.getProperty(IProductConstants.WINDOW_IMAGES));

			if (imageURLs.length > 0)
			{
				images = new Image[imageURLs.length];
				for (int i = 0; i < imageURLs.length; i++)
				{
					String url = imageURLs[i];
					ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(product.getDefiningBundle().getSymbolicName(), url);
					images[i] = descriptor.createImage(true);
				}
				newShell.setImages(images);
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		Button removeCurrentUser = createButton(parent, IDialogConstants.CLIENT_ID, "&Delete User", false);

		removeCurrentUser.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				savedDetails.remove(userIdText.getText());
				initializeUsers("");
			}
		});

		createButton(parent, IDialogConstants.OK_ID, "&Login", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label accountLabel = new Label(composite, SWT.NONE);
		accountLabel.setText("Account details:");
		accountLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&User ID:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		userIdText = new Combo(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);

		gridData.widthHint = convertHeightInCharsToPixels(20);
		userIdText.setLayoutData(gridData);

		userIdText.addListener(SWT.Modify, new Listener()
		{
			public void handleEvent(Event event)
			{
				ConnectionDetails d = (ConnectionDetails) savedDetails.get(userIdText.getText());
				if (d != null)
				{
					passwordText.setText(d.getPassword());
				}
			}
		});

		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("&Password:");
		passwordLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		final Button autoLogin = new Button(composite, SWT.CHECK);
		autoLogin.setText("Login &automatically at startup");
		autoLogin.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));

		autoLogin.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IEclipsePreferences prefs = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
				prefs.putBoolean(LoginPreferencePage.AUTO_LOGIN, autoLogin.getSelection());
			}
		});

		IPreferencesService service = Platform.getPreferencesService();
		boolean auto_login = service.getBoolean(Activator.PLUGIN_ID, LoginPreferencePage.AUTO_LOGIN, true, null);

		// LOGIN DATABASE STUFF

		Label loginLabel = new Label(composite, SWT.NONE);
		loginLabel.setText("Login database details:");
		loginLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		String loginDbHost = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB_HOST, "127.0.0.1", null);
		Label loginDbHostLabel = new Label(composite, SWT.NONE);
		loginDbHostLabel.setText("&Login database IP:");
		loginDbHostLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		loginDbHostText = new Text(composite, SWT.BORDER);
		loginDbHostText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		loginDbHostText.setText(loginDbHost);

		String loginDbName = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB, "l2jdb", null);
		Label loginDbNameLabel = new Label(composite, SWT.NONE);
		loginDbNameLabel.setText("&Login database Name:");
		loginDbNameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		loginDbNameText = new Text(composite, SWT.BORDER);
		loginDbNameText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		loginDbNameText.setText(loginDbName);

		String loginDbUser = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB_USER, "root", null);
		Label loginDbUserLabel = new Label(composite, SWT.NONE);
		loginDbUserLabel.setText("&Login database User:");
		loginDbUserLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		loginDbUserText = new Text(composite, SWT.BORDER);
		loginDbUserText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		loginDbUserText.setText(loginDbUser);

		String loginDbPassword = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB_PASS, "password", null);
		Label loginDbPassLabel = new Label(composite, SWT.NONE);
		loginDbPassLabel.setText("&Login database Password:");
		loginDbPassLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		loginDbPasserText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		loginDbPasserText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		loginDbPasserText.setText(loginDbPassword);

		// SERVER DATABASE STUFF

		Label serverLabel = new Label(composite, SWT.NONE);
		serverLabel.setText("Server database details:");
		serverLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		String serverDbHost = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB_HOST, "127.0.0.1", null);
		Label serverDbHostLabel = new Label(composite, SWT.NONE);
		serverDbHostLabel.setText("&Server database IP:");
		serverDbHostLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		serverDbHostText = new Text(composite, SWT.BORDER);
		serverDbHostText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		serverDbHostText.setText(serverDbHost);

		String serverDbName = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB, "l2jdb", null);
		Label serverDbNameLabel = new Label(composite, SWT.NONE);
		serverDbNameLabel.setText("&Server database Name:");
		serverDbNameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		serverDbNameText = new Text(composite, SWT.BORDER);
		serverDbNameText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		serverDbNameText.setText(serverDbName);

		String serverDbUser = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB_USER, "root", null);
		Label serverDbUserLabel = new Label(composite, SWT.NONE);
		serverDbUserLabel.setText("&Server database User:");
		serverDbUserLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		serverDbUserText = new Text(composite, SWT.BORDER);
		serverDbUserText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		serverDbUserText.setText(serverDbUser);

		String serverDbPassword = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB_PASS, "password", null);
		Label serverDbPassLabel = new Label(composite, SWT.NONE);
		serverDbPassLabel.setText("&Login database Password:");
		serverDbPassLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		serverDbPasserText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		serverDbPasserText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		serverDbPasserText.setText(serverDbPassword);

		// RMI SERVER STUFF
		final Button autoLoginRMI = new Button(composite, SWT.CHECK);
		autoLoginRMI.setText("Connect to RMI server at startup");
		autoLoginRMI.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));

		autoLoginRMI.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IEclipsePreferences prefs = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
				prefs.putBoolean(LoginPreferencePage.AUTO_LOGIN_RMI, autoLoginRMI.getSelection());
			}
		});
		boolean auto_logn_rmi = service.getBoolean(Activator.PLUGIN_ID, LoginPreferencePage.AUTO_LOGIN_RMI, false, null);

		String rmiServerPassword = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.RMI_SERVER_PASSWORD, "password", null);
		Label rmiServerLabel = new Label(composite, SWT.NONE);
		rmiServerLabel.setText("&RMI Server Password:");
		rmiServerLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		rmiServerText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		rmiServerText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		rmiServerText.setText(rmiServerPassword);

		String rmiServerPort = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.RMI_SERVER_PORT, "1099", null);
		Label rmiServerPortLabel = new Label(composite, SWT.NONE);
		rmiServerPortLabel.setText("&RMI Server Port:");
		rmiServerPortLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		rmiServerPortText = new Text(composite, SWT.BORDER);
		rmiServerPortText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		rmiServerPortText.setText(rmiServerPort);

		autoLogin.setSelection(auto_login);
		autoLoginRMI.setSelection(auto_logn_rmi);

		String lastUser = "none";
		if (connectionDetails != null)
			lastUser = connectionDetails.getUserId();

		initializeUsers(lastUser);

		return composite;
	}

	/**
	 * Returns the connection details entered by the user, or <code>null</code>
	 * if the dialog was canceled.
	 */
	public ConnectionDetails getConnectionDetails()
	{
		return connectionDetails;
	}

	@SuppressWarnings("unchecked")
	protected void initializeUsers(String defaultUser)
	{
		userIdText.removeAll();
		passwordText.setText("");
		for (Iterator it = savedDetails.keySet().iterator(); it.hasNext();)
			userIdText.add((String) it.next());
		int index = Math.max(userIdText.indexOf(defaultUser), 0);
		userIdText.select(index);
	}

	@SuppressWarnings("unchecked")
	private void loadDescriptors()
	{
		try
		{
			Preferences preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
			Preferences connections = preferences.node(SAVED);
			String[] userNames = connections.childrenNames();

			for (int i = 0; i < userNames.length; i++)
			{
				String userName = userNames[i];
				Preferences node = connections.node(userName);
				savedDetails.put(userName, new ConnectionDetails(userName, node.get(PASSWORD, "")));
			}
			connectionDetails = (ConnectionDetails) savedDetails.get(preferences.get(LAST_USER, ""));
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void okPressed()
	{
		if (connectionDetails.getUserId().equals(""))
		{
			MessageDialog.openError(getShell(), "Invalid User ID", "User ID field must not be blank.");
			return;
		}
		super.okPressed();
	}

	@SuppressWarnings("unchecked")
	public void saveDescriptors()
	{
		Preferences preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
		preferences.put(LAST_USER, connectionDetails.getUserId());
		Preferences connections = preferences.node(SAVED);
		for (Iterator it = savedDetails.keySet().iterator(); it.hasNext();)
		{
			String name = (String) it.next();
			ConnectionDetails d = (ConnectionDetails) savedDetails.get(name);
			Preferences connection = connections.node(name);
			connection.put(PASSWORD, d.getPassword());
		}
		try
		{
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}
	}
}
