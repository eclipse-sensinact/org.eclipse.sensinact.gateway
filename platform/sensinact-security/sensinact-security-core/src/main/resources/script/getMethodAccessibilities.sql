/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 *    Christophe Munilla - initial API and implementation
 */
---------------------------------------------------------------------------
--                                 FUNCTIONS                             --
--				       method accessibility from object path			 --
--						      and user public key                        --
---------------------------------------------------------------------------
WITH RECURSIVE path_element (ITERATION, PATH, LAST, POSITION, ELEMENT, PARENT, OID, PATTERN) AS
(
SELECT 0, '#VAR#' as PATH, 0 as LAST, 1 as POSITION, '' as ELEMENT, NULL, 0, 0
UNION ALL 
SELECT  
path_element.ITERATION+1,
path_element.PATH, 
path_element.POSITION, 
CASE WHEN instr(substr(path_element.PATH, path_element.POSITION+1),'/')=0 
THEN  length(path_element.PATH)+1 
ELSE instr(substr(path_element.PATH, path_element.POSITION+1),'/')+path_element.POSITION 
END, 
CASE WHEN  instr(substr(path_element.PATH, path_element.POSITION+1),'/')=0 
THEN  substr(path_element.PATH,path_element.POSITION, length(path_element.PATH) +1-path_element.POSITION) 
ELSE substr(path_element.PATH,path_element.POSITION, instr(substr(path_element.PATH, path_element.POSITION+1),'/')) 
END AS MYELEMENT,
OBJECT.PARENT,
OBJECT.OID,
CASE WHEN path_element.PATTERN = 1 THEN 1 ELSE OBJECT.PATTERN END
FROM path_element, OBJECT
WHERE ((OBJECT.PATTERN = 1 AND substr(MYELEMENT,2) REGEXP OBJECT.NAME)
OR (OBJECT.PATTERN = 0 AND substr(MYELEMENT,2) = OBJECT.NAME))
AND path_element.OID = OBJECT.PARENT
),
maximum_iteration(ITERATION) AS
(
SELECT MAX(path_element.ITERATION) AS ITERATION FROM path_element
),
element_object( ITERATION,PATTERN, OID,BID,SAUTH,OPID,NAME, PARENT,PATH) AS
(
SELECT path_element.ITERATION AS ITERATION, path_element.PATTERN AS PATTERN, 
OBJECT.OID AS OID, OBJECT.BID AS BID, OBJECT.SAUTH AS SAUTH, 
OBJECT.OPID AS OPID, OBJECT.NAME AS NAME, OBJECT.PARENT AS PARENT, 
OBJECT.PATH AS PATH
FROM OBJECT , path_element , maximum_iteration
WHERE OBJECT.OID = path_element.OID
AND maximum_iteration.ITERATION = path_element.ITERATION
),
not_pattern_element_object (ITERATION,PATTERN,OID,BID,SAUTH,OPID,NAME,PARENT,PATH) AS
(
 SELECT * FROM element_object WHERE PATTERN = 0
),
not_pattern_exist(NO_PATTERN_EXIST) AS
(
 SELECT COUNT(not_pattern_element_object.OID) AS NO_PATTERN_EXIST FROM not_pattern_element_object
),
pattern_element_object(ITERATION,PATTERN,OID,BID,SAUTH,OPID,NAME,PARENT,PATH) AS
(
 SELECT element_object.ITERATION AS ITERATION, element_object.PATTERN AS PATTERN, 
element_object.OID AS OID, element_object.BID AS BID, element_object.SAUTH AS SAUTH, 
element_object.OPID AS OPID, element_object.NAME AS NAME, element_object.PARENT AS PARENT, 
element_object.PATH AS PATH FROM element_object, not_pattern_exist
 WHERE element_object.PATTERN = 1 AND not_pattern_exist.NO_PATTERN_EXIST = 0
),
result_object(OID, BID, SAUTH, OPID, NAME, PATTERN,  PARENT, PATH) AS
(
SELECT OID, BID, SAUTH, OPID, NAME, PATTERN,  PARENT, PATH 
FROM not_pattern_element_object
UNION ALL
SELECT OID, BID, SAUTH, OPID, NAME, PATTERN,  PARENT, PATH 
FROM pattern_element_object
),
user_level (UAID) AS 
(
SELECT UAID 
FROM USER_ACCESS_LEVEL, result_object
WHERE USER_ACCESS_LEVEL.SUPUBLIC_KEY = '#VAR#'
AND USER_ACCESS_LEVEL.OID = result_object.OID
)
SELECT METHOD_ACCESSIBILITY.* 
FROM METHOD_ACCESSIBILITY, result_object, user_level
WHERE METHOD_ACCESSIBILITY.UAID = user_level.UAID
AND METHOD_ACCESSIBILITY.OID = result_object.OID;