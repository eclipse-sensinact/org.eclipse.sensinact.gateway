/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.historic.storage.agent.generic;

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
			return;
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
