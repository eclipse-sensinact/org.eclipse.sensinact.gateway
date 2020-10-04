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
package org.eclipse.sensinact.gateway.agent.storage.generic;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class Stack {
	private List<JSONObject> list = new ArrayList<>();
	
	public boolean isEmpty() {
		synchronized (list) {
			return list.isEmpty();
		}
	}
	
	public int size() {
		synchronized (list) {
			return list.size();
		}
	}
	
	public void push(JSONObject element) {
		if (element == null)
			throw new IllegalArgumentException("Can't Push a null element");
		synchronized (list) {
			list.add(element);
		}
	}
	
	public JSONObject pop() {
		synchronized (list) {
			if (list.isEmpty())
				return null;
			return list.remove(list.size()-1);
		}
	}	
}
