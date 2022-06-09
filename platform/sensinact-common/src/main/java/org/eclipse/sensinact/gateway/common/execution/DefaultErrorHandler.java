/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;

/**
 * Default {@link ErrorHandler} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DefaultErrorHandler implements ErrorHandler {
	
	private static final Logger LOG = Logger.getLogger(DefaultErrorHandler.class.getName());
    private List<JsonObject> errors;

    private volatile int exceptions = 0;
	
	private Execution alternative;
	private Object[] alternativeParameters;

	private Object alternativeResult;
	
	private final int policy;
	
	public DefaultErrorHandler(){
		this(ErrorHandler.Policy.DEFAULT_POLICY);
	}
	
	public DefaultErrorHandler(int policy){
		this.policy = policy;
	}
	
    /**
     * @inheritedDoc
     * 
     * @see org.eclipse.sensinact.gateway.common.execution.ErrorHandler#register(java.lang.Exception)
     */
    @Override
    public int handle(Exception exception) {
        if (exception == null) {
            return ErrorHandler.Policy.IGNORE;
        }
        this.exceptions++;
        JsonProvider provider = JsonProviderFactory.getProvider();
		JsonObjectBuilder exceptionObject = provider.createObjectBuilder();
        exceptionObject.add("message", exception.getMessage() == null ? JsonValue.NULL : 
        	provider.createValue(exception.getMessage()));

        StringBuilder buffer = new StringBuilder();
        if (exception != null) {
            StackTraceElement[] trace = exception.getStackTrace();

            int index = 0;
            int length = trace.length;

            for (; index < length; index++) {
                buffer.append(trace[index].toString());
                buffer.append("\n");
            }
        }
        exceptionObject.add("trace", buffer.toString());

        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        errors.add(exceptionObject.build());
        int policy = this.getPolicy();
        
        if(ErrorHandler.Policy.contains(policy, ErrorHandler.Policy.LOG)){
        	LOG.log(Level.SEVERE, exception.getMessage(), exception);
        }
        if(ErrorHandler.Policy.contains(policy, ErrorHandler.Policy.ALTERNATIVE)){
        	if(this.alternative != null){
        		Object[] arguments = this.alternativeParameters;
        		this.alternative.prepare(arguments);
        		this.alternative.process(arguments);
        		this.alternative.conclude();        		
        		this.alternativeResult = this.alternative.getResult();
        	}
        }
        if(ErrorHandler.Policy.contains(policy, ErrorHandler.Policy.IGNORE)){
        	return ErrorHandler.Policy.IGNORE;
        }
        if(ErrorHandler.Policy.contains(policy, ErrorHandler.Policy.ROLLBACK)){
        	return ErrorHandler.Policy.ROLLBACK;
        }
        if(ErrorHandler.Policy.contains(policy, ErrorHandler.Policy.CONTINUE)){
        	return ErrorHandler.Policy.CONTINUE;
        }
        return ErrorHandler.Policy.STOP;        
    }

    /**
     * @inheritedDoc
     * 
     * @see org.eclipse.sensinact.gateway.common.execution.ErrorHandler#getPolicy()
     */
    @Override
    public int getPolicy() {
        return this.policy;
    }

    /**
     * @inheritedDoc
     * 
     * @see org.eclipse.sensinact.gateway.common.execution.ErrorHandler#getStackTrace()
     */
    @Override
    public JsonArray getStackTrace() {
        return JsonProviderFactory.getProvider().createArrayBuilder(errors).build();
    }

    /**
     * @inheritedDoc
     * 
     * @see org.eclipse.sensinact.gateway.common.execution.ErrorHandler#getAlternative()
     */
    @Override
    public void setAlternative(Execution alternative ) {
    	this.alternative = alternative;;
    }

    /**
     * @inheritDoc
     * 
	 * @see org.eclipse.sensinact.gateway.common.execution.ErrorHandler#setAlternativeParameters(java.lang.Object[])
	 */
	@Override
	public void setAlternativeParameters(Object[] parameters) {
		this.alternativeParameters = parameters;		
	}

    /**
     * @inheritDoc
     * 
	 * @see org.eclipse.sensinact.gateway.common.execution.ErrorHandler#getExceptions()
	 */
	@Override
	public int getExceptions() {
		return this.exceptions;
	}

    /**
     * @inheritDoc
     * 
	 * @see org.eclipse.sensinact.gateway.common.execution.ErrorHandler#getAlternativeResult()
	 */
	@Override
	public Object getAlternativeResult() {
		return this.alternativeResult;
	}
    
}
