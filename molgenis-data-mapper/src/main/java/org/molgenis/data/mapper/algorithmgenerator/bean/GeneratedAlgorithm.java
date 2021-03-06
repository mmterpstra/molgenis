package org.molgenis.data.mapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.util.Set;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GeneratedAlgorithm.class)
public abstract class GeneratedAlgorithm
{
	public abstract String getAlgorithm();

	@Nullable
	public abstract Set<AttributeMetaData> getSourceAttributes();

	@Nullable
	public abstract AlgorithmState getAlgorithmState();

	public static GeneratedAlgorithm create(String algorithm, Set<AttributeMetaData> sourceAttributes,
			AlgorithmState algorithmState)
	{
		return new AutoValue_GeneratedAlgorithm(algorithm, sourceAttributes, algorithmState);
	}
}
