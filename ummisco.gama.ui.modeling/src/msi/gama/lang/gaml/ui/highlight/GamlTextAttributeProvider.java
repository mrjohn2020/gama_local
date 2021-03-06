/**
 * Created by drogoul, 30 nov. 2020
 * 
 */
package msi.gama.lang.gaml.ui.highlight;

import static org.eclipse.xtext.ui.editor.utils.EditorUtils.colorFromRGB;
import static org.eclipse.xtext.ui.editor.utils.EditorUtils.fontFromFontData;

import java.util.ArrayList;

/**
 * The class GamlTextAttributeProvider.
 *
 * @author drogoul
 * @since 30 nov. 2020
 *
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ITextAttributeProvider;
import org.eclipse.xtext.ui.editor.syntaxcoloring.PreferenceStoreAccessor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;
import org.eclipse.xtext.util.Strings;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import msi.gama.application.workbench.ThemeHelper;
import msi.gama.common.preferences.GamaPreferences;
import msi.gama.common.preferences.GamaPreferences.Modeling;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaFont;
import msi.gaml.types.IType;
import ummisco.gama.ui.resources.GamaColors;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@Singleton
public class GamlTextAttributeProvider implements ITextAttributeProvider, IHighlightingConfigurationAcceptor {

	// private final PreferenceStoreAccessor preferencesAccessor;
	private final HashMap<String, TextAttribute> attributes;
	private final GamlHighlightingConfiguration highlightingConfig;
	private @Inject @Named (Constants.LANGUAGE_NAME) String languageName;

	// public static GamaFont getDefaultFont() {
	// final var fd = PreferenceConverter.getFontData(EditorsPlugin.getDefault().getPreferenceStore(),
	// JFaceResources.TEXT_FONT);
	// return new GamaFont(fd.getName(), fd.getStyle(), fd.getHeight());
	// }

	public static GamaFont getFont(final TextStyle ts) {
		final var fds = ts.getFontData();
		// if (fds == null)
		// return getDefaultFont();
		if (fds == null)
			return null;
		final var fd = fds[0];
		return new GamaFont(fd.getName(), fd.getStyle(), fd.getHeight());
	}

	@Inject
	public GamlTextAttributeProvider(IHighlightingConfiguration c, IPreferenceStoreAccess preferenceStoreAccess,
			PreferenceStoreAccessor prefStoreAccessor) {
		highlightingConfig = (GamlHighlightingConfiguration) c;
		this.attributes = new HashMap<>();
		// we first create the preferences
		configureHighlightingPreferences();
		// preferenceStoreAccess.getPreferenceStore().addPropertyChangeListener(this);
		initialize();
		ThemeHelper.addListener(isLight -> {
			highlightingConfig.changeTo(isLight);
			initialize();
		});
	}

	private void initialize() {

		// WorkbenchHelper.asyncRun(() -> {
		attributes.clear();
		highlightingConfig.configure(GamlTextAttributeProvider.this);
		// });

		// if (Display.getCurrent() == null) {
		//
		// WorkbenchHelper.asyncRun(() -> {
		// attributes.clear();
		// highlightingConfig.configure(GamlTextAttributeProvider.this);
		// });
		//
		// } else {
		// attributes.clear();
		// highlightingConfig.configure(this);
		// }
	}

	@Override
	public TextAttribute getAttribute(String id) {
		return attributes.get(id);
	}

	@Override
	public TextAttribute getMergedAttributes(String[] ids) {
		if (ids.length < 2)
			throw new IllegalStateException();
		final var mergedIds = getMergedIds(ids);
		var result = getAttribute(mergedIds);
		if (result == null) {
			for (final String id : ids) {
				result = merge(result, getAttribute(id));
			}
			if (result != null)
				attributes.put(mergedIds, result);
			else
				attributes.remove(mergedIds);
		}
		return result;
	}

	private TextAttribute merge(TextAttribute first, TextAttribute second) {
		if (first == null)
			return second;
		if (second == null)
			return first;
		final var style = first.getStyle() | second.getStyle();
		var fgColor = second.getForeground();
		if (fgColor == null)
			fgColor = first.getForeground();
		var bgColor = second.getBackground();
		if (bgColor == null)
			bgColor = first.getBackground();
		var font = second.getFont();
		if (font == null)
			font = first.getFont();
		return new TextAttribute(fgColor, bgColor, style, font);
	}

	public String getMergedIds(String[] ids) {
		return "$$$Merged:" + Strings.concat("/", Arrays.asList(ids)) + "$$$";
	}

	@Override
	public void acceptDefaultHighlighting(String id, String name, TextStyle style) {
		this.attributes.put(id, createTextAttribute(id, style));
	}

	protected TextAttribute createTextAttribute(String id, TextStyle textStyle) {
		return new TextAttribute(colorFromRGB(textStyle.getColor()), colorFromRGB(textStyle.getBackgroundColor()),
				textStyle.getStyle(), fontFromFontData(textStyle.getFontData()));
	}

	public void configureHighlightingPreferences() {
		final List<String> ids = new ArrayList<>();
		// First we create and/or read the preferences
		highlightingConfig.configure((id, name, style) -> {
			final var pref = GamaPreferences
					.create("pref_" + id + "_font", name + " font", (() -> getFont(style)), IType.FONT, false)
					.in(Modeling.NAME, "Syntax coloring").onChange(font -> {
						System.out.println("Pref " + "pref_" + id + "_font changed");
						applyFont(id, name, style, font);
					});
			applyFont(id, name, style, pref.getValue());

			final var pref2 =
					GamaPreferences
							.create("pref_" + id + "_color", "... and color",
									() -> GamaColors.toGamaColor(style.getColor()), IType.COLOR, false)
							.in(Modeling.NAME, "Syntax coloring").onChange(color -> {
								System.out.println("Pref " + "pref_" + id + "_color changed to " + color);
								applyColor(id, name, style, color);
							});
			applyColor(id, name, style, pref2.getValue());
			ids.add(pref.getKey());
			ids.add(pref2.getKey());
		});
		ThemeHelper.CORE_THEME_LIGHT.refreshes(ids.toArray(new String[0]));
	}

	private void applyFont(String id, String name, TextStyle style, GamaFont font) {
		if (font != null)
			style.setFontData(new FontData(font.getFontName(), font.getSize(), font.getStyle()));
		acceptDefaultHighlighting(id, name, style);
	}

	private void applyColor(String id, String name, TextStyle style, GamaColor color) {
		style.setColor(new RGB(color.red(), color.green(), color.blue()));
		acceptDefaultHighlighting(id, name, style);
	}

}
