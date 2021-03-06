package org.molgenis.data.annotation.test.core.entity.impl.gavin;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.core.entity.impl.ExacAnnotator;
import org.molgenis.data.annotation.core.entity.impl.gavin.GavinAnnotator;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.GavinAnnotatorSettings;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfWriterUtils;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.annotation.core.entity.impl.gavin.GavinAnnotator.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { GavinAnnotatorTest.Config.class, GavinAnnotator.class })
public class GavinAnnotatorTest extends AbstractMolgenisSpringTest
{
	private static final String BENIGN = "Benign";
	private static final String CALIBRATED = "calibrated";

	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	RepositoryAnnotator annotator;

	@Autowired
	EffectsMetaData effectsMetaData;

	private EntityMetaData emd;
	private EntityMetaData entityMetaData;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
		emd = entityMetaDataFactory.create().setName("gavin");
		entityMetaData = entityMetaDataFactory.create().setName("test_variant");
		List<AttributeMetaData> refAttributesList = Arrays
				.asList(CaddAnnotator.getCaddScaledAttr(attributeMetaDataFactory),
						ExacAnnotator.getExacAFAttr(attributeMetaDataFactory), vcfAttributes.getAltAttribute());
		entityMetaData.addAttributes(refAttributesList);
		AttributeMetaData refAttr = attributeMetaDataFactory.create().setName("test_variant").setDataType(XREF)
				.setRefEntity(entityMetaData).setDescription(
						"This annotator needs a references to an entity containing: " + StreamSupport
								.stream(refAttributesList.spliterator(), false).map(AttributeMetaData::getName)
								.collect(Collectors.joining(", ")));

		emd.addAttributes(
				Arrays.asList(effectsMetaData.getGeneNameAttr(), effectsMetaData.getPutativeImpactAttr(), refAttr,
						vcfAttributes.getAltAttribute()));

		AttributeMetaData idAttr = attributeMetaDataFactory.create().setName("idAttribute").setAuto(true);
		emd.addAttribute(idAttr);
		emd.setIdAttribute(idAttr);
		emd.addAttributes(effectsMetaData.getOrderedAttributes());
		emd.addAttribute(
				attributeMetaDataFactory.create().setName(EffectsMetaData.VARIANT).setNillable(false).setDataType(XREF)
						.setRefEntity(entityMetaData));
		AttributeMetaData classification = attributeMetaDataFactory.create().setName(CLASSIFICATION).setDataType(STRING)
				.setDescription(CLASSIFICATION).setLabel(CLASSIFICATION);
		AttributeMetaData confidence = attributeMetaDataFactory.create().setName(CONFIDENCE).setDataType(STRING)
				.setDescription(CONFIDENCE).setLabel(CONFIDENCE);
		AttributeMetaData reason = attributeMetaDataFactory.create().setName(REASON).setDataType(STRING)
				.setDescription(REASON).setLabel(REASON);
		emd.addAttributes(Arrays.asList(classification, confidence, reason));

		entityMetaData.addAttribute(idAttr);
		entityMetaData.setIdAttribute(idAttr);

	}

	@Test
	public void testGeneWithNullFields()
	{
		Entity variantEntity = getVariantEntity("A,T", "6,80", "0.00001,0.00001");
		Entity effectEntity = getEffectEntity("T", "HIGH", "ABCD1", variantEntity);

		Iterator<Entity> results = annotator.annotate(singletonList(effectEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), BENIGN);
		assertEquals(resultEntity.get(CONFIDENCE), CALIBRATED);
		assertEquals(resultEntity.get(REASON), "Variant MAF of 1.0E-5 is greater than 0.0.");
	}

	@Test
	public void testAnnotateHighMafBenign()
	{
		Entity variant_entity = getVariantEntity("A,T", "1,2", "2,3");
		Entity effect_entity = getEffectEntity("A", "HIGH", null, variant_entity);

		Iterator<Entity> results = annotator.annotate(singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Benign");
		assertEquals(resultEntity.get(CONFIDENCE), "genomewide");
		assertEquals(resultEntity.get(REASON),
				"Variant MAF of 2.0 is not rare enough to generally be considered pathogenic.");
	}

	@Test
	public void testAnnotateLowCaddBenign()
	{
		Entity variant_entity = getVariantEntity("A,T", "1,2", "0.00001,0.00001");
		Entity effect_entity = getEffectEntity("A", "HIGH", null, variant_entity);

		Iterator<Entity> results = annotator.annotate(singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Benign");
		assertEquals(resultEntity.get(CONFIDENCE), "genomewide");
		assertEquals(resultEntity.get(REASON),
				"Variant CADD score of 1.0 is less than a global threshold of 15, although the variant MAF of 1.0E-5 is rare enough to be potentially pathogenic.");
	}

	@Test
	public void testAnnotateNoCaddVOUS()
	{
		Entity variant_entity = getVariantEntity("A,T", null, "0.00001,0.00001");
		Entity effect_entity = getEffectEntity("A", "HIGH", "TFR2", variant_entity);

		Iterator<Entity> results = annotator.annotate(singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "VOUS");
		assertEquals(resultEntity.get(CONFIDENCE), "genomewide");
		assertEquals(resultEntity.get(REASON),
				"Unable to classify variant as benign or pathogenic. The combination of HIGH impact, a CADD score of null and MAF of 1.0E-5 in TFR2 is inconclusive.");

	}

	@Test
	public void testAnnotateLowVariantCaddBenign()
	{
		Entity variant_entity = getVariantEntity("A,T", "6,80", "0.00001,0.00001");
		Entity effect_entity = getEffectEntity("A", "HIGH", "TFR2", variant_entity);

		Iterator<Entity> results = annotator.annotate(singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Benign");
		assertEquals(resultEntity.get(CONFIDENCE), "calibrated");
		assertEquals(resultEntity.get(REASON),
				"Variant CADD score of 6.0 is less than 13.329999999999998 for this gene.");
	}

	@Test
	public void testAnnotateHighCaddPathogenic()
	{
		Entity variant_entity = getVariantEntity("A,T", "6,80", "0.00001,0.00001");
		Entity effect_entity = getEffectEntity("T", "HIGH", "TFR2", variant_entity);

		Iterator<Entity> results = annotator.annotate(singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Pathogenic");
		assertEquals(resultEntity.get(CONFIDENCE), "calibrated");
		assertEquals(resultEntity.get(REASON), "Variant CADD score of 80.0 is greater than 30.35 for this gene.");
	}

	private Entity getEffectEntity(String alt, String putativeImpact, String geneName, Entity variantEntity)
	{
		Entity effectEntity = new DynamicEntity(emd);
		effectEntity.set(EffectsMetaData.ALT, alt);
		effectEntity.set(EffectsMetaData.PUTATIVE_IMPACT, putativeImpact);
		effectEntity.set(EffectsMetaData.GENE_NAME, geneName);
		effectEntity.set(VcfWriterUtils.VARIANT, variantEntity);
		return effectEntity;
	}

	private Entity getVariantEntity(String alt, String caddScaled, String exacAF)
	{
		Entity variantEntity = new DynamicEntity(entityMetaData);
		variantEntity.set(VcfAttributes.ALT, alt);
		variantEntity.set(CaddAnnotator.CADD_SCALED, caddScaled);
		variantEntity.set(ExacAnnotator.EXAC_AF, exacAF);
		return variantEntity;
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model", "org.molgenis.data.annotation.core.effects" })
	public static class Config
	{
		@Bean
		public GavinAnnotatorSettings gavinAnnotatorSettings()
		{
			GavinAnnotatorSettings settings = mock(GavinAnnotatorSettings.class);
			when(settings.getString(GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION))
					.thenReturn(ResourceUtils.getFile(getClass(), "/gavin/GAVIN_calibrations_r0.1.xlsx").getPath());

			return settings;
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public AnnotationService annotationService()
		{
			return mock(AnnotationService.class);
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}

		@Bean
		public Entity caddAnnotatorSettings()
		{
			return mock(Entity.class);
		}

		@Bean
		public Entity exacAnnotatorSettings()
		{
			return mock(Entity.class);
		}

		@Bean
		public GeneNameQueryCreator geneNameQueryCreator()
		{
			return new GeneNameQueryCreator();
		}

		@Bean
		public EntityListenersService entityListenersService()
		{
			return new EntityListenersService();
		}
	}
}
