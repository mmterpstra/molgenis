package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.model.MolgenisModelException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class LongField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyType()
	{
		return "Long";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Long.parseLong(value) + "L";
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "BIGINT";
	}

	@Override
	public String getPostgreSqlType()
	{
		return "bigint"; // alias: int8
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "NUMBER (19,0)";
	}

	@Override
	public String getHsqlType()
	{
		return "LONG";
	}

	@Override
	public String getXsdType()
	{
		return "boolean";
	}

	@Override
	public String getFormatString()
	{
		return "%d";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "long";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Long;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return Long.class;
	}

	@Override
	public Long getTypedValue(String value) throws ParseException
	{
		return Long.parseLong(value);
	}

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.LONG;
	}

	@Override
	public Long getMaxLength()
	{
		return null;
	}

	@Override
	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS", "LESS", "GREATER");
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		if (value instanceof Number) return ((Number) value).longValue();
		if (value instanceof String) return Long.parseLong(value.toString());
		throw new RuntimeException("LongField.convert(" + value + ") failed");
	}

}
