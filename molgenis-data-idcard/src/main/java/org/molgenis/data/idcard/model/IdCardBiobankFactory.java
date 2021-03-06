package org.molgenis.data.idcard.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankFactory extends AbstractSystemEntityFactory<IdCardBiobank, IdCardBiobankMetaData, Integer>
{
	@Autowired
	IdCardBiobankFactory(IdCardBiobankMetaData idCardBiobankMetaData)
	{
		super(IdCardBiobank.class, idCardBiobankMetaData);
	}
}
