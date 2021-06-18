/*********************************************************************************************
 *
 * 'NavigatorLabelProvider.java, in plugin ummisco.gama.ui.navigator, is part of the source code of the GAMA modeling
 * and simulation platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.navigator;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import ummisco.gama.ui.navigator.contents.VirtualContent;
import ummisco.gama.ui.resources.GamaFonts;

public class NavigatorLabelProvider extends CellLabelProvider implements ILabelProvider, IColorProvider, IFontProvider {

	@Override
	public String getText(final Object element) {
		if (element instanceof VirtualContent) { return ((VirtualContent<?>) element).getName(); }
		return null;
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof VirtualContent) { return ((VirtualContent<?>) element).getImage(); }
		return null;
	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	@Override
	public Font getFont(final Object element) {
		if (element instanceof VirtualContent) { return ((VirtualContent<?>) element).getFont(); }
		return GamaFonts.getNavigFolderFont();
	}

	@Override
	public Color getForeground(final Object element) {
		if (element instanceof VirtualContent) { return ((VirtualContent<?>) element).getColor(); }
		return null;
	}

	@Override
	public Color getBackground(final Object element) {
		return null;
	}

	@Override
	public void update(final ViewerCell cell) {}

}
