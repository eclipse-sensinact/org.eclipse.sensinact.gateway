/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.dao.directive;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.dao.SnaDAO;
import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class CreateDirective extends Directive
{
	
	//********************************************************************//
	//						NESTED DECLARATIONS		    				  //
	//********************************************************************//


	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
	

	//********************************************************************//
	//						STATIC DECLARATIONS		      				  //
	//********************************************************************//

	/**
	 * @param mediator
	 * @param entityType
	 * @param fields
	 * 
	 * @return
	 */
	public static <E extends SnaEntity> CreateDirective 
	getCreateDirective(Mediator mediator, E entity)
	{		
        Class<? extends SnaEntity> entityType = entity.getClass();
		
		Table table = entityType.getAnnotation(Table.class);
		PrimaryKey primaryKey = entityType.getAnnotation(PrimaryKey.class);
		Map<Field, Column> fields = SnaEntity.getFields(entityType);
		
		CreateDirective createDirective = new CreateDirective(mediator,table.value());		
		Iterator<Map.Entry<Field, Column>> iterator = fields.entrySet().iterator();
		
		String[] keys = primaryKey==null?null:primaryKey.value();
		int index = 0;
		int length = keys==null?0:keys.length;
		
		while(iterator.hasNext())
		{
			Map.Entry<Field, Column> entry = iterator.next();
			
			index = 0;
			for(;index < length; index++)
			{
				if(keys[index].equals(entry.getValue().value())
					&& entry.getKey().getAnnotation(
						ForeignKey.class)==null)
				{
					break;
				}
			}
			if(index < length)
			{
				continue;
			}
			createDirective.create(entry.getValue().value(), 
					entity.getFieldValue(entry.getKey()));
		}
		
		return createDirective;			
	}

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	
	protected Map<String, Object> create;
	
	/**
	 * Constructor
	 */
	public CreateDirective(Mediator mediator, String table)
	{
		super(mediator,table);
		this.create = new HashMap<String, Object>();
	}
	
	/**
	 * @param column
	 */
	public void create(String column, Object value)
	{
		if(column == null || column.length()==0)
		{
			return;
		}
		this.create.put(column, value);
	}	
	
	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{		
		StringBuilder columnsBuilder = new StringBuilder();
		columnsBuilder.append(SnaDAO.INSERT_DIRECTIVE);
		columnsBuilder.append(SnaDAO.SPACE);
		columnsBuilder.append(super.table);
		columnsBuilder.append(SnaDAO.OPEN_PARENTHESIS);
		
		StringBuilder valuesBuilder = new StringBuilder();
		valuesBuilder.append(SnaDAO.VALUES_DIRECTIVE);
		valuesBuilder.append(SnaDAO.OPEN_PARENTHESIS);
		
		Map.Entry<String,Object> entry = null;
		
		Iterator<Map.Entry<String,Object>> iterator = 
				this.create.entrySet().iterator();
		
		if(iterator.hasNext())
		{
			entry = iterator.next();
			columnsBuilder.append(entry.getKey());
			valuesBuilder.append(super.getStringValue(entry.getValue()));
		}
		while(iterator.hasNext())
		{	
			entry = iterator.next();
			columnsBuilder.append(SnaDAO.COMMA);
			columnsBuilder.append(entry.getKey());			
			valuesBuilder.append(SnaDAO.COMMA);
			valuesBuilder.append(super.getStringValue(entry.getValue()));
		}
		columnsBuilder.append(SnaDAO.CLOSE_PARENTHESIS);
		valuesBuilder.append(SnaDAO.CLOSE_PARENTHESIS);
		
		columnsBuilder.append(valuesBuilder.toString());
		String createDirective = columnsBuilder.toString();
//		System.out.println("**************************************");
//		System.out.println(createDirective);
//		System.out.println("**************************************");
		return createDirective;
	}

}
