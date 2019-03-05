package org.eclipse.sensinact.gateway.sthbnd.inovallee;

import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.method.AbstractAccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor.ExecutionPolicy;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.generic.ExtModelInstance;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.eclipse.sensinact.gateway.generic.ExtServiceImpl;
import org.eclipse.sensinact.gateway.generic.annotation.Act;
import org.eclipse.sensinact.gateway.generic.annotation.Get;
import org.eclipse.sensinact.gateway.generic.annotation.Subscribe;
import org.eclipse.sensinact.gateway.generic.annotation.Unsubscribe;
import org.eclipse.sensinact.gateway.generic.parser.MethodDefinition;
import org.eclipse.sensinact.gateway.generic.parser.SignatureDefinition;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class InoResourceImpl extends ExtResourceImpl {

	protected InoResourceImpl(ExtModelInstance<?> modelInstance, ExtResourceConfig resourceConfig, ExtServiceImpl service) {
		super(modelInstance, resourceConfig, service);
		System.out.println("-----------> INIT");
		System.out.println("-----------> " + getName());
		System.out.println("-----------> " + getPath());
		
	}

	@Get
    public JSONObject notNamedAct() {
		System.out.println("-----------> NO ARGS");
        return new JSONObject();
    }

	
	@Get
    public JSONObject notNamedAct(String arg1) {
		System.out.println("-----------> 1 arg : " + arg1);
        return new JSONObject();
    }
	
	
	@Get
    public JSONObject notNamedAct(String arg1, String arg2) {
		System.out.println("-----------> 2 args : " + arg1 + " " + arg2);
		return new JSONObject();
    }	
}

