/*********************************************************************************************
 *
 * 'WebHelper.java, in plugin ummisco.gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.utils;

import static msi.gama.application.workbench.ThemeHelper.isDark;
import static org.eclipse.core.runtime.FileLocator.toFileURL;
import static org.eclipse.core.runtime.Platform.getBundle;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import msi.gama.application.workbench.IWebHelper;
import msi.gama.common.interfaces.IGamaView.Html;
import msi.gama.common.preferences.GamaPreferences;

public class WebHelper implements IWebHelper {

	private static WebHelper instance = new WebHelper();

	public static WebHelper getInstance() {
		return instance;
	}

	private WebHelper() {}

	private static URL HOME_URL;

	public static URL getWelcomePageURL() {
		if (HOME_URL == null)
			try {
				final var welcomePage = "/welcome/" + (isDark() ? "dark" : "light") + "/welcome.html";
				HOME_URL = toFileURL(getBundle("ummisco.gama.ui.shared").getEntry(welcomePage));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		return HOME_URL;
	}

	public static void openWelcomePage(final boolean ifEmpty) {
		if (ifEmpty && WorkbenchHelper.getPage().getActiveEditor() != null) { return; }
		if (ifEmpty && !GamaPreferences.Interface.CORE_SHOW_PAGE.getValue()) { return; }
		// get the workspace
		final var workspace = ResourcesPlugin.getWorkspace();

		// create the path to the file
		final IPath location = new Path(getWelcomePageURL().getPath());

		// try to get the IFile (returns null if it could not be found in the
		// workspace)
		final var file = workspace.getRoot().getFileForLocation(location);
		IEditorInput input;
		if (file == null) {
			// not found in the workspace, get the IFileStore (external files)
			final var fileStore = EFS.getLocalFileSystem().getStore(location);
			input = new FileStoreEditorInput(fileStore);

		} else {
			input = new FileEditorInput(file);
		}

		try {
			WorkbenchHelper.getPage().openEditor(input, "msi.gama.application.browser");
		} catch (final PartInitException e) {
			e.printStackTrace();
		}
	}

	public static void showWeb2Editor(final URL url) {

		// get the workspace
		final var workspace = ResourcesPlugin.getWorkspace();

		// create the path to the file
		final IPath location = new Path(url.getPath());

		// try to get the IFile (returns null if it could not be found in the
		// workspace)
		final var file = workspace.getRoot().getFileForLocation(location);
		IEditorInput input;
		if (file == null) {
			// not found in the workspace, get the IFileStore (external files)
			final var fileStore = EFS.getLocalFileSystem().getStore(location);
			input = new FileStoreEditorInput(fileStore);

		} else {
			input = new FileEditorInput(file);
		}

		try {
			WorkbenchHelper.getPage().openEditor(input, "msi.gama.application.browser");
		} catch (final PartInitException e) {
			e.printStackTrace();
		}

	}

	public static void openPage(final String string) {
		try {
			final var view =
					(Html) WorkbenchHelper.getPage().openEditor(new NullEditorInput(), "msi.gama.application.browser");
			view.setUrl(string);
		} catch (final PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void showWelcome() {
		openWelcomePage(false);

	}

	@Override
	public void showPage(final String url) {
		openPage(url);
	}

	@Override
	public void showURL(final URL url) {
		showWeb2Editor(url);

	}

}
