/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.security.dao.SnaDAO;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.NotNull;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class SnaEntity {
		private static final Logger LOG = LoggerFactory.getLogger(SnaEntity.class);

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	public class Key implements Nameable {
		private Field field;
		private String localColumn;
		private String foreignColumn;

		private boolean foreignKey;
		private boolean nullable;

		/**
		 * Constructor
		 * 
		 * @param field
		 */
		public Key(Field field) {
			Table table = SnaEntity.this.getClass().getAnnotation(Table.class);
			ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
			Column column = field.getAnnotation(Column.class);

			this.nullable = field.getAnnotation(NotNull.class) != null;
			this.foreignKey = foreignKey != null;

			this.localColumn = new StringBuilder().append(table.value()).append(SnaDAO.DOT).append(column.value())
					.toString();

			if (this.foreignKey) {
				this.foreignColumn = new StringBuilder().append(foreignKey.table()).append(SnaDAO.DOT)
						.append(foreignKey.refer()).toString();
			}
			this.field = field;
		}

		/**
		 * @return
		 */
		public long value() {
			Object object = SnaEntity.this.getFieldValue(this.field);
			if (object == null) {
				return -1;
			}
			return (Long) object;
		}

		/**
		 * @return
		 */
		public boolean isNullable() {
			return this.nullable;
		}

		/**
		 * @inheritDoc
		 *
		 * @see Nameable#getName()
		 */
		@Override
		public String getName() {
			if (foreignKey) {
				return this.foreignColumn;
			}
			return this.localColumn;
		}
	}

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	/**
	 * the {@link Column}s annotation of this SnaEntity type
	 */
	private final static Map<Class<? extends SnaEntity>, Map<Field, Column>> FIELDS_MAP = new HashMap<Class<? extends SnaEntity>, Map<Field, Column>>();

	/**
	 * @param snaEntityType
	 * @return
	 */
	public static <E extends SnaEntity> Map<Field, Column> getFields(Class<E> entityType) {
		Map<Field, Column> fields = SnaEntity.FIELDS_MAP.get(entityType);
		if (fields == null) {
			fields = ReflectUtils.getAnnotatedFields(entityType, Column.class);
			SnaEntity.FIELDS_MAP.put(entityType, fields);
		}
		return fields;
	}

	private static final <E extends SnaEntity> java.lang.reflect.Field getUniqueFieldPrimaryKey(E entity) {
		Class<E> entityType = (Class<E>) entity.getClass();
		PrimaryKey primaryKey = entityType.getAnnotation(PrimaryKey.class);
		String[] columns = null;
		if (primaryKey == null || (columns = primaryKey.value()).length != 1) {
			return null;
		}
		Map<Field, Column> fields = SnaEntity.getFields(entityType);

		Iterator<Map.Entry<java.lang.reflect.Field, Column>> iterator = fields.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<Field, Column> entry = iterator.next();
			if (columns[0].equals(entry.getValue().value())) {
				java.lang.reflect.Field field = entry.getKey();
				return field;
			}
		}
		return null;
	}

	/**
	 * @param snaEntityType
	 * @return
	 */
	public static <E extends SnaEntity> long getUniqueLongPrimaryKey(E entity) {
		java.lang.reflect.Field field = getUniqueFieldPrimaryKey(entity);

		if (field != null) {
			try {
				field.setAccessible(true);
				return CastUtils.cast(long.class, field.get(entity));
			} catch (Exception e) {
				LOG.debug(e.getMessage());
			}
		}
		return -1;
	}

	/**
	 * @param snaEntityType
	 * @return
	 */
	public static <E extends SnaEntity> boolean setUniqueLongPrimaryKey(E entity, long identifier) {
		java.lang.reflect.Field field = getUniqueFieldPrimaryKey(entity);

		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(entity, identifier);
				return true;

			} catch (Exception e) {
				LOG.debug(e.getMessage());
			}
		}
		return false;
	}

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * List of SnaEntity.Key of this SnaEntity
	 */
	protected List<Key> keys;

	/**
	 * Constructor
	 * 
	 */
	protected SnaEntity() {
		Map<Field, Column> fields = SnaEntity.getFields(getClass());
		this.keys = this.createKeyList(this.getClass().getAnnotation(PrimaryKey.class), fields);
	}

	/**
	 * Constructor
	 * 

	 * @param row
	 *            the JSONObject describing this SnaEntity
	 */
	protected SnaEntity(JSONObject row) {
		this();
		if (JSONObject.NULL.equals(row)) {
			return;
		}
		Table table = this.getClass().getAnnotation(Table.class);
		Map<Field, Column> fields = SnaEntity.getFields(getClass());

		Iterator<Map.Entry<java.lang.reflect.Field, Column>> iterator = fields.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<Field, Column> entry = iterator.next();
			// ForeignKey foreignKey = entry.getKey().getAnnotation(
			// ForeignKey.class);

			// if(foreignKey == null)
			// {
			String rowColumn = entry.getValue().value();
			String column = new StringBuilder().append(table.value()).append(SnaDAO.DOT).append(rowColumn).toString();

			if (row.has(column)) {
				setFieldValue(entry.getKey(), row.get(column));

			} else if (row.has(rowColumn)) {
				setFieldValue(entry.getKey(), row.get(rowColumn));
			}
			// } else if(SnaEntity.class.isAssignableFrom(entry.getKey().getType()))
			// {
			// try
			// {
			// Object entity = entry.getKey().getType().getConstructor(JSONObject.class
			//).newInstance(row);
			// setFieldValue(entry.getKey(), entity);
			//
			// } catch(Exception e)
			// {
			// e.printStackTrace();
			// }
			// }
		}
	}

	/**
	 * @return
	 */
	public List<Key> getKeys() {
		return Collections.<Key>unmodifiableList(this.keys);
	}

	/**
	 * @param entity
	 */
	protected List<Key> createKeyList(PrimaryKey primaryKey, Map<Field, Column> fields) {
		List<Key> keyMap = new ArrayList<Key>();
		String[] keys = primaryKey == null ? null : primaryKey.value();
		int length = keys == null ? 0 : keys.length;

		if (length == 0) {
			return keyMap;
		}
		Iterator<Map.Entry<Field, Column>> iterator = fields.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<Field, Column> entry = iterator.next();
			String column = entry.getValue().value();

			int index = 0;
			for (; index < length; index++) {
				if (keys[index].equals(column)) {
					break;
				}
			}
			if (index == length) {
				continue;
			}
			keyMap.add(new Key(entry.getKey()));
		}
		return keyMap;
	}

	/**
	 * @param field
	 * @param value
	 */
	public Object getFieldValue(String field) {
		if (field == null || field.length() == 0) {
			return null;
		}
		Map<Field, Column> fields = SnaEntity.getFields(this.getClass());
		Iterator<Map.Entry<Field, Column>> iterator = fields.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Field, Column> entry = iterator.next();
			if (field.equals(entry.getValue().value())) {
				return getFieldValue(entry.getKey());
			}
		}
		return null;
	}

	/**
	 * @param field
	 * @param value
	 */
	public Object getFieldValue(Field field) {
		Object object = null;
		try {
			field.setAccessible(true);
			object = field.get(SnaEntity.this);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// ForeignKey foreignKey = null;
		//
		// if(object!=null && SnaEntity.class.isAssignableFrom(
		// field.getType()) && (foreignKey = field.getAnnotation(
		// ForeignKey.class))!=null)
		// {
		// object = ((SnaEntity) object).getFieldValue(foreignKey.refer());
		// }
		return object;

	}

	/**
	 * @param field
	 * @param value
	 */
	protected void setFieldValue(Field field, Object value) {
		Method method = null;
		try {
			if ((method = this.getMethod(this.getMethodName(field.getName(), "set"), field.getType())) != null) {
				method.invoke(this, CastUtils.cast(field.getType(), value));
			} else {
				field.setAccessible(true);
				field.set(this, CastUtils.cast(field.getType(), value));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param methodName
	 * @param fieldType
	 * @return
	 */
	private Method getMethod(String methodName, Class<?> fieldType) {
		try {
			return this.getClass().getDeclaredMethod(methodName, fieldType);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param fieldName
	 * @param prefix
	 * @return
	 */
	private String getMethodName(String fieldName, String prefix) {
		String firstUpper = fieldName.substring(0, 1).toUpperCase();
		String methodName = new StringBuilder().append(prefix).append(firstUpper).append(fieldName.substring(1))
				.toString();
		return methodName;
	}
}
