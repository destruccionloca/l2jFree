package elayne.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import elayne.views.BannedPlayersView;
import elayne.views.ClanInfoView;
import elayne.views.OnlinePlayersView;
import elayne.views.PlayerInfoView;
import elayne.views.SearchView;

public class Perspective implements IPerspectiveFactory
{
	public IPageLayout pLayout;
	/** The standard perspective used in the application. */
	public static final String PERSPECTIVE_ID = "Elayne.perspective";
	/** Left folder's id. */
	public static final String FI_LEFT = "Elayne.perspective.leftFolder";
	/** Top folder's id. */
	public static final String FI_TOP = "Elayne.perspective.topFolder";
	/** Top folder's id. */
	public static final String FI_BOTTOM = "Elayne.perspective.bottomFolder";

	public void createInitialLayout(IPageLayout layout)
	{
		String editorArea = layout.getEditorArea();

		IFolderLayout leftFolder = layout.createFolder(FI_LEFT, IPageLayout.LEFT, 0.25f, editorArea);
		leftFolder.addView(OnlinePlayersView.ID);
		IViewLayout onlinePlayersLayout = layout.getViewLayout(OnlinePlayersView.ID);
		onlinePlayersLayout.setCloseable(false);
		onlinePlayersLayout.setMoveable(false);
		leftFolder.addPlaceholder(OnlinePlayersView.ID + ":*");

		IFolderLayout topFolder = layout.createFolder(FI_TOP, IPageLayout.TOP, 0.70f, editorArea);
		topFolder.addView(PlayerInfoView.ID);
		IViewLayout playersLayout = layout.getViewLayout(PlayerInfoView.ID);
		playersLayout.setCloseable(false);

		topFolder.addPlaceholder(SearchView.ID);

		topFolder.addView(ClanInfoView.ID);
		IViewLayout clanLayout = layout.getViewLayout(ClanInfoView.ID);
		clanLayout.setCloseable(false);

		IFolderLayout bottomFolder = layout.createFolder(FI_BOTTOM, IPageLayout.BOTTOM, 0.10f, editorArea);
		bottomFolder.addView("org.eclipse.ui.console.ConsoleView");
		bottomFolder.addView(BannedPlayersView.ID);

		pLayout = layout;
		layout.setEditorAreaVisible(false);
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		layout.addPerspectiveShortcut(PERSPECTIVE_ID);
	}

	public IPageLayout getPLayout()
	{
		return pLayout;
	}
}
