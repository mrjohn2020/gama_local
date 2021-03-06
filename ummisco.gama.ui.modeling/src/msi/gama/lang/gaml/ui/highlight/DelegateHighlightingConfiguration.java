/**
 * Created by drogoul, 1 déc. 2020
 * 
 */
package msi.gama.lang.gaml.ui.highlight;

import static java.util.Map.entry;
import static msi.gama.common.preferences.GamaPreferences.create;
import static msi.gama.lang.gaml.ui.highlight.GamlTextAttributeProvider.getFont;
import static ummisco.gama.ui.resources.GamaColors.toGamaColor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ide.editor.syntaxcoloring.HighlightingStyles;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

import msi.gama.common.preferences.GamaPreferences;
import msi.gama.common.preferences.GamaPreferences.Modeling;
import msi.gama.common.preferences.Pref;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaFont;
import msi.gaml.types.IType;

/**
 * The class IGamlHighlightingConfiguration.
 *
 * @author drogoul
 * @since 1 déc. 2020
 *
 */
@SuppressWarnings ("unchecked")
public abstract class DelegateHighlightingConfiguration implements IHighlightingConfiguration {
	public static final String OPERATOR_ID = "binary";
	public static final String RESERVED_ID = "reserved";
	public static final String FACET_ID = "facet";
	public static final String FIELD_ID = "field";
	public static final String GLOBAL_ID = "global";
	public static final String VARIABLE_ID = "variable";
	public static final String VARDEF_ID = "varDef";
	public static final String TYPE_ID = "typeDef";
	public static final String ASSIGN_ID = "assignment";
	public static final String UNIT_ID = "unit";
	public static final String PRAGMA_ID = "pragma";
	public static final String KEYWORD_ID = HighlightingStyles.KEYWORD_ID;
	public static final String PUNCTUATION_ID = HighlightingStyles.PUNCTUATION_ID;
	public static final String COMMENT_ID = HighlightingStyles.COMMENT_ID;
	public static final String STRING_ID = HighlightingStyles.STRING_ID;
	public static final String NUMBER_ID = HighlightingStyles.NUMBER_ID;
	public static final String DEFAULT_ID = HighlightingStyles.DEFAULT_ID;
	public static final String INVALID_TOKEN_ID = HighlightingStyles.INVALID_TOKEN_ID;
	public static final String TASK_ID = HighlightingStyles.TASK_ID;

	String theme;
	final Map<String, Pref<?>> preferences = new HashMap<>();
	final static Map<String, String> TITLES = Map.ofEntries(entry(KEYWORD_ID, "Statement keyword"),
			entry(PUNCTUATION_ID, "Punctuation character"), entry(OPERATOR_ID, "Operator & action call"),
			entry(RESERVED_ID, "Reserved symbol"), entry(COMMENT_ID, "Comment"), entry(STRING_ID, "String"),
			entry(NUMBER_ID, "Literal constant"), entry(DEFAULT_ID, "Default"), entry(FACET_ID, "Facet key"),
			entry(VARIABLE_ID, "Variable"), entry(VARDEF_ID, "Variable definition"), entry(TYPE_ID, "Type"),
			entry(ASSIGN_ID, "Assignment sign"), entry(UNIT_ID, "Unit & constant"), entry(TASK_ID, "Task"),
			entry(PRAGMA_ID, "Pragma"), entry(FIELD_ID, "Field"));
	final Map<String, TextStyle> styles = Map.ofEntries(entry(KEYWORD_ID, keywordTextStyle()),
			entry(PUNCTUATION_ID, punctuationTextStyle()), entry(OPERATOR_ID, operatorTextStyle()),
			entry(RESERVED_ID, reservedTextStyle()), entry(COMMENT_ID, commentTextStyle()),
			entry(STRING_ID, stringTextStyle()), entry(NUMBER_ID, numberTextStyle()),
			entry(DEFAULT_ID, defaultTextStyle()), entry(FACET_ID, facetTextStyle()),
			entry(VARIABLE_ID, variableTextStyle()), entry(VARDEF_ID, varDefTextStyle()),
			entry(TYPE_ID, typeTextStyle()), entry(ASSIGN_ID, assignTextStyle()), entry(UNIT_ID, unitTextStyle()),
			entry(TASK_ID, taskTextStyle()), entry(PRAGMA_ID, pragmaTextStyle()), entry(FIELD_ID, fieldTextStyle()));

	TextStyle newStyle(int style, int red, int green, int blue) {
		final var textStyle = newStyle(style);
		textStyle.setColor(new RGB(red, green, blue));
		return textStyle;
	}

	TextStyle newStyle(int style) {
		final var textStyle = newStyle();
		textStyle.setStyle(style);
		return textStyle;
	}

	TextStyle newStyle() {
		return defaultTextStyle().copy();
	}

	public DelegateHighlightingConfiguration(String themeName) {
		theme = themeName;
		configurePreferences();
	}

	private void configure(final IHighlightingConfigurationAcceptor acceptor, String key) {
		acceptor.acceptDefaultHighlighting(key, TITLES.get(key), styles.get(key));
	}

	@Override
	public void configure(final IHighlightingConfigurationAcceptor acceptor) {
		TITLES.keySet().forEach(each -> configure(acceptor, each));
	}

	public void configurePreferences() {
		// we create and/or read the preferences
		configure((id, name, style) -> {
			var key = "pref_" + id + "_font";
			final var pref = create(key + "_" + theme, theme + " theme " + name + " font", (() -> getFont(style)),
					IType.FONT, false).hidden().in(Modeling.NAME, "Syntax coloring");
			preferences.put(key, pref);
			final var font = pref.getValue();
			if (font != null)
				style.setFontData(new FontData(font.getFontName(), font.getSize(), font.getStyle()));
			key = "pref_" + id + "_color";
			final var pref2 =
					create(key + "_" + theme, "... and color", () -> toGamaColor(style.getColor()), IType.COLOR, false)
							.hidden().in(Modeling.NAME, "Syntax coloring");
			preferences.put(key, pref2);
			final var color = pref2.getValue();
			if (color != null)
				style.setColor(new RGB(color.red(), color.green(), color.blue()));
		});

	}

	public void saveCurrentPreferences() {
		configure((id, name, style) -> {
			var key = "pref_" + id + "_font";
			saveFontPreference(key);
			key = "pref_" + id + "_color";
			saveColorPreference(key);
		});
	}

	private void saveFontPreference(String key) {
		System.out.println("Preference saved : " + key + "_" + theme);
		final var value = ((Pref<GamaFont>) GamaPreferences.get(key)).getValue();
		final var myPref = (Pref<GamaFont>) preferences.get(key);
		myPref.set(value);
		myPref.save();
	}

	private void saveColorPreference(String key) {
		System.out.println("Preference saved : " + key + "_" + theme);
		final var value = ((Pref<GamaColor>) GamaPreferences.get(key)).getValue();
		final var myPref = (Pref<GamaColor>) preferences.get(key);
		myPref.set(value);
		myPref.save();
	}

	public void restoreCurrentPreferences() {
		configure((id, name, style) -> {
			var key = "pref_" + id + "_font";
			restoreFontPreference(key);
			key = "pref_" + id + "_color";
			restoreColorPreference(key);
		});
	}

	private void restoreFontPreference(String key) {
		System.out.println("Preference restored : " + key + "_" + theme);
		final var value = ((Pref<GamaFont>) preferences.get(key)).getValue();
		final var myPref = (Pref<GamaFont>) GamaPreferences.get(key);
		myPref.set(value);
		myPref.save();
	}

	private void restoreColorPreference(String key) {
		System.out.println("Preference restored : " + key + "_" + theme);
		final var value = ((Pref<GamaColor>) preferences.get(key)).getValue();
		final var myPref = (Pref<GamaColor>) GamaPreferences.get(key);
		myPref.set(value);
		myPref.save();
	}

	abstract TextStyle facetTextStyle();

	abstract TextStyle pragmaTextStyle();

	abstract TextStyle typeTextStyle();

	abstract TextStyle assignTextStyle();

	abstract TextStyle variableTextStyle();

	abstract TextStyle fieldTextStyle();

	abstract TextStyle operatorTextStyle();

	abstract TextStyle reservedTextStyle();

	abstract TextStyle errorTextStyle();

	abstract TextStyle unitTextStyle();

	abstract TextStyle numberTextStyle();

	abstract TextStyle keywordTextStyle();

	abstract TextStyle punctuationTextStyle();

	abstract TextStyle stringTextStyle();

	abstract TextStyle varDefTextStyle();

	abstract TextStyle taskTextStyle();

	abstract TextStyle commentTextStyle();

	abstract TextStyle defaultTextStyle();

}
