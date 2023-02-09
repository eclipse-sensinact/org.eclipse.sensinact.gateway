//********************************************************************
//* Copyright (c) 2023 Contributors to the Eclipse Foundation.
//*
//* This program and the accompanying materials are made
//* available under the terms of the Eclipse Public License 2.0
//* which is available at https://www.eclipse.org/legal/epl-2.0/
//*
//* SPDX-License-Identifier: EPL-2.0
//*
//* Contributors:
//*   Kentyou - initial implementation
//**********************************************************************/

// Grammar based on an extract of the ABNF OData grammar:
// http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/abnf/odata-abnf-construction-rules.txt
// Converted with AbnfToAntlr

grammar ODataFilter;

//------------------------------------------------------------------------------
// odata-abnf-construction-rules
//------------------------------------------------------------------------------
//
//    OData Version 4.0 Plus Errata 02
//    OASIS Standard incorporating Approved Errata 02
//    30 October 2014
//    Copyright (c) OASIS Open 2014. All Rights Reserved.
//    Source: http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/abnf/
//
//
// Technical Committee:
//   OASIS Open Data Protocol (OData) TC
//   https://www.oasis-open.org/committees/odata
//
// Chairs:
//   - Barbara Hartel (barbara.hartel@sap.com), SAP AG
//   - Ram Jeyaraman (Ram.Jeyaraman@microsoft.com), Microsoft
//
// Editors:
//   - Ralf Handl (ralf.handl@sap.com), SAP AG
//   - Michael Pizzo (mikep@microsoft.com), Microsoft
//   - Martin Zurmuehl (martin.zurmuehl@sap.com), SAP AG
//
// Additional artifacts:
//   This grammar is one component of a Work Product which consists of:
//   - OData Version 4.0 Part 1: Protocol
//   - OData Version 4.0 Part 2: URL Conventions
//   - OData Version 4.0 Part 3: Common Schema Definition Language (CSDL)
//   - OData ABNF Construction Rules Version 4.0 (this document)
//   - OData ABNF Test Cases
//   - OData Core Vocabulary
//   - OData Capabilities Vocabulary
//   - OData Measures Vocabulary
//   - OData Metadata Service Entity Model
//   - OData EDMX XML Schema
//   - OData EDM XML Schema
//
// Related work:
//   This work product is related to the following two Work Products, each of
//   which define alternate formats for OData payloads
//   - OData Atom Format Version 4.0
//   - OData JSON Format Version 4.0
//   This specification replaces or supersedes:
//   - None
//
// Declared XML namespaces:
//   - http://docs.oasis-open.org/odata/ns/edmx
//   - http://docs.oasis-open.org/odata/ns/edm
//
// Abstract:
//   The Open Data Protocol (OData) enables the creation of REST-based data
//   services, which allow resources, identified using Uniform Resource
//   Identifiers (URLs) and defined in a data model, to be published and
//   edited by Web clients using simple HTTP messages. This document defines
//   the URL syntax for requests and the serialization format for primitive
//   literals in request and response payloads.
//
// Overview:
//   This grammar uses the ABNF defined in RFC5234 with one extension: literals
//   enclosed in single quotes (e.g. '$metadata') are treated case-sensitive.
//
//   The following rules assume that URIs have been percent-encoding normalized
//   as described in section 6.2.2.2 of RFC3986
//   (http://tools.ietf.org/html/rfc3986#section-6.2.2.2)
//   before applying the grammar to them, i.e. all characters in the unreserved
//   set (see rule "unreserved" below) are plain literals and NOT
//   percent-encoded.
//
//   For characters outside the unreserved set the rules explicitly state
//   whether the percent-encoded representation is treated identical to the
//   plain literal representation.
//
//   One prominent example is the single quote that delimits OData primitive
//   type literals: %27 and ' are treated identically, so a single quote within
//   a string literal is "encoded" as two consecutive single quotes in either
//   literal or percent-encoded representation.
//
// Contents:
//   1. Resource Path
//   2. Query Options
//   3. Context URL Fragments
//   4. Expressions
//   5. JSON format for function parameters
//   6. Names and identifiers
//   7. Literal Data Values
//   8. Header values
//   9. Punctuation
//
//   A. URI syntax [RFC3986]
//   B. IRI syntax [RFC3986]
//   C. ABNF core definitions [RFC5234]
//
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
// 1. Resource Path
//------------------------------------------------------------------------------

collectionnavigation : ( SLASH qualifiedentitytypename )? ( collectionnavpath )?;
collectionnavpath    : keypredicate
                     | keypredicate singlenavigation
                     | collectionpath
                     | ref_1;

keypredicate     : simplekey | compoundkey;
simplekey        : open keypropertyvalue close;
compoundkey      : open keyvaluepair ( comma keyvaluepair )* close;
keyvaluepair     : ( primitivekeyproperty | keypropertyalias ) eq keypropertyvalue;
keypropertyvalue : primitiveliteral;
keypropertyalias : odataidentifier;

singlenavigation : ( SLASH qualifiedentitytypename )?
                   ( (SLASH propertypath)
                   | boundoperation
                   | ref_1
                   | value  // request the media resource of a media entity
                   )?;

propertypath : entitycolnavigationproperty
             | entitycolnavigationproperty collectionnavigation
             | entitynavigationproperty
             | entitynavigationproperty    singlenavigation
             | complexcolproperty
             | complexcolproperty          collectionpath
             | complexproperty
             | complexproperty             complexpath
             | primitivecolproperty
             | primitivecolproperty        collectionpath
             | primitiveproperty
             | primitiveproperty           singlepath
             | streamproperty
             | streamproperty              boundoperation;

collectionpath : count | boundoperation;

singlepath     : value | boundoperation;

complexpath    : ( SLASH qualifiedcomplextypename )?
                 ( (SLASH propertypath)
                 | boundoperation
                 );

count : (SLASH DOLLAR C O U N T);
ref_1   : (SLASH DOLLAR R E F);
value : (SLASH DOLLAR V A L U E);

// boundOperation segments can only be composed if the type of the previous segment
// matches the type of the first parameter of the action or function being called.
// Note that the rule name reflects the return type of the function.
boundoperation : SLASH ( boundactioncall
                     | boundentitycolfunccall
                     | boundentitycolfunccall     collectionnavigation
                     | boundentityfunccall
                     | boundentityfunccall        singlenavigation
                     | boundcomplexcolfunccall   ( SLASH qualifiedcomplextypename )?
                     | boundcomplexcolfunccall   ( SLASH qualifiedcomplextypename )?  collectionpath
                     | boundcomplexfunccall
                     | boundcomplexfunccall      complexpath
                     | boundprimitivecolfunccall
                     | boundprimitivecolfunccall  collectionpath
                     | boundprimitivefunccall
                     | boundprimitivefunccall     singlepath
                     );

boundactioncall  : namespace_1 PERIOD action;
                   // with the added restriction that the binding parameter MUST be either an entity or collection of entities
                   // and is specified by reference using the URI immediately preceding (to the left) of the boundActionCall

// The following boundXxxFuncCall rules have the added restrictions that
//  - the function MUST support binding, and
//  - the binding parameter type MUST match the type of resource identified by the
//    URI immediately preceding (to the left) of the boundXxxFuncCall, and
//  - the functionParameters MUST NOT include the bindingParameter.
boundentityfunccall       : namespace_1 PERIOD entityfunction       functionparameters;
boundentitycolfunccall    : namespace_1 PERIOD entitycolfunction    functionparameters;
boundcomplexfunccall      : namespace_1 PERIOD complexfunction      functionparameters;
boundcomplexcolfunccall   : namespace_1 PERIOD complexcolfunction   functionparameters;
boundprimitivefunccall    : namespace_1 PERIOD primitivefunction    functionparameters;
boundprimitivecolfunccall : namespace_1 PERIOD primitivecolfunction functionparameters;

functionparameters : open ( functionparameter ( comma functionparameter )* )? close;
functionparameter  : parametername eq ( parameteralias | primitiveliteral );
parametername      : odataidentifier;
parameteralias     : at odataidentifier;

//------------------------------------------------------------------------------
// 2. Query Options
//------------------------------------------------------------------------------

parametervalue : arrayorobject
               | commonexpr;

//------------------------------------------------------------------------------
// 4. Expressions
//------------------------------------------------------------------------------

// Note: a boolCommonExpr is also a commonExpr, e.g. sort by Boolean
commonexpr : ( primitiveliteral
             | parameteralias
             | arrayorobject
             | rootexpr
             | methodcallexpr
             | firstmemberexpr
             | functionexpr
             | negateexpr
             | parenexpr
             | castexpr
             )
             ( addexpr
             | subexpr
             | mulexpr
             | divexpr
             | modexpr
             )?;

boolcommonexpr : ( isofexpr
                 | boolmethodcallexpr
                 | notexpr
                 | (commonexpr
                   ( eqexpr
                   | neexpr
                   | ltexpr
                   | leexpr
                   | gtexpr
                   | geexpr
                   | hasexpr
                   )?)
                 | boolparenexpr
                 ) ( andexpr | orexpr )?;

rootexpr : (DOLLAR R O O T SLASH) ( (entitysetname keypredicate) | singletonentity ) ( singlenavigationexpr )?;

firstmemberexpr : memberexpr
                | (inscopevariableexpr ( SLASH memberexpr )?);

memberexpr : ( qualifiedentitytypename SLASH )?
             ( propertypathexpr
             | boundfunctionexpr
             );

propertypathexpr : ( (entitycolnavigationproperty ( collectionnavigationexpr )?)
                   | (entitynavigationproperty    ( singlenavigationexpr )?)
                   | (complexcolproperty          ( collectionpathexpr )?)
                   | (complexproperty             ( complexpathexpr )?)
                   | (primitivecolproperty        ( collectionpathexpr )?)
                   | (primitiveproperty           ( singlepathexpr )?)
                   | (streamproperty              ( singlepathexpr )?)
                   );

inscopevariableexpr       : implicitvariableexpr
                          | lambdavariableexpr; // only allowed inside a lambdaPredicateExpr
implicitvariableexpr      : (DOLLAR I T);              // references the unnamed outer variable of the query
lambdavariableexpr        : odataidentifier;

collectionnavigationexpr : ( SLASH qualifiedentitytypename )?
                           ( (keypredicate ( singlenavigationexpr )?)
                           | collectionpathexpr
                           );

singlenavigationexpr : SLASH memberexpr;

collectionpathexpr : count
                   | (SLASH boundfunctionexpr)
                   | (SLASH anyexpr)
                   | (SLASH allexpr);

complexpathexpr : SLASH ( qualifiedcomplextypename SLASH )?
                  ( propertypathexpr
                  | boundfunctionexpr
                  );

singlepathexpr : SLASH boundfunctionexpr;

boundfunctionexpr : functionexpr; // boundFunction segments can only be composed if the type of the
                                 // previous segment matches the type of the first function parameter

functionexpr : namespace_1 PERIOD
               ( (entitycolfunction    functionexprparameters ( collectionnavigationexpr )?)
               | (entityfunction       functionexprparameters ( singlenavigationexpr )?)
               | (complexcolfunction   functionexprparameters ( collectionpathexpr )?)
               | (complexfunction      functionexprparameters ( complexpathexpr )?)
               | (primitivecolfunction functionexprparameters ( collectionpathexpr )?)
               | (primitivefunction    functionexprparameters ( singlepathexpr )?)
               );

functionexprparameters : open ( functionexprparameter ( comma functionexprparameter )* )? close;
functionexprparameter  : parametername eq ( parameteralias | parametervalue );

anyexpr : (A N Y) open bws ( lambdavariableexpr bws colon bws lambdapredicateexpr )? bws close;
allexpr : (A L L) open bws   lambdavariableexpr bws colon bws lambdapredicateexpr   bws close;
lambdapredicateexpr : boolcommonexpr; // containing at least one lambdaVariableExpr

methodcallexpr : indexofmethodcallexpr
               | tolowermethodcallexpr
               | touppermethodcallexpr
               | trimmethodcallexpr
               | substringmethodcallexpr
               | concatmethodcallexpr
               | lengthmethodcallexpr
               | yearmethodcallexpr
               | monthmethodcallexpr
               | daymethodcallexpr
               | hourmethodcallexpr
               | minutemethodcallexpr
               | secondmethodcallexpr
               | fractionalsecondsmethodcallexpr
               | totalsecondsmethodcallexpr
               | datemethodcallexpr
               | timemethodcallexpr
               | roundmethodcallexpr
               | floormethodcallexpr
               | ceilingmethodcallexpr
               | distancemethodcallexpr
               | geolengthmethodcallexpr
               | totaloffsetminutesmethodcallexpr
               | mindatetimemethodcallexpr
               | maxdatetimemethodcallexpr
               | nowmethodcallexpr;

boolmethodcallexpr : endswithmethodcallexpr
                   | startswithmethodcallexpr
                   | containsmethodcallexpr
                   | intersectsmethodcallexpr;

containsmethodcallexpr   : ((C O N T A I N S) | (S U B S T R I N G O F))   open bws commonexpr bws comma bws commonexpr bws close;
startswithmethodcallexpr : (S T A R T S W I T H) open bws commonexpr bws comma bws commonexpr bws close;
endswithmethodcallexpr   : (E N D S W I T H)   open bws commonexpr bws comma bws commonexpr bws close;
lengthmethodcallexpr     : (L E N G T H)     open bws commonexpr bws close;
indexofmethodcallexpr    : (I N D E X O F)    open bws commonexpr bws comma bws commonexpr bws close;
substringmethodcallexpr  : (S U B S T R I N G)  open bws commonexpr bws comma bws commonexpr bws ( comma bws commonexpr bws )? close;
tolowermethodcallexpr    : (T O L O W E R)    open bws commonexpr bws close;
touppermethodcallexpr    : (T O U P P E R)    open bws commonexpr bws close;
trimmethodcallexpr       : (T R I M)       open bws commonexpr bws close;
concatmethodcallexpr     : (C O N C A T)     open bws commonexpr bws comma bws commonexpr bws close;

yearmethodcallexpr               : (Y E A R)               open bws commonexpr bws close;
monthmethodcallexpr              : (M O N T H)              open bws commonexpr bws close;
daymethodcallexpr                : (D A Y)                open bws commonexpr bws close;
hourmethodcallexpr               : (H O U R)               open bws commonexpr bws close;
minutemethodcallexpr             : (M I N U T E)             open bws commonexpr bws close;
secondmethodcallexpr             : (S E C O N D)             open bws commonexpr bws close;
fractionalsecondsmethodcallexpr  : (F R A C T I O N A L S E C O N D S)  open bws commonexpr bws close;
totalsecondsmethodcallexpr       : (T O T A L S E C O N D S)       open bws commonexpr bws close;
datemethodcallexpr               : (D A T E)               open bws commonexpr bws close;
timemethodcallexpr               : (T I M E)               open bws commonexpr bws close;
totaloffsetminutesmethodcallexpr : (T O T A L O F F S E T M I N U T E S) open bws commonexpr bws close;

mindatetimemethodcallexpr : (M I N D A T E T I M E LEFT_PAREN) bws RIGHT_PAREN;
maxdatetimemethodcallexpr : (M A X D A T E T I M E LEFT_PAREN) bws RIGHT_PAREN;
nowmethodcallexpr         : (N O W LEFT_PAREN) bws RIGHT_PAREN;

roundmethodcallexpr   : (R O U N D)   open bws commonexpr bws close;
floormethodcallexpr   : (F L O O R)   open bws commonexpr bws close;
ceilingmethodcallexpr : (C E I L I N G) open bws commonexpr bws close;

distancemethodcallexpr   : (G E O PERIOD D I S T A N C E)   open bws commonexpr bws comma bws commonexpr bws close;
geolengthmethodcallexpr  : (G E O PERIOD L E N G T H)     open bws commonexpr bws close;
intersectsmethodcallexpr : (G E O PERIOD I N T E R S E C T S) open bws commonexpr bws comma bws commonexpr bws close;

boolparenexpr : open bws boolcommonexpr bws close;
parenexpr     : open bws commonexpr     bws close;

andexpr : rws (A N D) rws boolcommonexpr;
orexpr  : rws (O R)  rws boolcommonexpr;

eqexpr : rws (E Q) rws commonexpr;
neexpr : rws (N E) rws commonexpr;
ltexpr : rws (L T) rws commonexpr;
leexpr : rws (L E) rws commonexpr;
gtexpr : rws (G T) rws commonexpr;
geexpr : rws (G E) rws commonexpr;

hasexpr : rws (H A S) rws enum_1;

addexpr : rws (A D D) rws commonexpr;
subexpr : rws (S U B) rws commonexpr;
mulexpr : rws (M U L) rws commonexpr;
divexpr : rws (D I V) rws commonexpr;
modexpr : rws (M O D) rws commonexpr;

negateexpr : DASH bws commonexpr;

notexpr : (N O T) rws boolcommonexpr;

isofexpr : (I S O F) open bws ( commonexpr bws comma bws )? qualifiedtypename bws close;
castexpr : (C A S T) open bws ( commonexpr bws comma bws )? qualifiedtypename bws close;


//------------------------------------------------------------------------------
// 5. JSON format for function parameters
//------------------------------------------------------------------------------
// Note: the query part of a URI needs to be partially percent-decoded before
// applying these rules, see comment at the top of this file
//------------------------------------------------------------------------------

arrayorobject : complexcolinuri
              | complexinuri
              | rootexprcol
              | primitivecolinuri;

complexcolinuri : begin_array
                  ( complexinuri ( value_separator complexinuri )* )?
                  end_array;

complexinuri : begin_object
               ( ( annotationinuri
                 | primitivepropertyinuri
                 | complexpropertyinuri
                 | collectionpropertyinuri
                 | navigationpropertyinuri
                 )
                 ( value_separator
                    ( annotationinuri
                    | primitivepropertyinuri
                    | complexpropertyinuri
                    | collectionpropertyinuri
                    | navigationpropertyinuri
                    )
                  )*
               )?
               end_object;

collectionpropertyinuri : ( quotation_mark primitivecolproperty quotation_mark
                            name_separator
                            primitivecolinuri
                          )
                        | ( quotation_mark complexcolproperty quotation_mark
                            name_separator
                            complexcolinuri
                          );

primitivecolinuri : begin_array
                    ( primitiveliteralinjson ( value_separator primitiveliteralinjson )* )?
                    end_array;

complexpropertyinuri : quotation_mark complexproperty quotation_mark
                       name_separator
                       complexinuri;

annotationinuri : quotation_mark at namespace_1 PERIOD termname quotation_mark
                  name_separator
                  ( complexinuri | complexcolinuri | primitiveliteralinjson | primitivecolinuri );

primitivepropertyinuri : quotation_mark primitiveproperty quotation_mark
                         name_separator
                         primitiveliteralinjson;

navigationpropertyinuri : singlenavpropinjson
                        | collectionnavpropinjson;
singlenavpropinjson     : quotation_mark entitynavigationproperty quotation_mark
                                                    name_separator
                                                    rootexpr;
collectionnavpropinjson : quotation_mark entitycolnavigationproperty quotation_mark
                                                    name_separator
                                                    rootexprcol;

rootexprcol : begin_array
              ( rootexpr ( value_separator rootexpr )* )?
              end_array;

// JSON syntax: adapted to URI restrictions from [RFC4627]
begin_object : bws ( LEFT_CURLY_BRACE | (PERCENT SEVEN (CAP_B | B)) ) bws;
end_object   : bws ( RIGHT_CURLY_BRACE | (PERCENT SEVEN (CAP_D | D)) ) bws;

begin_array : bws ( LEFT_BRACE | (PERCENT FIVE (CAP_B | B)) ) bws;
end_array   : bws ( RIGHT_BRACE | (PERCENT FIVE (CAP_D | D)) ) bws;

quotation_mark  : dquote | (PERCENT TWO TWO);
name_separator  : bws colon bws;
value_separator : bws comma bws;

primitiveliteralinjson : stringinjson
                       | numberinjson
                       | (T R U E)
                       | (F A L S E)
                       | (N U L L);

stringinjson : quotation_mark charinjson* quotation_mark;
charinjson   : qchar_unescaped
             | qchar_json_special
             | (escape ( quotation_mark
                      | escape
                      | ( SLASH | (PERCENT TWO (CAP_F | F)) ) // solidus         U+002F - literal form is allowed in the query part of a URL
                      | B             // backspace       U+0008
                      | F             // form feed       U+000C
                      | N             // line feed       U+000A
                      | R             // carriage return U+000D
                      | T             // tab             U+0009
                      | (U (hexdig hexdig hexdig hexdig))     //                 U+XXXX
                      ));

qchar_json_special : sp | COLON | LEFT_CURLY_BRACE | RIGHT_CURLY_BRACE | LEFT_BRACE | RIGHT_BRACE; // some agents put these unencoded into the query part of a URL

escape : BACKSLASH | (PERCENT FIVE (CAP_C | C));     // reverse solidus U+005C

numberinjson : ( DASH )? int_1 ( frac )? ( exp )?;
int_1          : ZERO | ( onetonine digit* );
frac         : PERIOD digit+;
exp          : (CAP_E | E) ( DASH | PLUS )? digit+;


//------------------------------------------------------------------------------
// 6. Names and identifiers
//------------------------------------------------------------------------------

singlequalifiedtypename : qualifiedentitytypename
                        | qualifiedcomplextypename
                        | qualifiedtypedefinitionname
                        | qualifiedenumtypename
                        | primitivetypename;

qualifiedtypename : singlequalifiedtypename
                  | ((CAP_C O L L E C T I O N) open singlequalifiedtypename close);

qualifiedentitytypename     : namespace_1 PERIOD entitytypename;
qualifiedcomplextypename    : namespace_1 PERIOD complextypename;
qualifiedtypedefinitionname : namespace_1 PERIOD typedefinitionname;
qualifiedenumtypename       : namespace_1 PERIOD enumerationtypename;

// an alias is just a single-part namespace
namespace_1     : namespacepart ( PERIOD namespacepart )*;
namespacepart : odataidentifier;

entitysetname       : odataidentifier;
singletonentity     : odataidentifier;
entitytypename      : odataidentifier;
complextypename     : odataidentifier;
typedefinitionname  : odataidentifier;
enumerationtypename : odataidentifier;
enumerationmember   : odataidentifier;
termname            : odataidentifier;

// Note: this pattern is overly restrictive, the normative definition is type TSimpleIdentifier in OData EDM XML Schema
odataidentifier : identifierleadingcharacter
                | identifierleadingcharacter (c+=identifiercharacter)+ {$c.size() <= 127}?;
identifierleadingcharacter  : alpha | UNDERSCORE;         // plus Unicode characters from the categories L or Nl
identifiercharacter         : alpha | UNDERSCORE | digit; // plus Unicode characters from the categories L, Nl, Nd, Mn, Mc, Pc, or Cf

primitivetypename : (CAP_E D M PERIOD) ( (CAP_B I N A R Y)
                           | (CAP_B O O L E A N)
                           | (CAP_B Y T E)
                           | (CAP_D A T E)
                           | (CAP_D A T E CAP_T I M E CAP_O F F S E T)
                           | (CAP_D E C I M A L)
                           | (CAP_D O U B L E)
                           | (CAP_D U R A T I O N)
                           | (CAP_G U I D)
                           | (CAP_I N T ONE SIX)
                           | (CAP_I N T THREE TWO)
                           | (CAP_I N T SIX FOUR)
                           | (CAP_S CAP_B Y T E)
                           | (CAP_S I N G L E)
                           | (CAP_S T R E A M)
                           | (CAP_S T R I N G)
                           | (CAP_T I M E CAP_O F CAP_D A Y)
                           | (abstractspatialtypename ( concretespatialtypename )?)
                           );
abstractspatialtypename : (CAP_G E O G R A P H Y)
                        | (CAP_G E O M E T R Y);
concretespatialtypename : (CAP_C O L L E C T I O N)
                        | (CAP_L I N E CAP_S T R I N G)
                        | (CAP_M U L T I CAP_L I N E CAP_S T R I N G)
                        | (CAP_M U L T I CAP_P O I N T)
                        | (CAP_M U L T I CAP_P O L Y G O N)
                        | (CAP_P O I N T)
                        | (CAP_P O L Y G O N);

primitiveproperty       : primitivekeyproperty | primitivenonkeyproperty;
primitivekeyproperty    : odataidentifier;
primitivenonkeyproperty : odataidentifier;
primitivecolproperty    : odataidentifier;
complexproperty         : odataidentifier;
complexcolproperty      : odataidentifier;
streamproperty          : odataidentifier;

entitynavigationproperty    : odataidentifier;
entitycolnavigationproperty : odataidentifier;

action       : odataidentifier;

function : entityfunction
         | entitycolfunction
         | complexfunction
         | complexcolfunction
         | primitivefunction
         | primitivecolfunction;

entityfunction       : odataidentifier;
entitycolfunction    : odataidentifier;
complexfunction      : odataidentifier;
complexcolfunction   : odataidentifier;
primitivefunction    : odataidentifier;
primitivecolfunction : odataidentifier;


//------------------------------------------------------------------------------
// 7. Literal Data Values
//------------------------------------------------------------------------------

// in URLs
primitiveliteral : nullvalue                  // plain values up to int64Value
                 | booleanvalue
                 | guidvalue
                 | datevalue
                 | datetimeoffsetvalue
                 | timeofdayvalue
                 | decimalvalue
                 | doublevalue
                 | singlevalue
                 | sbytevalue
                 | bytevalue
                 | int16value
                 | int32value
                 | int64value
                 | string_1                     // single-quoted
                 | duration                   // all others are quoted and prefixed
                 | binary
                 | enum_1
                 | geographycollection
                 | geographylinestring
                 | geographymultilinestring
                 | geographymultipoint
                 | geographymultipolygon
                 | geographypoint
                 | geographypolygon
                 | geometrycollection
                 | geometrylinestring
                 | geometrymultilinestring
                 | geometrymultipoint
                 | geometrymultipolygon
                 | geometrypoint
                 | geometrypolygon;

// in Atom and JSON message bodies and CSDL DefaultValue attributes
primitivevalue : booleanvalue
               | guidvalue
               | durationvalue
               | datevalue
               | datetimeoffsetvalue
               | timeofdayvalue
               | enumvalue
               | fullcollectionliteral
               | fulllinestringliteral
               | fullmultipointliteral
               | fullmultilinestringliteral
               | fullmultipolygonliteral
               | fullpointliteral
               | fullpolygonliteral
               | decimalvalue
               | doublevalue
               | singlevalue
               | sbytevalue
               | bytevalue
               | int16value
               | int32value
               | int64value
               | binaryvalue;
               // also valid are:
               // - any XML string for strings in Atom and CSDL documents
               // - any JSON string for JSON documents

nullvalue : (N U L L);

// base64url encoding according to http://tools.ietf.org/html/rfc4648#section-5
binary      : ((CAP_B | B) (CAP_I | I) (CAP_N | N) (CAP_A | A) (CAP_R | R) (CAP_Y | Y)) squote binaryvalue squote;
binaryvalue : ((base64char base64char base64char base64char))* ( base64b16  | base64b8 )?;
base64b16   : (base64char base64char) ( CAP_A | CAP_E | CAP_I | CAP_M | CAP_Q | CAP_U | CAP_Y | C | G | K | O | S | W | ZERO | FOUR | EIGHT )   ( EQUALS )?;
base64b8    : base64char ( CAP_A | CAP_Q | G | W ) ( (EQUALS EQUALS) )?;
base64char  : alpha | digit | DASH | UNDERSCORE;

booleanvalue : ((CAP_T | T) (CAP_R | R) (CAP_U | U) (CAP_E | E)) | ((CAP_F | F) (CAP_A | A) (CAP_L | L) (CAP_S | S) (CAP_E | E));

decimalvalue : (sign)? digit+ PERIOD (digit+)?;

doublevalue : (decimalvalue ( (CAP_E | E) (sign)? digit+ )?) | naninfinity; // IEEE 754 binary64 floating-point number (15-17 decimal digits)
singlevalue : doublevalue;                                       // IEEE 754 binary32 floating-point number (6-9 decimal digits)
naninfinity : (CAP_N A CAP_N) | (DASH CAP_I CAP_N CAP_F) | (CAP_I CAP_N CAP_F);

guidvalue : (hexdig hexdig hexdig hexdig hexdig hexdig hexdig hexdig) DASH (hexdig hexdig hexdig hexdig) DASH (hexdig hexdig hexdig hexdig) DASH (hexdig hexdig hexdig hexdig) DASH (hexdig hexdig hexdig hexdig hexdig hexdig hexdig hexdig hexdig hexdig hexdig hexdig);

bytevalue  : (digit ((digit digit) | digit?));           // numbers in the range from 0 to 255
sbytevalue : ( sign )? (digit ((digit digit) | digit?));  // numbers in the range from -128 to 127
int16value : ( sign )? (digit ((digit digit digit digit) | (digit digit digit) | (digit digit) | digit?));  // numbers in the range from -32768 to 32767
int32value : ( sign )? (digit ((digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit) | (digit digit digit digit digit digit) | (digit digit digit digit digit) | (digit digit digit digit) | (digit digit digit) | (digit digit) | digit?)); // numbers in the range from -2147483648 to 2147483647
int64value : ( sign )? (digit ((digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit) | (digit digit digit digit digit digit) | (digit digit digit digit digit) | (digit digit digit digit) | (digit digit digit) | (digit digit) | digit?)); // numbers in the range from -9223372036854775808 to 9223372036854775807

string_1           : squote ( squote_in_string | pchar_no_squote )* squote;
squote_in_string : squote squote; // two consecutive single quotes represent one within a string literal

datevalue : year DASH month DASH day;

datetimeoffsetvalue : year DASH month DASH day (CAP_T | T) hour COLON minute ( COLON second ( PERIOD fractionalseconds )? )? ( (CAP_Z | Z) | (sign hour COLON minute) );

duration      : ((CAP_D | D) (CAP_U | U) (CAP_R | R) (CAP_A | A) (CAP_T | T) (CAP_I | I) (CAP_O | O) (CAP_N | N)) squote durationvalue squote;
durationvalue : ( sign )? (CAP_P | P) ( digit+ (CAP_D | D) )? ( (CAP_T | T) ( digit+ (CAP_H | H) )? ( digit+ (CAP_M | M) )? ( digit+ ( PERIOD digit+ )? (CAP_S | S) )? )?;
     // the above is an approximation of the rules for an xml dayTimeDuration.
     // see the lexical representation for dayTimeDuration in http://www.w3.org/TR/xmlschema11-2#dayTimeDuration for more information

timeofdayvalue : hour COLON minute ( COLON second ( PERIOD fractionalseconds )? )?;

onetonine       : ONE | TWO | THREE | FOUR | FIVE | SIX | SEVEN | EIGHT | NINE;
zerotofiftynine : ( ZERO | ONE | TWO | THREE | FOUR | FIVE ) digit;
year  : ( DASH )? ( (ZERO (digit digit digit)) | (onetonine (digit digit digit+)) );
month : (ZERO onetonine)
      | (ONE ( ZERO | ONE | TWO ));
day   : (ZERO onetonine)
      | (( ONE | TWO ) digit)
      | (THREE ( ZERO | ONE ));
hour   : (( ZERO | ONE ) digit)
       | (TWO ( ZERO | ONE | TWO | THREE ));
minute : zerotofiftynine;
second : zerotofiftynine;
fractionalseconds : (digit ((digit digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit digit) | (digit digit digit digit digit digit digit) | (digit digit digit digit digit digit) | (digit digit digit digit digit) | (digit digit digit digit) | (digit digit digit) | (digit digit) | digit?));

enum_1            : qualifiedenumtypename squote enumvalue squote;
enumvalue       : singleenumvalue ( comma singleenumvalue )*;
singleenumvalue : enumerationmember | enummembervalue;
enummembervalue : int64value;

geographycollection   : geographyprefix squote fullcollectionliteral squote;
fullcollectionliteral : sridliteral? collectionliteral;
collectionliteral     : ((CAP_C | C) (CAP_O | O) (CAP_L | L) (CAP_L | L) (CAP_E | E) (CAP_C | C) (CAP_T | T) (CAP_I | I) (CAP_O | O) (CAP_N | N) LEFT_PAREN) geoliteral ( comma geoliteral )* close;
geoliteral            : collectionliteral
                      | linestringliteral
                      | multipointliteral
                      | multilinestringliteral
                      | multipolygonliteral
                      | pointliteral
                      | polygonliteral;

geographylinestring   : geographyprefix squote fulllinestringliteral squote;
fulllinestringliteral : sridliteral? linestringliteral;
linestringliteral     : ((CAP_L | L) (CAP_I | I) (CAP_N | N) (CAP_E | E) (CAP_S | S) (CAP_T | T) (CAP_R | R) (CAP_I | I) (CAP_N | N) (CAP_G | G)) linestringdata;
linestringdata        : open bws positionliteral bws ( comma bws positionliteral bws )+ close;

geographymultilinestring   : geographyprefix squote fullmultilinestringliteral squote;
fullmultilinestringliteral : sridliteral? multilinestringliteral;
multilinestringliteral     : ((CAP_M | M) (CAP_U | U) (CAP_L | L) (CAP_T | T) (CAP_I | I) (CAP_L | L) (CAP_I | I) (CAP_N | N) (CAP_E | E) (CAP_S | S) (CAP_T | T) (CAP_R | R) (CAP_I | I) (CAP_N | N) (CAP_G | G) LEFT_PAREN) ( linestringdata ( comma linestringdata )* )? close;

geographymultipoint   : geographyprefix squote fullmultipointliteral squote;
fullmultipointliteral : sridliteral? multipointliteral;
multipointliteral     : ((CAP_M | M) (CAP_U | U) (CAP_L | L) (CAP_T | T) (CAP_I | I) (CAP_P | P) (CAP_O | O) (CAP_I | I) (CAP_N | N) (CAP_T | T) LEFT_PAREN) ( pointdata ( comma pointdata )* )? close;

geographymultipolygon   : geographyprefix squote fullmultipolygonliteral squote;
fullmultipolygonliteral : sridliteral? multipolygonliteral;
multipolygonliteral     : ((CAP_M | M) (CAP_U | U) (CAP_L | L) (CAP_T | T) (CAP_I | I) (CAP_P | P) (CAP_O | O) (CAP_L | L) (CAP_Y | Y) (CAP_G | G) (CAP_O | O) (CAP_N | N) LEFT_PAREN) ( polygondata ( comma polygondata )* )? close;

geographypoint   : geographyprefix squote fullpointliteral squote;
fullpointliteral : sridliteral? pointliteral;
sridliteral      : ((CAP_S | S) (CAP_R | R) (CAP_I | I) (CAP_D | D)) eq (digit ((digit digit digit digit) | (digit digit digit) | (digit digit) | digit?)) semi;
pointliteral     :((CAP_P | P) (CAP_O | O) (CAP_I | I) (CAP_N | N) (CAP_T | T)) bws pointdata;
pointdata        : open bws positionliteral bws close;
positionliteral  : (int16value | doublevalue) rws (int16value | doublevalue);  // longitude, then latitude

geographypolygon   : geographyprefix squote fullpolygonliteral squote;
fullpolygonliteral : sridliteral? polygonliteral;
polygonliteral     : ((CAP_P | P) (CAP_O | O) (CAP_L | L) (CAP_Y | Y) (CAP_G | G) (CAP_O | O) (CAP_N | N)) polygondata;
polygondata        : open ringliteral ( comma ringliteral )* close;
ringliteral        : open positionliteral ( comma positionliteral )* close;
                   // Within each ringLiteral, the first and last positionLiteral elements MUST be an exact syntactic match to each other.
                   // Within the polygonData, the ringLiterals MUST specify their points in appropriate winding order.
                   // In order of traversal, points to the left side of the ring are interpreted as being in the polygon.

geometrycollection      : geometryprefix squote fullcollectionliteral      squote;
geometrylinestring      : geometryprefix squote fulllinestringliteral      squote;
geometrymultilinestring : geometryprefix squote fullmultilinestringliteral squote;
geometrymultipoint      : geometryprefix squote fullmultipointliteral      squote;
geometrymultipolygon    : geometryprefix squote fullmultipolygonliteral    squote;
geometrypoint           : geometryprefix squote fullpointliteral           squote;
geometrypolygon         : geometryprefix squote fullpolygonliteral         squote;

geographyprefix : ((CAP_G | G) (CAP_E | E) (CAP_O | O) (CAP_G | G) (CAP_R | R) (CAP_A | A) (CAP_P | P) (CAP_H | H) (CAP_Y | Y));
geometryprefix  : ((CAP_G | G) (CAP_E | E) (CAP_O | O) (CAP_M | M) (CAP_E | E) (CAP_T | T) (CAP_R | R) (CAP_Y | Y));

//------------------------------------------------------------------------------
// 9. Punctuation
//------------------------------------------------------------------------------

rws : ( sp | htab | (PERCENT TWO ZERO) | (PERCENT ZERO NINE) )+;  // "required" whitespace
bws :  ( sp | htab | (PERCENT TWO ZERO) | (PERCENT ZERO NINE) )*;  // "bad" whitespace

at     : AT | (PERCENT FOUR ZERO);
colon  : COLON | (PERCENT THREE (CAP_A | A));
comma  : COMMA | (PERCENT TWO (CAP_C | C));
eq     : EQUALS;
sign   : PLUS | (PERCENT TWO (CAP_B | B)) | DASH;
semi   : SEMICOLON | (PERCENT THREE (CAP_B | B));
star   : ASTERISK | (PERCENT TWO (CAP_A | A));
squote : APOSTROPHE | (PERCENT TWO SEVEN);

open  : LEFT_PAREN | (PERCENT TWO EIGHT);
close : RIGHT_PAREN | (PERCENT TWO NINE);


//------------------------------------------------------------------------------
// A. URI syntax [RFC3986]
//------------------------------------------------------------------------------

unreserved    : alpha | digit | DASH | PERIOD | UNDERSCORE | TILDE | rws;
other_delims   : EXCLAMATION | LEFT_PAREN | RIGHT_PAREN | ASTERISK | PLUS | COMMA | SEMICOLON;

pchar_no_squote       : unreserved | pct_encoded_no_squote | other_delims | DOLLAR | AMPERSAND | EQUALS | COLON | AT;
pct_encoded_no_squote : (PERCENT ( ZERO | ONE |   THREE | FOUR | FIVE | SIX | EIGHT | NINE | a_to_f ) hexdig)
                      | (PERCENT TWO ( ZERO | ONE | TWO | THREE | FOUR | FIVE | SIX |   EIGHT | NINE | a_to_f ));

qchar_unescaped       : unreserved | pct_encoded_unescaped | other_delims | COLON | AT | SLASH | QUESTION | DOLLAR | APOSTROPHE | EQUALS;
pct_encoded_unescaped : (PERCENT ( ZERO | ONE |   THREE | FOUR |   SIX | SEVEN | EIGHT | NINE | a_to_f ) hexdig)
                      | (PERCENT TWO ( ZERO | ONE |   THREE | FOUR | FIVE | SIX | SEVEN | EIGHT | NINE | a_to_f ))
                      | (PERCENT FIVE ( digit | (CAP_A | A) | (CAP_B | B) |   (CAP_D | D) | (CAP_E | E) | (CAP_F | F) ));

//------------------------------------------------------------------------------
// C. ABNF core definitions [RFC5234]
//------------------------------------------------------------------------------

alpha  : (CAP_A | CAP_B | CAP_C | CAP_D | CAP_E | CAP_F | CAP_G | CAP_H | CAP_I | CAP_J | CAP_K | CAP_L | CAP_M | CAP_N | CAP_O | CAP_P | CAP_Q | CAP_R | CAP_S | CAP_T | CAP_U | CAP_V | CAP_W | CAP_X | CAP_Y | CAP_Z) | (A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z);
digit  : (ZERO | ONE | TWO | THREE | FOUR | FIVE | SIX | SEVEN | EIGHT | NINE);
hexdig : digit | a_to_f;
a_to_f : (CAP_A | A) | (CAP_B | B) | (CAP_C | C) | (CAP_D | D) | (CAP_E | E) | (CAP_F | F);
dquote : QUOTE;
sp     : SPACE;
htab   : TAB;

//------------------------------------------------------------------------------
// End of odata-abnf-construction-rules
//------------------------------------------------------------------------------


////////////////////////////////////////////////////////////////////////////////////////////
// Lexer rules generated for each distinct character in original grammar
// Simplified character names based on Unicode (http://www.unicode.org/charts/PDF/U0000.pdf)
////////////////////////////////////////////////////////////////////////////////////////////

TAB : '\u0009';
SPACE : ' ';
EXCLAMATION : '!';
QUOTE : '"';
HASH : '#';
DOLLAR : '$';
PERCENT : '%';
AMPERSAND : '&';
APOSTROPHE : '\'';
LEFT_PAREN : '(';
RIGHT_PAREN : ')';
ASTERISK : '*';
PLUS : '+';
COMMA : ',';
DASH : '-';
PERIOD : '.';
SLASH : '/';
ZERO : '0';
ONE : '1';
TWO : '2';
THREE : '3';
FOUR : '4';
FIVE : '5';
SIX : '6';
SEVEN : '7';
EIGHT : '8';
NINE : '9';
COLON : ':';
SEMICOLON : ';';
LESS_THAN : '<';
EQUALS : '=';
GREATER_THAN : '>';
QUESTION : '?';
AT : '@';
CAP_A : 'A';
CAP_B : 'B';
CAP_C : 'C';
CAP_D : 'D';
CAP_E : 'E';
CAP_F : 'F';
CAP_G : 'G';
CAP_H : 'H';
CAP_I : 'I';
CAP_J : 'J';
CAP_K : 'K';
CAP_L : 'L';
CAP_M : 'M';
CAP_N : 'N';
CAP_O : 'O';
CAP_P : 'P';
CAP_Q : 'Q';
CAP_R : 'R';
CAP_S : 'S';
CAP_T : 'T';
CAP_U : 'U';
CAP_V : 'V';
CAP_W : 'W';
CAP_X : 'X';
CAP_Y : 'Y';
CAP_Z : 'Z';
LEFT_BRACE : '[';
BACKSLASH : '\\';
RIGHT_BRACE : ']';
CARAT : '^';
UNDERSCORE : '_';
ACCENT : '`';
A : 'a';
B : 'b';
C : 'c';
D : 'd';
E : 'e';
F : 'f';
G : 'g';
H : 'h';
I : 'i';
J : 'j';
K : 'k';
L : 'l';
M : 'm';
N : 'n';
O : 'o';
P : 'p';
Q : 'q';
R : 'r';
S : 's';
T : 't';
U : 'u';
V : 'v';
W : 'w';
X : 'x';
Y : 'y';
Z : 'z';
LEFT_CURLY_BRACE : '{';
PIPE : '|';
RIGHT_CURLY_BRACE : '}';
TILDE : '~';
U_0080 : '\u0080';
U_0081 : '\u0081';
U_0082 : '\u0082';
U_0083 : '\u0083';
U_0084 : '\u0084';
U_0085 : '\u0085';
U_0086 : '\u0086';
U_0087 : '\u0087';
U_0088 : '\u0088';
U_0089 : '\u0089';
U_008A : '\u008A';
U_008B : '\u008B';
U_008C : '\u008C';
U_008D : '\u008D';
U_008E : '\u008E';
U_008F : '\u008F';
U_0090 : '\u0090';
U_0091 : '\u0091';
U_0092 : '\u0092';
U_0093 : '\u0093';
U_0094 : '\u0094';
U_0095 : '\u0095';
U_0096 : '\u0096';
U_0097 : '\u0097';
U_0098 : '\u0098';
U_0099 : '\u0099';
U_009A : '\u009A';
U_009B : '\u009B';
U_009C : '\u009C';
U_009D : '\u009D';
U_009E : '\u009E';
U_009F : '\u009F';
U_00A0 : '\u00A0';
U_00A1 : '\u00A1';
U_00A2 : '\u00A2';
U_00A3 : '\u00A3';
U_00A4 : '\u00A4';
U_00A5 : '\u00A5';
U_00A6 : '\u00A6';
U_00A7 : '\u00A7';
U_00A8 : '\u00A8';
U_00A9 : '\u00A9';
U_00AA : '\u00AA';
U_00AB : '\u00AB';
U_00AC : '\u00AC';
U_00AD : '\u00AD';
U_00AE : '\u00AE';
U_00AF : '\u00AF';
U_00B0 : '\u00B0';
U_00B1 : '\u00B1';
U_00B2 : '\u00B2';
U_00B3 : '\u00B3';
U_00B4 : '\u00B4';
U_00B5 : '\u00B5';
U_00B6 : '\u00B6';
U_00B7 : '\u00B7';
U_00B8 : '\u00B8';
U_00B9 : '\u00B9';
U_00BA : '\u00BA';
U_00BB : '\u00BB';
U_00BC : '\u00BC';
U_00BD : '\u00BD';
U_00BE : '\u00BE';
U_00BF : '\u00BF';
U_00C0 : '\u00C0';
U_00C1 : '\u00C1';
U_00C2 : '\u00C2';
U_00C3 : '\u00C3';
U_00C4 : '\u00C4';
U_00C5 : '\u00C5';
U_00C6 : '\u00C6';
U_00C7 : '\u00C7';
U_00C8 : '\u00C8';
U_00C9 : '\u00C9';
U_00CA : '\u00CA';
U_00CB : '\u00CB';
U_00CC : '\u00CC';
U_00CD : '\u00CD';
U_00CE : '\u00CE';
U_00CF : '\u00CF';
U_00D0 : '\u00D0';
U_00D1 : '\u00D1';
U_00D2 : '\u00D2';
U_00D3 : '\u00D3';
U_00D4 : '\u00D4';
U_00D5 : '\u00D5';
U_00D6 : '\u00D6';
U_00D7 : '\u00D7';
U_00D8 : '\u00D8';
U_00D9 : '\u00D9';
U_00DA : '\u00DA';
U_00DB : '\u00DB';
U_00DC : '\u00DC';
U_00DD : '\u00DD';
U_00DE : '\u00DE';
U_00DF : '\u00DF';
U_00E0 : '\u00E0';
U_00E1 : '\u00E1';
U_00E2 : '\u00E2';
U_00E3 : '\u00E3';
U_00E4 : '\u00E4';
U_00E5 : '\u00E5';
U_00E6 : '\u00E6';
U_00E7 : '\u00E7';
U_00E8 : '\u00E8';
U_00E9 : '\u00E9';
U_00EA : '\u00EA';
U_00EB : '\u00EB';
U_00EC : '\u00EC';
U_00ED : '\u00ED';
U_00EE : '\u00EE';
U_00EF : '\u00EF';
U_00F0 : '\u00F0';
U_00F1 : '\u00F1';
U_00F2 : '\u00F2';
U_00F3 : '\u00F3';
U_00F4 : '\u00F4';
U_00F5 : '\u00F5';
U_00F6 : '\u00F6';
U_00F7 : '\u00F7';
U_00F8 : '\u00F8';
U_00F9 : '\u00F9';
U_00FA : '\u00FA';
U_00FB : '\u00FB';
U_00FC : '\u00FC';
U_00FD : '\u00FD';
U_00FE : '\u00FE';
U_00FF : '\u00FF';
