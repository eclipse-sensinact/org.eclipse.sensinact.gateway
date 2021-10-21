package org.eclipse.sensinact.gateway.sthbnd.http.factory.packet;

public abstract class Formatter {

	static String removeSurroundingQuotes(String s) {
		if(s == null)
			return s;
		String r = s;
		if(r.startsWith("\"") && r.endsWith("\"") 
		 || r.startsWith("'") && r.endsWith("'"))
			r = r.substring(1,r.length()-1);
		return r;
	}
	
	static String removeQuotes(String s) {
		if(s == null)
			return s;
		String r = removeQuotes(s,'_');
		return r;
	}

	static String removeQuotes(String s, char replacement) {
		if(s == null)
			return s;
		String r = removeSurroundingQuotes(s);
		r = r.replace('"', replacement);
		r = r.replace('\'',replacement);
		return r;
	}
	
	static String removeSurroundingSpaces(String s) {
		if(s == null)
			return s;
		String r = s;
		while(true) {
			if(r.length() > 0 && (r.startsWith(" ") || r.endsWith(" "))) {
				r = r.trim();
				continue;
			}
			break;
		}
		return r;
	}
	
	static String removeSpaces(String s) {
		if(s == null)
			return s;
		return removeSpaces(s, '_');
	}

	static String removeSpaces(String s, char replacement) {
		if(s == null)
			return s;
		String r = removeSurroundingSpaces(s);
		r = r.replace(' ', replacement);
		return r;
	}
	
	static String formatProviderId(String providerId) {
		String s = removeQuotes(providerId);
		s = removeSpaces(s);
		return s;
	}

}
