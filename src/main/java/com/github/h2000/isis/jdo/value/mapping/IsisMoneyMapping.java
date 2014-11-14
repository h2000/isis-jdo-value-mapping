package com.github.h2000.isis.jdo.value.mapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.isis.applib.value.Money;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.mapping.MappingManager;
import org.datanucleus.store.rdbms.mapping.java.SingleFieldMultiMapping;
import org.datanucleus.store.rdbms.table.Column;
import org.datanucleus.store.rdbms.table.Table;

public class IsisMoneyMapping extends SingleFieldMultiMapping {

	public IsisMoneyMapping() {

	}

	@Override
	public Class<?> getJavaType() {

		return org.apache.isis.applib.value.Money.class;
	}

	@Override
	public void initialize(final AbstractMemberMetaData mmd, final Table container, final ClassLoaderResolver clr) {

		super.initialize(mmd, container, clr);
		addColumns();
	}

	@Override
	public void initialize(final RDBMSStoreManager storeMgr, final String type) {

		super.initialize(storeMgr, type);

		addColumns();
	}

	private void addColumns() {

		// amount
		addColumns(ClassNameConstants.JAVA_LANG_LONG);

		// currency
		addColumnWithLength(ClassNameConstants.JAVA_LANG_STRING, 3);
	}

	public void addColumnWithLength(final String typeName, final int columnLength) {

		final MappingManager mgr = getStoreManager().getMappingManager();
		Column column = null;
		if (table != null) {
			column = mgr.createColumn(this, typeName, getNumberOfDatastoreMappings());
			/* TODO metaData.setJdbcType("NCHAR") */
			column.setColumnMetaData(column.getColumnMetaData().setLength(columnLength));
		}
		mgr.createDatastoreMapping(this, column, typeName);
	}

	@Override
	public Object getValueForDatastoreMapping(final NucleusContext nucleusCtx, final int index, final Object value) {

		final Money m = ((Money) value);
		switch (index) {
			case 0:
				return m.longValue();
			case 1:
				return m.getCurrency();
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public void setObject(final ExecutionContext ec, final PreparedStatement preparedStmt, final int[] exprIndex,
			final Object value) {

		final Money m = ((Money) value);
		if (m == null) {
			getDatastoreMapping(0).setLong(preparedStmt, exprIndex[0], 0l);
			getDatastoreMapping(1).setString(preparedStmt, exprIndex[1], null);
		} else {
			getDatastoreMapping(0).setLong(preparedStmt, exprIndex[0], m.longValue());
			getDatastoreMapping(1).setString(preparedStmt, exprIndex[1], m.getCurrency());
		}
	}

	@Override
	public Object getObject(final ExecutionContext ec, final ResultSet resultSet, final int[] exprIndex) {

		try {
			// Check for null entries
			if (getDatastoreMapping(0).getObject(resultSet, exprIndex[0]) == null
					|| getDatastoreMapping(1).getObject(resultSet, exprIndex[1]) == null) {
				return null;
			}
		} catch (final Exception e) {
			// Do nothing
		}

		final long amount = getDatastoreMapping(0).getLong(resultSet, exprIndex[0]);
		final String currency = getDatastoreMapping(1).getString(resultSet, exprIndex[1]);
		if (currency == null) {
			return null;
		}
		return new Money(((Long) amount).doubleValue() / 100, currency);
	}

}