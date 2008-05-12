package elayne.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import elayne.application.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.P_STRING, "Default value");

		// Login Preferences
		store.setDefault(LoginPreferencePage.AUTO_LOGIN, false);
		store.setDefault(LoginPreferencePage.LOGIN_DB, "l2jdb");
		store.setDefault(LoginPreferencePage.LOGIN_DB_HOST, "127.0.0.1");
		store.setDefault(LoginPreferencePage.LOGIN_DB_USER, "root");
		store.setDefault(LoginPreferencePage.LOGIN_DB_PASS, "password");

		store.setDefault(LoginPreferencePage.SERVER_DB, "l2jdb");
		store.setDefault(LoginPreferencePage.SERVER_DB_HOST, "127.0.0.1");
		store.setDefault(LoginPreferencePage.SERVER_DB_USER, "root");
		store.setDefault(LoginPreferencePage.SERVER_DB_PASS, "password");
	}
}
