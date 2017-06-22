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
package org.eclipse.sensinact.gateway.sthbnd.android.internal;

import org.json.JSONObject;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Created by nj246216 on 02/10/15.
 */
public class AndroidPacketReader extends SimplePacketReader<AndroidPacket>
{

    public AndroidPacketReader(Mediator Mediator)
    {
    	super(Mediator);
    }

    @Override
    public void parse(AndroidPacket packet) throws InvalidPacketException
    {
    	String jsonStr = new String(packet.getBytes());
    	try
    	{    		
    		JSONObject json = new JSONObject(jsonStr);
    		String location = (String) json.opt("location");
    		if(location != null)
    		{
    			super.setServiceProviderId("android");
    			super.setServiceId("admin");
    			super.setResourceId("location");
    			super.setData(location);
    			super.configure();
    		}
    		double alpha = json.optDouble("alpha");
    		if(Double.NaN != alpha)
    		{
    			super.setServiceProviderId("android");
    			super.setServiceId("accelerometer");
    			super.setResourceId("alpha");
    			super.setData((float)alpha);
    			super.configure();
    		}
    		double beta = (float) json.optDouble("beta");
    		if(Double.NaN != beta)
    		{
    			super.setServiceProviderId("android");
    			super.setServiceId("accelerometer");
    			super.setResourceId("beta");
    			super.setData((float)beta);
    			super.configure();
    		}
    		double epsilon = (float) json.optDouble("epsilon");
    		if(Double.NaN != epsilon)
    		{
    			super.setServiceProviderId("android");
    			super.setServiceId("accelerometer");
    			super.setResourceId("epsilon");
    			super.setData((float)epsilon);
    			super.configure();
    		}    		
    	} catch(Exception e)
    	{
    		throw new InvalidPacketException(e);
    	}
    }
}
