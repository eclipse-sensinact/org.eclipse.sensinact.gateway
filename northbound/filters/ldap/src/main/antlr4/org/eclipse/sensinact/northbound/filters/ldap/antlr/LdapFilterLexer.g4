/*********************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 * ********************************************************************
 */

lexer grammar LdapFilterLexer;

options {
  language = Java;
}

SKIPPED: [\r\n\b\f] -> skip;
WS: [ \t];

TRUE options { caseInsensitive = true; }: 'true';
FALSE options { caseInsensitive = true; }: 'false';
NULL options { caseInsensitive = true; }: 'null';

LPAR: '(';
RPAR: ')';

AND: '&';
OR: '|';
NOT: '!';

APPROX: '~=';
GREATER_EQ: '>=';
LESS_EQ: '<=';
EQUAL: '=';

DOT: '.';
STAR: '*';
UNDERCORE: '_';
PLUS: '+';
MINUS: '-';
COLUMN: ':';

PACKAGE: 'PACKAGE';
MODEL: 'MODEL';
PROVIDER: 'PROVIDER';

HEX_ALPHA: [A-Fa-f];
OTHER_ALPHA: [F-Zf-z];
DIGIT: [0-9];

QUOTE: '"';

ESCAPE_CHAR: '\\';

OTHER: .;
