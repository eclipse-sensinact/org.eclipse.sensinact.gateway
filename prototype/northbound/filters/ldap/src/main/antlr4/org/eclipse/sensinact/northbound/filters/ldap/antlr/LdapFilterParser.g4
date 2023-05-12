/*********************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 * ********************************************************************
 */

parser grammar LdapFilterParser;

options {
	tokenVocab = LdapFilterLexer;
	language = Java;
}

comparator: APPROX | LESS_EQ | GREATER_EQ | EQUAL;

escaped_hex: ESCAPE_CHAR (DIGIT | HEX_ALPHA) (DIGIT | HEX_ALPHA);
escaped_quote: ESCAPE_CHAR QUOTE;
escaped_escape: ESCAPE_CHAR ESCAPE_CHAR;
alpha: HEX_ALPHA | OTHER_ALPHA;

valid_attr: (STAR | (alpha | DIGIT | UNDERCORE)+) (
		alpha
		| DIGIT
		| MINUS
		| UNDERCORE
  | COLUMN
	)*;
number: DIGIT+;

filter: WS* LPAR WS* filterContent WS* RPAR WS*;

filterContent: andFilter | orFilter | notFilter | comparison;
andFilter: AND filter+;
orFilter: OR filter+;
notFilter: NOT filter;

attr: MODEL | PROVIDER | (valid_attr (DOT valid_attr)*);

value:
	anyValue
	| nullValue
	| numericValue
	| booleanValue
	| stringValue;
anyValue: STAR;
nullValue: NULL;
booleanValue: TRUE | FALSE;
sign: MINUS | PLUS;
numericValue: sign? number (DOT number?)? | sign? DOT number;

pureString: (
		escaped_hex
		| alpha
		| DIGIT
		| DOT
		| OR
		| AND
		| UNDERCORE
		| MINUS
		| PLUS
		| comparator
		| WS
		| MODEL
		| PROVIDER
  | COLUMN
		| OTHER
	)+;

regexString: pureString (STAR regexString?)* | STAR regexString+;

quotedString:
	QUOTE (
		escaped_quote
		| regexString
		| LPAR
		| RPAR
		| NULL
		| TRUE
		| FALSE
		| OTHER
	)* QUOTE;

stringValue: quotedString | regexString;

comparison: attr comparator value;
