package org.molgenis.elasticsearch.index;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

public class EntityToSourceConverterTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void EntityToSourceConverter()
	{
		new EntityToSourceConverter(null);
	}

	@Test
	public void convert() throws ParseException
	{
		String entityName = "entity";
		String refEntityName = "refentity";

		String idAttributeName = "id";

		String refLabelAttributeName = "label";
		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData(refEntityName);
		refEntityMetaData.addAttribute(idAttributeName).setIdAttribute(true).setUnique(true);
		refEntityMetaData.addAttribute(refLabelAttributeName).setLabelAttribute(true).setUnique(true);

		String boolAttributeName = "xbool";
		String categoricalAttributeName = "xcategorical";
		String compoundAttributeName = "xcompound";
		String compoundPart0AttributeName = "xcompoundpart0";
		String compoundPart1AttributeName = "xcompoundpart1";
		String dateAttributeName = "xdate";
		String dateTimeAttributeName = "xdatetime";
		String decimalAttributeName = "xdecimal";
		String emailAttributeName = "xemail";
		String enumAttributeName = "xenum";
		String htmlAttributeName = "xhtml";
		String hyperlinkAttributeName = "xhyperlink";
		String intAttributeName = "xint";
		String longAttributeName = "xlong";
		String mrefAttributeName = "xmref";
		String scriptAttributeName = "xscript";
		String stringAttributeName = "xstring";
		String textAttributeName = "xtext";
		String xrefAttributeName = "xxref";

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute(idAttributeName).setIdAttribute(true).setUnique(true);
		entityMetaData.addAttribute(boolAttributeName).setDataType(MolgenisFieldTypes.BOOL);
		entityMetaData.addAttribute(categoricalAttributeName).setDataType(MolgenisFieldTypes.CATEGORICAL)
				.setRefEntity(refEntityMetaData);
		DefaultAttributeMetaData compoundPart0Attribute = new DefaultAttributeMetaData(compoundPart0AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		DefaultAttributeMetaData compoundPart1Attribute = new DefaultAttributeMetaData(compoundPart1AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		entityMetaData
				.addAttribute(compoundAttributeName)
				.setDataType(MolgenisFieldTypes.COMPOUND)
				.setAttributesMetaData(
						Arrays.<AttributeMetaData> asList(compoundPart0Attribute, compoundPart1Attribute));
		entityMetaData.addAttribute(dateAttributeName).setDataType(MolgenisFieldTypes.DATE);
		entityMetaData.addAttribute(dateTimeAttributeName).setDataType(MolgenisFieldTypes.DATETIME);
		entityMetaData.addAttribute(decimalAttributeName).setDataType(MolgenisFieldTypes.DECIMAL);
		entityMetaData.addAttribute(emailAttributeName).setDataType(MolgenisFieldTypes.EMAIL);
		entityMetaData.addAttribute(enumAttributeName).setDataType(MolgenisFieldTypes.ENUM);
		entityMetaData.addAttribute(htmlAttributeName).setDataType(MolgenisFieldTypes.HTML);
		entityMetaData.addAttribute(hyperlinkAttributeName).setDataType(MolgenisFieldTypes.HYPERLINK);
		entityMetaData.addAttribute(intAttributeName).setDataType(MolgenisFieldTypes.INT);
		entityMetaData.addAttribute(longAttributeName).setDataType(MolgenisFieldTypes.LONG);
		entityMetaData.addAttribute(mrefAttributeName).setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(refEntityMetaData);
		entityMetaData.addAttribute(scriptAttributeName).setDataType(MolgenisFieldTypes.SCRIPT);
		entityMetaData.addAttribute(stringAttributeName).setDataType(MolgenisFieldTypes.STRING);
		entityMetaData.addAttribute(textAttributeName).setDataType(MolgenisFieldTypes.TEXT);
		entityMetaData.addAttribute(xrefAttributeName).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(refEntityMetaData);

		MapEntity refEntity0 = new MapEntity(idAttributeName);
		String refIdValue0 = "refid0";
		String refIdValue1 = "refid1";
		String refLabelValue0 = "label0";
		String refLabelValue1 = "label1";
		refEntity0.set(idAttributeName, refIdValue0);
		refEntity0.set(refLabelAttributeName, refLabelValue0);
		MapEntity refEntity1 = new MapEntity(idAttributeName);
		refEntity1.set(idAttributeName, refIdValue1);
		refEntity1.set(refLabelAttributeName, refLabelValue1);

		String idValue = "entityid";
		Boolean boolValue = Boolean.TRUE;
		MapEntity categoricalValue = refEntity0;
		String compoundPart0Value = "compoundpart0";
		String compoundPart1Value = "compoundpart1";
		String dateValueStr = "2014-09-03";
		Date dateValue = MolgenisDateFormat.getDateFormat().parse(dateValueStr);
		String dateTimeValueStr = "2014-09-03T08:02:10+0200";
		Date dateTimeValue = MolgenisDateFormat.getDateTimeFormat().parse(dateTimeValueStr);
		Double decimalValue = Double.valueOf(1.23);
		String emailValue = "test@email.com";
		String enumValue = "enumval";
		String htmlValue = "<h1>html</h1>";
		String hyperlinkValue = "http://www.website.com/";
		Integer intValue = Integer.valueOf(1);
		Long longValue = Long.valueOf(12147483647l);
		List<MapEntity> mrefValue = Arrays.asList(refEntity0, refEntity1);
		String scriptValue = "a cool R script";
		String stringValue = "string";
		String textValue = "some interesting text";
		MapEntity xrefValue = refEntity1;

		MapEntity entity = new MapEntity(idAttributeName);
		entity.set(idAttributeName, idValue);
		entity.set(boolAttributeName, boolValue);
		entity.set(categoricalAttributeName, categoricalValue);
		entity.set(compoundPart0AttributeName, compoundPart0Value);
		entity.set(compoundPart1AttributeName, compoundPart1Value);
		entity.set(dateAttributeName, dateValue);
		entity.set(dateTimeAttributeName, dateTimeValue);
		entity.set(decimalAttributeName, decimalValue);
		entity.set(emailAttributeName, emailValue);
		entity.set(enumAttributeName, enumValue); // FIXME update
		entity.set(htmlAttributeName, htmlValue);
		entity.set(hyperlinkAttributeName, hyperlinkValue);
		entity.set(intAttributeName, intValue);
		entity.set(longAttributeName, longValue);
		entity.set(mrefAttributeName, mrefValue);
		entity.set(scriptAttributeName, scriptValue);
		entity.set(stringAttributeName, stringValue);
		entity.set(textAttributeName, textValue);
		entity.set(xrefAttributeName, refEntity1);

		Map<String, List<Object>> expectedMrefValue = new HashMap<String, List<Object>>();
		expectedMrefValue.put(idAttributeName, Arrays.<Object> asList(refIdValue0, refIdValue1));
		expectedMrefValue.put(refLabelAttributeName, Arrays.<Object> asList(refLabelValue0, refLabelValue1));
		DataService dataService = mock(DataService.class);
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(refEntityName)).thenReturn(refEntityMetaData);

		Map<String, Object> expectedSource = new HashMap<String, Object>();
		expectedSource.put(idAttributeName, idValue);
		expectedSource.put(boolAttributeName, boolValue);
		expectedSource.put(categoricalAttributeName, categoricalValue.get(refLabelAttributeName));
		expectedSource.put("key-" + categoricalAttributeName, categoricalValue.get(idAttributeName));
		expectedSource.put(dateAttributeName, dateValueStr);
		expectedSource.put(dateTimeAttributeName, dateTimeValueStr);
		expectedSource.put(decimalAttributeName, decimalValue);
		expectedSource.put(emailAttributeName, emailValue);
		expectedSource.put(enumAttributeName, enumValue);
		expectedSource.put(htmlAttributeName, htmlValue);
		expectedSource.put(hyperlinkAttributeName, hyperlinkValue);
		expectedSource.put(intAttributeName, intValue);
		expectedSource.put(longAttributeName, longValue);
		expectedSource.put(mrefAttributeName, Arrays.asList(expectedMrefValue));
		expectedSource.put("id-" + mrefAttributeName, expectedMrefValue.get(idAttributeName));
		expectedSource.put("key-" + mrefAttributeName, expectedMrefValue.get(idAttributeName));
		expectedSource.put(scriptAttributeName, scriptValue);
		expectedSource.put(stringAttributeName, stringValue);
		expectedSource.put(textAttributeName, textValue);
		expectedSource.put(xrefAttributeName, xrefValue.get(refLabelAttributeName));
		expectedSource.put("key-" + xrefAttributeName, xrefValue.get(idAttributeName));
		expectedSource.put(compoundPart0AttributeName, compoundPart0Value);
		expectedSource.put(compoundPart1AttributeName, compoundPart1Value);
		expectedSource.put("_xrefvalue", Collections.emptySet());

		Map<String, Object> source = new EntityToSourceConverter(dataService).convert(entity, entityMetaData);
		assertEquals(source, expectedSource);
	}
}