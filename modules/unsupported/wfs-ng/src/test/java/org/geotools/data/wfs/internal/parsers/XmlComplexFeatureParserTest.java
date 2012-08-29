/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2012, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.data.wfs.internal.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.geotools.feature.FakeTypes;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

public class XmlComplexFeatureParserTest {
	/**
	 * This method gets a file input stream for the file name specified. It
	 * looks for the file in /org/geotools/data/wfs/internal/parsers/test-data/
	 * and will Assert.fail() if it's not there.
	 * 
	 * @param resourceName
	 *            The name of the file whose stream you want. (Must be in
	 *            test-data folder).
	 * @return A FileInputStream of the file you requested.
	 */
	private FileInputStream getResourceAsFileInputStream(String resourceName) {
		final URL url = getClass().getResource(
				"/org/geotools/data/wfs/internal/parsers/test-data/"
						+ resourceName);

		try {
			return new FileInputStream(new File(url.getPath().replaceAll("%20",
					" ")));
		} catch (FileNotFoundException fnfe) {
			Assert.fail("Could not find the file '" + resourceName + "'.");
		}

		// I don't think this is actually reachable because the Assert.fail()
		// should cause the method to terminate but it has to be here to
		// compile.
		return null;
	}

	private XmlComplexFeatureParser getParser(String inputFileName) {
		try {
			return new XmlComplexFeatureParser(
					/* getFeatureResponseStream: */getResourceAsFileInputStream(inputFileName),
					/* targetType: */FakeTypes.Mine.MINETYPE_TYPE,
					/* featureDescriptorName: */FakeTypes.Mine.NAME_Mine);
		} catch (IOException ioe) {
			Assert.fail("Can't find " + inputFileName);
		}

		return null;
	}

	@Test
	public void getNumberOfFeatures_WFSResponseWithTwoFeatures_returns2()
			throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_two_mines.xml");

		// Act
		int numberOfFeatures = mineParser.getNumberOfFeatures();

		// Assert
		Assert.assertEquals(2, numberOfFeatures);
	}

	/**
	 * This test has a pretty brute-force assertion which makes it kind of
	 * brittle. Other tests are more fine-grained in their approach, checking
	 * that specific things have been set.
	 * 
	 * @throws IOException
	 */
	@Test
	public void parse_firstMine_returnsAdmiralHill() throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_two_mines.xml");

		// Act
		Feature feature = mineParser.parse();

		// Assert
		Assert.assertEquals(
				"FeatureImpl:MineType<MineType id=er.mine.S0000001>=[ComplexAttributeImpl:MineNamePropertyType=[ComplexAttributeImpl:MineName<MineNameType id=MINENAMETYPE_TYPE_1>=[ComplexAttributeImpl:MineNameType=[AttributeImpl:isPreferred<boolean id=isPreferred_1>=true, AttributeImpl:mineName<string id=mineName_1>=Pieces of Eight - Admiral Hill]]], ComplexAttributeImpl:MineNamePropertyType=[ComplexAttributeImpl:MineName<MineNameType id=MINENAMETYPE_TYPE_2>=[ComplexAttributeImpl:MineNameType=[AttributeImpl:isPreferred<boolean id=isPreferred_2>=false, AttributeImpl:mineName<string id=mineName_2>=Admiral Hill S - W Shear (WAMIN)]]]]",
				feature.toString());
	}

	/**
	 * This test has a pretty brute-force assertion which makes it kind of
	 * brittle. Other tests are more fine-grained in their approach, checking
	 * that specific things have been set.
	 * 
	 * @throws IOException
	 */
	@Test
	public void parse_secondMine_returnsAspacia() throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_two_mines.xml");
		mineParser.parse(); // This gets rid of the first result.

		// Act
		Feature feature = mineParser.parse();

		// Assert
		Assert.assertEquals(
				"FeatureImpl:MineType<MineType id=er.mine.S0000005>=[ComplexAttributeImpl:MineNamePropertyType=[ComplexAttributeImpl:MineName<MineNameType>=[ComplexAttributeImpl:MineNameType=[AttributeImpl:isPreferred<boolean>=true, AttributeImpl:mineName<string>=Aspacia]]]]",
				feature.toString());
	}

	@Test
	public void parse_xlinkAfterTarget_linkedElementGetsSet()
			throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_xlink_after_target.xml");

		// Act
		Feature feature = mineParser.parse();
		Object[] properties = feature.getProperties("MineNamePropertyType")
				.toArray();

		// Assert
		Assert.assertSame(properties[0], properties[1]);
	}

	@Test
	public void parse_xlinkBeforeTarget_linkedElementGetsSet()
			throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_xlink_before_target.xml");

		// Act
		Feature feature = mineParser.parse();
		Object[] properties = feature.getProperties("MineNamePropertyType")
				.toArray();

		// Assert
		Assert.assertEquals(properties[0], properties[1]);
	}

	@Test
	public void parse_twoXlinksBeforeTarget_linkedElementsGetsSet()
			throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_two_xlink_before_target.xml");

		// Act
		Feature feature = mineParser.parse();
		Object[] properties = feature.getProperties("MineNamePropertyType")
				.toArray();

		// Assert
		Assert.assertEquals(properties[0], properties[1]);
		Assert.assertEquals(properties[1], properties[2]);
	}

	@Test
	public void parse_xlinkRefersToTargetInAnotherFeatureAbove_linkedElementGetsSet()
			throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_xlink_target_in_another_feature_above.xml");

		// Act
		Property mineNamePropertyType1 = mineParser.parse().getProperty(
				"MineNamePropertyType");
		Property mineNamePropertyType2 = mineParser.parse().getProperty(
				"MineNamePropertyType");

		// Assert
		Assert.assertSame(mineNamePropertyType1, mineNamePropertyType2);
	}

	@Test
	public void parse_xlinkRefersToTargetInAnotherFeatureBelow_linkedElementGetsSet()
			throws IOException {
		// Arrange
		XmlComplexFeatureParser mineParser = getParser("wfs_response_xlink_target_in_another_feature_below.xml");

		// Act
		Property mineNamePropertyType1 = mineParser.parse().getProperty(
				"MineNamePropertyType");
		Property mineNamePropertyType2 = mineParser.parse().getProperty(
				"MineNamePropertyType");

		// Assert
		Assert.assertEquals(mineNamePropertyType1, mineNamePropertyType2);
	}
}