/*******************************************************************************************************
 *
 * msi.gama.util.file.GamaShapeFile.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.util.file;

import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import msi.gama.common.geometry.Envelope3D;
import msi.gama.common.geometry.GeometryUtils;
import msi.gama.common.util.GISUtils;
import msi.gama.metamodel.shape.GamaGisGeometry;
import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.topology.projection.ProjectionFactory;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.operators.Strings;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ummisco.gama.dev.utils.DEBUG;

/**
 * Written by drogoul Modified on 13 nov. 2011
 *
 * @todo Description
 *
 */
@file (
		name = "shape",
		extensions = { "shp" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.SHAPEFILE, IConcept.FILE },
		doc = @doc ("Represents a shape file as defined by the ESRI standard. See https://en.wikipedia.org/wiki/Shapefile for more information."))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaShapeFile extends GamaGisFile {

	public static class ShapeInfo extends GamaFileMetaData {

		final int itemNumber;
		final CoordinateReferenceSystem crs;
		final double width;
		final double height;
		final Map<String, String> attributes = new LinkedHashMap();

		public ShapeInfo(final IScope scope, final URL url, final long modificationStamp) {
			super(modificationStamp);
			ShapefileDataStore store = null;
			ReferencedEnvelope env = new ReferencedEnvelope();
			CoordinateReferenceSystem crs1 = null;
			int number = 0;
			try {
				store = getDataStore(url);
				final SimpleFeatureSource source = store.getFeatureSource();
				final SimpleFeatureCollection features = source.getFeatures();
				try {
					crs1 = source.getInfo().getCRS();

				} catch (final Exception e) {
					DEBUG.ERR("Ignored exception in ShapeInfo getCRS:" + e.getMessage());
				}
				env = source.getBounds();
				if (crs1 == null) {
					crs1 = GISUtils.manageGoogleCRS(url);
					if (crs1 != null) {
						env = new ReferencedEnvelope(env, crs1);
					}
				}

				if (crs1 != null) {
					try {
						env = env.transform(new ProjectionFactory().getTargetCRS(scope), true);
					} catch (final Exception e) {
						throw e;
					}
				}
				try {
					number = features.size();
				} catch (final Exception e) {
					DEBUG.ERR("Error in loading shapefile: " + e.getMessage());
				}
				final java.util.List<AttributeDescriptor> att_list = store.getSchema().getAttributeDescriptors();
				for (final AttributeDescriptor desc : att_list) {
					String type;
					if (desc.getType() instanceof GeometryType) {
						type = "geometry";
					} else {
						type = Types.get(desc.getType().getBinding()).toString();
					}
					attributes.put(desc.getName().getLocalPart(), type);
				}
			} catch (final Exception e) {
				DEBUG.ERR("Error in reading metadata of " + url);
				e.printStackTrace();

			} finally {
				width = env.getWidth();
				height = env.getHeight();
				itemNumber = number;
				this.crs = crs1;
				if (store != null) {
					store.dispose();
				}
			}

		}

		public CoordinateReferenceSystem getCRS() {
			return crs;
		}

		public ShapeInfo(final String propertiesString) {
			super(propertiesString);
			final String[] segments = split(propertiesString);
			itemNumber = Integer.parseInt(segments[1]);
			final String crsString = segments[2];
			CoordinateReferenceSystem theCRS;
			if ("null".equals(crsString) || crsString.startsWith("Unknown")) {
				theCRS = null;
			} else {
				try {
					theCRS = CRS.parseWKT(crsString);
				} catch (final Exception e) {
					theCRS = null;
				}
			}
			crs = theCRS;
			width = Double.parseDouble(segments[3]);
			height = Double.parseDouble(segments[4]);
			if (segments.length > 5) {
				final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], SUB_DELIMITER);
				final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], SUB_DELIMITER);
				for (int i = 0; i < names.length; i++) {
					attributes.put(names[i], types[i]);
				}
			}
		}

		/**
		 * Method getSuffix()
		 *
		 * @see msi.gama.util.file.GamaFileMetaInformation#getSuffix()
		 */
		@Override
		public String getSuffix() {
			final StringBuilder sb = new StringBuilder();
			appendSuffix(sb);
			return sb.toString();
		}

		@Override
		public void appendSuffix(final StringBuilder sb) {
			sb.append(itemNumber).append(" object");
			if (itemNumber > 1) {
				sb.append("s");
			}
			sb.append(SUFFIX_DEL);
			sb.append(crs == null ? "Unknown CRS" : crs.getName().getCode());
			sb.append(SUFFIX_DEL);
			sb.append(Math.round(width)).append("m x ");
			sb.append(Math.round(height)).append("m");
		}

		@Override
		public String getDocumentation() {
			final StringBuilder sb = new StringBuilder();
			sb.append("Shapefile").append(Strings.LN);
			sb.append(itemNumber).append(" objects").append(Strings.LN);
			sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m").append(Strings.LN);
			sb.append("Coordinate Reference System: ").append(crs == null ? "Unknown CRS" : crs.getName().getCode())
					.append(Strings.LN);
			if (!attributes.isEmpty()) {
				sb.append("Attributes: ").append(Strings.LN);
				attributes.forEach((k, v) -> sb.append("<li>").append(k).append(" (" + v + ")").append("</li>"));
			}
			return sb.toString();
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		@Override
		public String toPropertyString() {
			// See Issue #1603: .toWKT() && pa can sometimes cause problem with
			// certain projections.
			String system = crs == null ? "Unknown projection" : crs.toWKT();
			try {
				CRS.parseWKT(system);
			} catch (final Exception e) {
				// The toWKT()/parseWKT() pair has a problem
				String srs = CRS.toSRS(crs);
				if (srs == null && crs != null) {
					srs = crs.getName().getCode();
				}
				system = "Unknown projection " + srs;

			}
			final String attributeNames = join(attributes.keySet(), SUB_DELIMITER);
			final String types = join(attributes.values(), SUB_DELIMITER);
			final Object[] toSave =
					new Object[] { super.toPropertyString(), itemNumber, system, width, height, attributeNames, types };
			return join(toSave, DELIMITER);
		}
	}

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 * @param pathName
	 */
	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\");",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
	}

	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code, as an int (epsg code)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"32648\");",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final Integer code) throws GamaRuntimeException {
		super(scope, pathName, code);
	}

	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code (epg,...,), as a string",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"EPSG:32648\");",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final String code) throws GamaRuntimeException {
		super(scope, pathName, code);
	}

	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", true);",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final boolean with3D) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null, with3D);
	}

	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code, as an int (epsg code) and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"32648\", true);",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final Integer code, final boolean with3D)
			throws GamaRuntimeException {
		super(scope, pathName, code, with3D);
	}

	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code (epg,...,), as a string and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"EPSG:32648\",true);",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final String code, final boolean with3D)
			throws GamaRuntimeException {
		super(scope, pathName, code, with3D);
	}

	/**
	 * @see msi.gama.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) { return; }
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		readShapes(scope);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		ShapeInfo s;
		final IFileMetaDataProvider p = scope.getGui().getMetaDataProvider();
		if (p != null) {
			s = (ShapeInfo) p.getMetaData(getFile(scope), false, true);
		} else {
			try {
				s = new ShapeInfo(scope, getFile(scope).toURI().toURL(), 0);
			} catch (final MalformedURLException e) {
				return GamaListFactory.EMPTY_LIST;
			}
		}
		return GamaListFactory.wrap(Types.STRING, s.attributes.keySet());
	}

	@Override
	protected CoordinateReferenceSystem getOwnCRS(final IScope scope) {
		ShapefileDataStore store = null;
		try {
			final URL url = getFile(scope).toURI().toURL();
			store = getDataStore(url);
			CoordinateReferenceSystem crs = store.getFeatureSource().getInfo().getCRS();
			if (crs == null) {
				crs = GISUtils.manageGoogleCRS(url);
			}
			return crs;
		} catch (final IOException e) {
			return null;
		} finally {
			if (store != null) {
				store.dispose();
			}
		}
	}

	static ShapefileDataStore getDataStore(final URL url) {
		final ShapefileDataStore store = new ShapefileDataStore(url);
		store.setGeometryFactory(GeometryUtils.GEOMETRY_FACTORY);
		store.setBufferCachingEnabled(true);
		store.setMemoryMapped(true);
		store.setCharset(Charset.forName("UTF8"));
		return store;
	}

	protected void readShapes(final IScope scope) {
		scope.getGui().getStatus(scope).beginSubStatus("Reading file " + getName(scope));
		ShapefileDataStore store = null;
		final File file = getFile(scope);
		final IList list = getBuffer();
		int size = 0;
		try {
			store = getDataStore(file.toURI().toURL());
			final ContentFeatureSource source = store.getFeatureSource();
			final Envelope3D env = Envelope3D.of(source.getBounds());
			size = source.getCount(Query.ALL);
			int index = 0;
			computeProjection(scope, env);
			try (FeatureReader reader = store.getFeatureReader()) {
				while (reader.hasNext()) {
					index++;
					if (index % 20 == 0) {
						scope.getGui().getStatus(scope).setSubStatusCompletion(index / (double) size);
					}
					final Feature feature = reader.next();
					Geometry g = (Geometry) feature.getDefaultGeometryProperty().getValue();
					if (g != null && !g.isEmpty() /* Fix for Issue 725 && 677 */ ) {
						if (!with3D && !g.isValid()) {
							g = GeometryUtils.cleanGeometry(g);
						}
						g = gis.transform(g);
						if (!with3D) {
							g.apply(ZERO_Z);
							g.geometryChanged();
						}
						g = multiPolygonManagement(g);
						GamaGisGeometry gt = new GamaGisGeometry(g, feature);
						if (gt.getInnerGeometry() != null)
							list.add(gt);
						
					} else if (g == null) {
						// See Issue 725
						GAMA.reportError(scope,
								GamaRuntimeException
										.warning("GamaShapeFile.fillBuffer; geometry could not be added  as it is "
												+ "nil: " + feature.getIdentifier(), scope),
								false);
					}
				}
			}
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			if (store != null) {
				store.dispose();
			}
			scope.getGui().getStatus(scope).endSubStatus("Reading file " + getName(scope));
		}
		if (size > list.size()) {
			GAMA.reportError(scope, GamaRuntimeException.warning("Problem with file " + getFile(scope) + ": only "
					+ list.size() + " of the " + size + " geometries could be added", scope), false);
		}
	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		if (gis == null) {
			ShapefileDataStore store = null;
			try {
				store = getDataStore(getFile(scope).toURI().toURL());
				final Envelope3D env = Envelope3D.of(store.getFeatureSource().getBounds());
				computeProjection(scope, env);
			} catch (final IOException e) {
				return Envelope3D.EMPTY;
			} finally {
				if (store != null) {
					store.dispose();
				}
			}
		}
		return gis.getProjectedEnvelope();

	}

}
