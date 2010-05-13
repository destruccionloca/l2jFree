package elayne.application;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.actions.RequestSearch;
import elayne.actions.RequestServerAnnouncement;
import elayne.actions.RequestServerInstanceReload;
import elayne.actions.RequestServerPMToOnlineGms;
import elayne.actions.RequestServerRestart;
import elayne.actions.ShowBannedPlayersView;
import elayne.actions.ShowConsoleView;
import elayne.actions.ShowOnlinePlayersView;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{
	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction showBannedPlayersView;
	private Action showOnlinePlayersView;
	private IWorkbenchAction showConsole;
	private IWorkbenchAction showPreferences;
	private IWorkbenchAction showWelcomeWindow;
	private IContributionItem openWindows;
	private Action searchAction;
	private Action announceToAll;
	private Action sendGMChat;
	private Action requestServerRestart;
	private Action requestServerShutDown;
	private Action requestAbortServerRestart;
	private Action reloadMultisell;
	private Action reloadSkills;
	private Action reloadNpc;
	private Action reloadHtml;
	private Action reloadItems;
	private Action reloadInstanceManager;
	private Action reloadZones;
	private Action reloadTeleports;
	private Action reloadSpawns;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer)
	{
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window)
	{
		// EXIT ACTION
		exitAction = ActionFactory.QUIT.create(window);
		exitAction.setDescription("Exit the program.");
		exitAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.EXIT));
		register(exitAction);
		// ABOUT ACTION
		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setDescription("About Elayne");
		aboutAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.INFORMATION));
		register(aboutAction);
		// SHOW BANNED PLAYERS ACTION
		showBannedPlayersView = new ShowBannedPlayersView(window);
		register(showBannedPlayersView);
		// SHOW ONLINE PLAYERS ACTION
		showOnlinePlayersView = new ShowOnlinePlayersView(window);
		register(showOnlinePlayersView);
		// ANNOUNCE TO ALL ACTION
		announceToAll = new RequestServerAnnouncement(window);
		register(announceToAll);
		sendGMChat = new RequestServerPMToOnlineGms(window);
		register(sendGMChat);
		// SHOW CONSOLE
		showConsole = new ShowConsoleView(window);
		register(showConsole);
		// SHOW THE PROGRAM PREFERENCES
		showPreferences = ActionFactory.PREFERENCES.create(window);
		register(showPreferences);
		searchAction = new RequestSearch(window);
		register(searchAction);
		requestServerRestart = new RequestServerRestart(window, RequestServerRestart.SERVER_RESTART);
		register(requestServerRestart);
		requestServerShutDown = new RequestServerRestart(window, RequestServerRestart.SERVER_SHUT_DOWN);
		register(requestServerShutDown);
		requestAbortServerRestart = new RequestServerRestart(window, RequestServerRestart.ABORT);
		register(requestAbortServerRestart);
		reloadMultisell = new RequestServerInstanceReload(window, RequestServerInstanceReload.MULTISELL);
		register(reloadMultisell);
		reloadSkills = new RequestServerInstanceReload(window, RequestServerInstanceReload.SKILLS);
		register(reloadSkills);
		reloadNpc = new RequestServerInstanceReload(window, RequestServerInstanceReload.NPC);
		register(reloadNpc);
		reloadHtml = new RequestServerInstanceReload(window, RequestServerInstanceReload.HTML);
		register(reloadHtml);
		reloadItems = new RequestServerInstanceReload(window, RequestServerInstanceReload.ITEMS);
		register(reloadItems);
		reloadInstanceManager = new RequestServerInstanceReload(window, RequestServerInstanceReload.INSTANCE_MANAGER);
		register(reloadInstanceManager);
		reloadZones = new RequestServerInstanceReload(window, RequestServerInstanceReload.ZONES);
		register(reloadZones);
		reloadTeleports = new RequestServerInstanceReload(window, RequestServerInstanceReload.TELEPORTS);
		register(reloadTeleports);
		reloadSpawns = new RequestServerInstanceReload(window, RequestServerInstanceReload.SPAWNS);
		register(reloadSpawns);
		showWelcomeWindow = ActionFactory.INTRO.create(window);
		register(showWelcomeWindow);
		openWindows = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar)
	{
		// Main Menu
		MenuManager fileMenu = new MenuManager("&File", "file");
		fileMenu.add(exitAction);
		// fileMenu.add(addBannedPlayerAction);

		// View Menu
		MenuManager viewMenu = new MenuManager("&View", "view");
		viewMenu.add(showBannedPlayersView);
		viewMenu.add(showOnlinePlayersView);
		viewMenu.add(showConsole);
		viewMenu.add(openWindows);

		// Reload Menu
		MenuManager reloadMenu = new MenuManager("&Reload", "reload");
		reloadMenu.add(reloadMultisell);
		reloadMenu.add(reloadSkills);
		reloadMenu.add(reloadNpc);
		reloadMenu.add(reloadHtml);
		reloadMenu.add(reloadItems);
		reloadMenu.add(reloadInstanceManager);
		reloadMenu.add(reloadZones);
		reloadMenu.add(reloadTeleports);
		reloadMenu.add(reloadSpawns);

		// Server Menu
		MenuManager serverMenu = new MenuManager("&Server", "server");
		serverMenu.add(announceToAll);
		serverMenu.add(sendGMChat);
		serverMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		serverMenu.add(requestServerRestart);
		serverMenu.add(requestServerShutDown);
		serverMenu.add(requestAbortServerRestart);
		serverMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		serverMenu.add(reloadMenu);

		// Tools Menu
		MenuManager toolsMenu = new MenuManager("&Tools", "tools");
		toolsMenu.add(searchAction);
		serverMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolsMenu.add(showPreferences);

		// HELP Menu
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		helpMenu.add(aboutAction);
		helpMenu.add(showWelcomeWindow);

		// Add Menus to the menu bar.
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(serverMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar)
	{
		// Add Tools tool bar.
		IToolBarManager toolsToolbar = new ToolBarManager(coolBar.getStyle());
		coolBar.add(toolsToolbar);
		// toolsToolbar.add(dataBaseConnectionAction);

		// Add Views Tool Bar.
		IToolBarManager viewsToolbar = new ToolBarManager(coolBar.getStyle());
		coolBar.add(viewsToolbar);
		viewsToolbar.add(showBannedPlayersView);
		viewsToolbar.add(showOnlinePlayersView);
		viewsToolbar.add(showConsole);
		viewsToolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewsToolbar.add(searchAction);
		viewsToolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewsToolbar.add(announceToAll);
		viewsToolbar.add(sendGMChat);
		viewsToolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewsToolbar.add(requestServerRestart);
		viewsToolbar.add(requestServerShutDown);
		viewsToolbar.add(requestAbortServerRestart);

		super.fillCoolBar(coolBar);
	}
}
