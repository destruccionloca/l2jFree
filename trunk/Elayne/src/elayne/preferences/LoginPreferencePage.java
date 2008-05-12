package elayne.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import elayne.application.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class LoginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private final static String newline = "\n";

	public static final String AUTO_LOGIN = "prefs_auto_login";

	public static final String LOGIN_DB_HOST = "prefs_login_db_host";
	public static final String LOGIN_DB = "prefs_login_db";
	public static final String LOGIN_DB_USER = "prefs_login_db_user";
	public static final String LOGIN_DB_PASS = "prefs_login_db_pass";

	public static final String SERVER_DB_HOST = "prefs_server_db_host";
	public static final String SERVER_DB = "prefs_server_db";
	public static final String SERVER_DB_USER = "prefs_server_db_user";
	public static final String SERVER_DB_PASS = "prefs_server_db_pass";

	public static final String RMI_SERVER_PASSWORD = "prefs_rmi_server_password";
	public static final String RMI_SERVER_PORT = "prefs_rmi_server_port";
	public static final String AUTO_LOGIN_RMI = "prefs_auto_login_rmi";

	private ScopedPreferenceStore preferences;

	public LoginPreferencePage()
	{
		super(GRID);
		this.preferences = new ScopedPreferenceStore(new ConfigurationScope(), Activator.PLUGIN_ID);
		setPreferenceStore(preferences);
		setDescription("Login Preferences used by the program to connect to the Server." + newline + "This preferences will determine which login db and which server database should be "
								+ "checked upon login and runtime of the software.");
	}

	public void init(IWorkbench workbench)
	{}

	@Override
	protected void createFieldEditors()
	{
		// Log In automatically
		BooleanFieldEditor boolEditor = new BooleanFieldEditor(AUTO_LOGIN, "Login automatically at startup", getFieldEditorParent());
		addField(boolEditor);

		// Login DB Stuff
		StringFieldEditor loginDbEditor = new StringFieldEditor(LOGIN_DB_HOST, "Login DB Host:", getFieldEditorParent());
		addField(loginDbEditor);

		StringFieldEditor loginDb = new StringFieldEditor(LOGIN_DB, "Login DB name:", getFieldEditorParent());
		addField(loginDb);

		StringFieldEditor loginDbUser = new StringFieldEditor(LOGIN_DB_USER, "Login DB user:", getFieldEditorParent());
		addField(loginDbUser);

		StringFieldEditor loginDbPass = new StringFieldEditor(LOGIN_DB_PASS, "Login DB password:", getFieldEditorParent());
		addField(loginDbPass);

		// SERVER DB STUFF
		StringFieldEditor sDbEditor = new StringFieldEditor(SERVER_DB_HOST, "Server DB Host:", getFieldEditorParent());
		addField(sDbEditor);

		StringFieldEditor sDb = new StringFieldEditor(SERVER_DB, "Server DB name:", getFieldEditorParent());
		addField(sDb);

		StringFieldEditor sDbUser = new StringFieldEditor(SERVER_DB_USER, "Server DB user:", getFieldEditorParent());
		addField(sDbUser);

		StringFieldEditor sDbPass = new StringFieldEditor(SERVER_DB_PASS, "Server DB password:", getFieldEditorParent());
		addField(sDbPass);

		// RMI SERVER STUFF
		BooleanFieldEditor autoRMI = new BooleanFieldEditor(AUTO_LOGIN_RMI, "Connect to RMI server at startup", getFieldEditorParent());
		addField(autoRMI);

		StringFieldEditor rmiPass = new StringFieldEditor(RMI_SERVER_PASSWORD, "RMI Server password:", getFieldEditorParent());
		addField(rmiPass);

		StringFieldEditor rmiPort = new StringFieldEditor(RMI_SERVER_PORT, "RMI Server port:", getFieldEditorParent());
		addField(rmiPort);
	}

	@Override
	public boolean performOk()
	{
		try
		{
			preferences.save();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return super.performOk();
	}
}
