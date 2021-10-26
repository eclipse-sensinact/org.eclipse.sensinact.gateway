package org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RootMappingTree {
	
	public static enum Action {
		Select, Ignore, Search;
	}
	
	private static class Node {
		private final Map<String, Node> children = new HashMap<>();
		
		private boolean terminal;
		
		private boolean wildcard;
		
		public boolean isTerminal() {
			return terminal;
		}
		
		public boolean isWildcard() {
			return wildcard;
		}
		
		public boolean hasChildren() {
			return !children.isEmpty();
		}
		
		public Node getChild(String s) {
			return children.get(s);
		}
		
		private void addChild(String s) {
			if(s.isEmpty()) {
				if(wildcard || hasChildren()) {
					throw new IllegalArgumentException("A selected path may not have children");
				}
				terminal = true;
			} else if (s.equals("*")) {
				if(terminal || hasChildren()) {
					throw new IllegalArgumentException("A wildcard path may not be selected or have children");
				}
				wildcard = true;
			} else {
				if(wildcard || terminal) {
					throw new IllegalArgumentException("A path may not be a child of a selected or wildcard path");
				}
				int idx = s.indexOf('/');
				if(idx < 0) {
					idx = s.length();
				}
				Node node = children.computeIfAbsent(s.substring(0, idx), k -> new Node());
				node.addChild(idx == s.length() ? "" : s.substring(idx + 1));
			}
		}
	}
	
	private final Node rootNode = new Node();
	
	public RootMappingTree(Collection<String> selections) {
		if(selections.isEmpty()) {
			rootNode.addChild("");
		} else {
			selections.forEach(rootNode::addChild);
		}
	}

	public Action getAction(List<Object> context) {

		Action toReturn;
		if(context.isEmpty()) {
			if(rootNode.isTerminal()) {
				toReturn = Action.Select;
			} else if(rootNode.isWildcard() || rootNode.hasChildren()) {
				toReturn = Action.Search;
			} else {
				toReturn = Action.Ignore;
			}
		} else {
			Node n = rootNode;
			
			for(int i = 0; i < (context.size() - 1) ; i++) {
				n = n.getChild(context.get(i).toString());
				if(n == null) {
					break;
				}
			}
			
			if(n == null) {
				toReturn = Action.Ignore;
			} else if (n.isWildcard()) {
				toReturn = Action.Select;
			} else {
				n = n.getChild(context.get(context.size() - 1).toString());
				if(n == null) {
					toReturn = Action.Ignore;
				} else if(n.isTerminal()) {
					toReturn = Action.Select;
				} else {
					toReturn = Action.Search;
				}
			}
		}
		return toReturn;
	}

	public String getMappedPath(LinkedList<Object> context) {
		if(context.isEmpty()) {
			return rootNode.isTerminal() ? "" : null;
		} else {
			StringBuilder sb = new StringBuilder();
			Node n = rootNode;
			
			for(int i = 0; i < (context.size() - 1) ; i++) {
				String s = context.get(i).toString();
				n = n.getChild(s);
				if(n == null) {
					return null;
				} else {
					sb.append(s).append("/");
				}
			}
			
			if (n.isWildcard()) {
				sb.append("*");
			} else {
				String s = context.get(context.size() - 1).toString();
				n = n.getChild(s);
				if(n == null || !n.isTerminal()) {
					return null;
				} else {
					sb.append(s);
				}
			}
			return sb.toString();
		}
	}
}
