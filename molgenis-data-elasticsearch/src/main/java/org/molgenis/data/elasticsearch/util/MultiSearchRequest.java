package org.molgenis.data.elasticsearch.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import java.util.ArrayList;
import java.util.List;

public class MultiSearchRequest
{
	private List<String> documentTypes;
	private QueryImpl<Entity> query;
	private List<String> fieldsToReturn = new ArrayList<>();

	public MultiSearchRequest()
	{
	}

	public MultiSearchRequest(List<String> documentTypes, Query<Entity> query, List<String> fieldsToReturn)
	{
		this.documentTypes = documentTypes;
		this.query = new QueryImpl<>(query);
		this.fieldsToReturn = fieldsToReturn;
	}

	public List<String> getDocumentType()
	{
		return documentTypes;
	}

	public Query<Entity> getQuery()
	{
		return query;
	}

	public List<String> getFieldsToReturn()
	{
		return fieldsToReturn;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}