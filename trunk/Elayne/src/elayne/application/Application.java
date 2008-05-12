package elayne.application;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import elayne.Session;
import elayne.dialogs.LoginDialog;
import elayne.model.ConnectionDetails;
import elayne.preferences.LoginPreferencePage;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception
	{
		Display display = PlatformUI.createDisplay();
		try
		{
			final Session session = Session.getInstance();
			if (!login(session))
				return IApplication.EXIT_OK;

			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			return IApplication.EXIT_OK;
		}
		finally
		{
			display.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop()
	{
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable()
		{
			public void run()
			{
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	private boolean login(final Session session)
	{
		boolean firstTry = true;
		LoginDialog loginDialog = new LoginDialog(null);
		while (!session.getIsAllowedUser())
		{
			IPreferencesService service = Platform.getPreferencesService();
			boolean auto_login = service.getBoolean(Activator.PLUGIN_ID, LoginPreferencePage.AUTO_LOGIN, false, null);
			ConnectionDetails details = loginDialog.getConnectionDetails();
			if (!auto_login || details == null || !firstTry)
			{
				if (loginDialog.open() != Window.OK)
					return false;
				details = loginDialog.getConnectionDetails();
			}
			firstTry = false;
			session.setConnectionDetails(details);
			connectWithProgress(session);

			// ERASE THE SPLASH SCREEN.
			Platform.endSplash();
		}
		return true;
	}

	private void connectWithProgress(final Session session)
	{
		ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
		progress.setCancelable(true);
		try
		{
			progress.run(true, true, new IRunnableWithProgress()
			{
				public void run(IProgressMonitor monitor) throws InvocationTargetException
				{
					try
					{
						session.connectAndLogin(monitor);
					}
					catch (Exception e)
					{
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// do nothing
		}
	}
}
