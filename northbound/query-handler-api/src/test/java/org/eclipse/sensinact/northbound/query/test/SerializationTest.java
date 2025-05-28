/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.query.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonType;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EQueryType;
import org.eclipse.sensinact.northbound.query.api.EReadWriteMode;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryActDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryDescribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryGetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.WrappedAccessMethodCallParametersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.AccessMethodDTO;
import org.eclipse.sensinact.northbound.query.dto.result.AccessMethodParameterDTO;
import org.eclipse.sensinact.northbound.query.dto.result.CompleteProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.query.dto.result.MetadataDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeResourceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeServiceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseSetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultActDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultDescribeProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ShortResourceDescriptionDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Tests DTOs serialization
 */
public class SerializationTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true)
            .build();

    /**
     * Tests Act DTOs (de-)serialization
     */
    @SuppressWarnings("unchecked")
    @Test
    void testAct() throws Exception {
        // Query
        final QueryActDTO query = new QueryActDTO();
        assertEquals(EQueryType.ACT, query.operation);
        query.requestId = "abc";
        query.uri = new SensinactPath("provider", "svc", "rc");
        query.parameters = Map.of("arg1", 42, "arg2", "abc");

        // Convert back from JSON
        final QueryActDTO parsedQuery = mapper.readValue(mapper.writeValueAsString(query), QueryActDTO.class);
        assertEquals(query.operation, parsedQuery.operation);
        assertEquals(query.requestId, parsedQuery.requestId);
        assertEquals(query.uri, parsedQuery.uri);
        assertEquals(query.parameters, parsedQuery.parameters);

        // Same with old style call
        final Map<String, Object> rawQuery = mapper.convertValue(query, Map.class);
        final AccessMethodCallParameterDTO arg1 = new AccessMethodCallParameterDTO();
        arg1.name = "arg1";
        arg1.type = Integer.class.getName();
        arg1.value = 42;
        final AccessMethodCallParameterDTO arg2 = new AccessMethodCallParameterDTO();
        arg2.name = "arg2";
        arg2.type = String.class.getName();
        arg2.value = "abc";
        rawQuery.put("parameters", List.of(arg1, arg2));

        final QueryActDTO parsedRawQuery = mapper.readValue(mapper.writeValueAsString(rawQuery), QueryActDTO.class);
        assertEquals(query.operation, parsedRawQuery.operation);
        assertEquals(query.requestId, parsedRawQuery.requestId);
        assertEquals(query.uri, parsedRawQuery.uri);
        assertEquals(query.parameters, parsedRawQuery.parameters);

        // Result
        final ResultActDTO result = new ResultActDTO();
        assertEquals(EResultType.ACT_RESPONSE, result.type);
        result.statusCode = 200;
        result.error = "abc";
        result.requestId = query.requestId;
        result.uri = query.uri.toUri();
        result.response = 42;

        final ResultActDTO parsedResult = mapper.readValue(mapper.writeValueAsString(result), ResultActDTO.class);
        assertEquals(result.type, parsedResult.type);
        assertEquals(result.statusCode, parsedResult.statusCode);
        assertEquals(result.error, parsedResult.error);
        assertEquals(result.requestId, parsedResult.requestId);
        assertEquals(result.uri, parsedResult.uri);
        assertEquals(result.response, parsedResult.response);
    }

    /**
     * Tests Describe DTOs (de-)serialization for a resource
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDescribeResource() throws Exception {
        // Query
        final QueryDescribeDTO query = new QueryDescribeDTO();
        assertEquals(EQueryType.DESCRIBE, query.operation);
        query.requestId = "abc";
        query.filter = "(PROVIDER=*)";
        query.filterLanguage = "ldap";
        query.uri = new SensinactPath("provider", "svc", "rc");
        assertTrue(query.uri.targetsSpecificResource());

        // Convert back from JSON
        QueryDescribeDTO parsedQuery = mapper.readValue(mapper.writeValueAsString(query), QueryDescribeDTO.class);
        assertEquals(query.operation, parsedQuery.operation);
        assertEquals(query.requestId, parsedQuery.requestId);
        assertEquals(query.filter, parsedQuery.filter);
        assertEquals(query.filterLanguage, parsedQuery.filterLanguage);
        assertEquals(query.uri, parsedQuery.uri);

        // Resource description
        final TypedResponse<ResponseDescribeResourceDTO> rcDescribe = new TypedResponse<>(
                EResultType.DESCRIBE_RESOURCE);
        assertEquals(EResultType.DESCRIBE_RESOURCE, rcDescribe.type);
        rcDescribe.statusCode = 200;
        rcDescribe.error = "abc";
        rcDescribe.requestId = query.requestId;
        rcDescribe.uri = query.uri.toUri();
        rcDescribe.response = new ResponseDescribeResourceDTO();
        rcDescribe.response.name = "rc";
        rcDescribe.response.type = ResourceType.PROPERTY;

        final MetadataDTO metadata = new MetadataDTO();
        metadata.name = "meta";
        metadata.type = Integer.class.getName();
        metadata.value = 42;
        rcDescribe.response.attributes = List.of(metadata);

        final AccessMethodDTO method = new AccessMethodDTO();
        method.name = "ACT";

        final AccessMethodParameterDTO param = new AccessMethodParameterDTO();
        param.name = "name";
        param.constraints = new String[] { "constraint" };
        param.fixed = false;
        param.type = String.class.getName();
        method.parameters = List.of(param);
        rcDescribe.response.accessMethods = List.of(method);

        final TypedResponse<ResponseDescribeResourceDTO> parsedResult = (TypedResponse<ResponseDescribeResourceDTO>) mapper
                .readValue(mapper.writeValueAsString(rcDescribe), TypedResponse.class);
        assertEquals(rcDescribe.type, parsedResult.type);
        assertEquals(rcDescribe.statusCode, parsedResult.statusCode);
        assertEquals(rcDescribe.error, parsedResult.error);
        assertEquals(rcDescribe.requestId, parsedResult.requestId);
        assertEquals(rcDescribe.uri, parsedResult.uri);
        assertEquals(rcDescribe.response.name, parsedResult.response.name);
        assertEquals(rcDescribe.response.type, parsedResult.response.type);

        assertEquals(rcDescribe.response.attributes.size(), parsedResult.response.attributes.size());
        final MetadataDTO parsedMetadata = parsedResult.response.attributes.get(0);
        assertEquals(metadata.name, parsedMetadata.name);
        assertEquals(metadata.type, parsedMetadata.type);
        assertEquals(metadata.value, parsedMetadata.value);

        assertEquals(rcDescribe.response.accessMethods.size(), parsedResult.response.accessMethods.size());
        final AccessMethodDTO parsedMethod = parsedResult.response.accessMethods.get(0);
        assertEquals(method.name, parsedMethod.name);

        assertEquals(method.parameters.size(), parsedMethod.parameters.size());
        final AccessMethodParameterDTO parsedParam = parsedMethod.parameters.get(0);
        assertEquals(param.name, parsedParam.name);
        assertArrayEquals(param.constraints, parsedParam.constraints);
        assertEquals(param.fixed, parsedParam.fixed);
        assertEquals(param.type, parsedParam.type);
    }

    /**
     * Tests Describe DTOs (de-)serialization for a service
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDescribeService() throws Exception {
        // Query
        final QueryDescribeDTO query = new QueryDescribeDTO();
        assertEquals(EQueryType.DESCRIBE, query.operation);
        query.requestId = "abc";
        query.filter = "(PROVIDER=*)";
        query.filterLanguage = "ldap";
        query.uri = new SensinactPath("provider", "svc");
        assertTrue(query.uri.targetsSpecificService());

        // Service description
        final TypedResponse<ResponseDescribeServiceDTO> svcDescribe = new TypedResponse<>(EResultType.DESCRIBE_SERVICE);
        assertEquals(EResultType.DESCRIBE_SERVICE, svcDescribe.type);
        svcDescribe.statusCode = 200;
        svcDescribe.error = "abc";
        svcDescribe.requestId = query.requestId;
        svcDescribe.uri = query.uri.toUri();
        svcDescribe.response = new ResponseDescribeServiceDTO();
        svcDescribe.response.name = "svc";

        final ShortResourceDescriptionDTO rcDesc = new ShortResourceDescriptionDTO();
        rcDesc.name = "rc";
        rcDesc.rws = EReadWriteMode.RW;
        rcDesc.type = ResourceType.PROPERTY;
        svcDescribe.response.resources = List.of(rcDesc);

        final TypedResponse<ResponseDescribeServiceDTO> parsedResult = (TypedResponse<ResponseDescribeServiceDTO>) mapper
                .readValue(mapper.writeValueAsString(svcDescribe), TypedResponse.class);
        assertEquals(svcDescribe.type, parsedResult.type);
        assertEquals(svcDescribe.statusCode, parsedResult.statusCode);
        assertEquals(svcDescribe.error, parsedResult.error);
        assertEquals(svcDescribe.requestId, parsedResult.requestId);
        assertEquals(svcDescribe.uri, parsedResult.uri);
        assertEquals(svcDescribe.response.name, parsedResult.response.name);

        assertEquals(svcDescribe.response.resources.size(), parsedResult.response.resources.size());
        final ShortResourceDescriptionDTO parsedRc = parsedResult.response.resources.get(0);
        assertEquals(rcDesc.name, parsedRc.name);
        assertEquals(rcDesc.rws, parsedRc.rws);
        assertEquals(rcDesc.type, parsedRc.type);
    }

    /**
     * Tests Describe DTOs (de-)serialization for a provider
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDescribeProvider() throws Exception {
        // Query
        final QueryDescribeDTO query = new QueryDescribeDTO();
        assertEquals(EQueryType.DESCRIBE, query.operation);
        query.requestId = "abc";
        query.filter = "(PROVIDER=*)";
        query.filterLanguage = "ldap";
        query.uri = new SensinactPath("provider");
        assertTrue(query.uri.targetsSpecificProvider());

        // Service description
        final TypedResponse<ResponseDescribeProviderDTO> provDescribe = new TypedResponse<>(
                EResultType.DESCRIBE_PROVIDER);
        assertEquals(EResultType.DESCRIBE_PROVIDER, provDescribe.type);
        provDescribe.statusCode = 200;
        provDescribe.error = "abc";
        provDescribe.requestId = query.requestId;
        provDescribe.uri = query.uri.toUri();
        provDescribe.response = new ResponseDescribeProviderDTO();
        provDescribe.response.name = "provider";
        provDescribe.response.services = List.of("admin", "svc");

        final TypedResponse<ResponseDescribeProviderDTO> parsedResult = (TypedResponse<ResponseDescribeProviderDTO>) mapper
                .readValue(mapper.writeValueAsString(provDescribe), TypedResponse.class);
        assertEquals(provDescribe.type, parsedResult.type);
        assertEquals(provDescribe.statusCode, parsedResult.statusCode);
        assertEquals(provDescribe.error, parsedResult.error);
        assertEquals(provDescribe.requestId, parsedResult.requestId);
        assertEquals(provDescribe.uri, parsedResult.uri);
        assertEquals(provDescribe.response.name, parsedResult.response.name);
        assertEquals(provDescribe.response.services, parsedResult.response.services);
    }

    /**
     * Tests Describe DTOs (de-)serialization for the list of providers
     */
    @Test
    void testDescribeProviders() throws Exception {
        // Query
        final QueryDescribeDTO query = new QueryDescribeDTO();
        assertEquals(EQueryType.DESCRIBE, query.operation);
        query.requestId = "abc";
        query.filter = "(PROVIDER=*)";
        query.filterLanguage = "ldap";
        query.uri = new SensinactPath();
        assertEquals("/", query.uri.toUri());

        // Service description
        final ResultDescribeProvidersDTO provDescribe = new ResultDescribeProvidersDTO();
        assertEquals(EResultType.COMPLETE_LIST, provDescribe.type);
        provDescribe.statusCode = 200;
        provDescribe.error = "abc";
        provDescribe.requestId = query.requestId;
        provDescribe.uri = query.uri.toUri();

        final CompleteProviderDescriptionDTO provider = new CompleteProviderDescriptionDTO();
        provider.name = "provider123";
        provider.icon = "icon123";

        final Point location = new Point();
        location.coordinates = new Coordinates();
        location.coordinates.latitude = 45;
        location.coordinates.longitude = 5;
        provider.location = location;

        final ResponseDescribeServiceDTO admin = new ResponseDescribeServiceDTO();
        admin.name = "admin";
        final ShortResourceDescriptionDTO locationRc = new ShortResourceDescriptionDTO();
        locationRc.name = "location";
        locationRc.rws = EReadWriteMode.RO;
        locationRc.type = ResourceType.PROPERTY;

        admin.resources = List.of(locationRc);
        provider.services = List.of(admin);

        provDescribe.providers = List.of(provider);

        final ResultDescribeProvidersDTO parsedResult = mapper.readValue(mapper.writeValueAsString(provDescribe),
                ResultDescribeProvidersDTO.class);
        assertEquals(provDescribe.type, parsedResult.type);
        assertEquals(provDescribe.statusCode, parsedResult.statusCode);
        assertEquals(provDescribe.error, parsedResult.error);
        assertEquals(provDescribe.requestId, parsedResult.requestId);
        assertEquals(provDescribe.uri, parsedResult.uri);
        assertEquals(provDescribe.providers.size(), parsedResult.providers.size());

        final CompleteProviderDescriptionDTO parsedProvider = parsedResult.providers.get(0);
        assertEquals(provider.name, parsedProvider.name);
        assertEquals(provider.icon, parsedProvider.icon);
        assertEquals(GeoJsonType.Point, parsedProvider.location.type);
        final Point parsedLocation = (Point) parsedProvider.location;
        assertEquals(location.coordinates.latitude, parsedLocation.coordinates.latitude);
        assertEquals(location.coordinates.longitude, parsedLocation.coordinates.longitude);

        assertEquals(provider.services.size(), parsedProvider.services.size());
        final ResponseDescribeServiceDTO parsedService = parsedProvider.services.get(0);
        assertEquals(admin.name, parsedService.name);

        assertEquals(admin.resources.size(), parsedService.resources.size());
        final ShortResourceDescriptionDTO parsedRc = parsedService.resources.get(0);
        assertEquals(locationRc.name, parsedRc.name);
        assertEquals(locationRc.rws, parsedRc.rws);
        assertEquals(locationRc.type, parsedRc.type);
    }

    /**
     * Tests Get DTOs (de-)serialization
     */
    @SuppressWarnings("unchecked")
    @Test
    void testGet() throws Exception {
        // Query
        final QueryGetDTO query = new QueryGetDTO();
        assertEquals(EQueryType.GET, query.operation);
        query.requestId = "abc";
        query.uri = new SensinactPath("provider", "svc", "rc");

        // Convert back from JSON
        final QueryGetDTO parsedQuery = mapper.readValue(mapper.writeValueAsString(query), QueryGetDTO.class);
        assertEquals(query.operation, parsedQuery.operation);
        assertEquals(query.requestId, parsedQuery.requestId);
        assertEquals(query.uri, parsedQuery.uri);

        // Result
        final TypedResponse<ResponseGetDTO> getRc = new TypedResponse<>(EResultType.GET_RESPONSE);
        assertEquals(EResultType.GET_RESPONSE, getRc.type);
        getRc.statusCode = 200;
        getRc.error = "abc";
        getRc.requestId = query.requestId;
        getRc.uri = query.uri.toUri();
        getRc.response = new ResponseGetDTO();
        getRc.response.name = "rc";
        getRc.response.timestamp = Instant.now().toEpochMilli();
        getRc.response.type = Integer.class.getName();
        getRc.response.value = 42;

        final TypedResponse<ResponseGetDTO> parsedResult = (TypedResponse<ResponseGetDTO>) mapper
                .readValue(mapper.writeValueAsString(getRc), TypedResponse.class);
        assertEquals(getRc.type, parsedResult.type);
        assertEquals(getRc.statusCode, parsedResult.statusCode);
        assertEquals(getRc.error, parsedResult.error);
        assertEquals(getRc.requestId, parsedResult.requestId);
        assertEquals(getRc.uri, parsedResult.uri);
        assertEquals(getRc.response.name, parsedResult.response.name);
        assertEquals(getRc.response.timestamp, parsedResult.response.timestamp);
        assertEquals(getRc.response.type, parsedResult.response.type);
        assertEquals(getRc.response.value, parsedResult.response.value);
    }

    /**
     * Tests Set DTOs (de-)serialization
     */
    @SuppressWarnings("unchecked")
    @Test
    void testSet() throws Exception {
        // Query
        final QuerySetDTO query = new QuerySetDTO();
        assertEquals(EQueryType.SET, query.operation);
        query.requestId = "abc";
        query.uri = new SensinactPath("provider", "svc", "rc");
        query.value = 42;
        query.valueType = Integer.class.getName();

        // Convert back from JSON
        final QuerySetDTO parsedQuery = mapper.readValue(mapper.writeValueAsString(query), QuerySetDTO.class);
        assertEquals(query.operation, parsedQuery.operation);
        assertEquals(query.requestId, parsedQuery.requestId);
        assertEquals(query.uri, parsedQuery.uri);
        assertEquals(query.value, parsedQuery.value);
        assertEquals(query.valueType, parsedQuery.valueType);

        // Result
        final TypedResponse<ResponseSetDTO> setRc = new TypedResponse<>(EResultType.SET_RESPONSE);
        assertEquals(EResultType.SET_RESPONSE, setRc.type);
        setRc.statusCode = 200;
        setRc.error = "abc";
        setRc.requestId = query.requestId;
        setRc.uri = query.uri.toUri();
        setRc.response = new ResponseSetDTO();
        setRc.response.name = "rc";
        setRc.response.timestamp = Instant.now().toEpochMilli();
        setRc.response.type = Integer.class.getName();
        setRc.response.value = 42;

        final TypedResponse<ResponseGetDTO> parsedResult = (TypedResponse<ResponseGetDTO>) mapper
                .readValue(mapper.writeValueAsString(setRc), TypedResponse.class);
        assertEquals(setRc.type, parsedResult.type);
        assertEquals(setRc.statusCode, parsedResult.statusCode);
        assertEquals(setRc.error, parsedResult.error);
        assertEquals(setRc.requestId, parsedResult.requestId);
        assertEquals(setRc.uri, parsedResult.uri);
        assertEquals(setRc.response.name, parsedResult.response.name);
        assertEquals(setRc.response.timestamp, parsedResult.response.timestamp);
        assertEquals(setRc.response.type, parsedResult.response.type);
        assertEquals(setRc.response.value, parsedResult.response.value);
    }

    @Test
    void testWrappedAccessMethodCallParametesrDTO() throws JsonProcessingException {

        final AccessMethodCallParameterDTO arg1 = new AccessMethodCallParameterDTO();
        arg1.name = "arg1";
        arg1.type = Integer.class.getName();
        arg1.value = 42;
        final AccessMethodCallParameterDTO arg2 = new AccessMethodCallParameterDTO();
        arg2.name = "arg2";
        arg2.type = String.class.getName();
        arg2.value = "abc";

        List<AccessMethodCallParameterDTO> parameters = List.of(arg1, arg2);

        final WrappedAccessMethodCallParametersDTO dto = new WrappedAccessMethodCallParametersDTO();
        dto.parameters = parameters;

        String s = mapper.writeValueAsString(dto);

        WrappedAccessMethodCallParametersDTO read = mapper.readValue(s, WrappedAccessMethodCallParametersDTO.class);

        assertEquals(dto.parameters.get(0).name, read.parameters.get(0).name);

        s = mapper.writeValueAsString(parameters);
        WrappedAccessMethodCallParametersDTO read2 = mapper.readValue(s, WrappedAccessMethodCallParametersDTO.class);
        assertEquals(dto.parameters.get(0).name, read2.parameters.get(0).name);

    }

    @Test
    void testTypedResponseSerialization() throws JsonProcessingException {
        // Original version
        final TypedResponse<ResponseGetDTO> original = new TypedResponse<>(EResultType.GET_RESPONSE);
        original.statusCode = 218;
        original.uri = "a/b/c";
        original.response = new ResponseGetDTO();
        original.response.name = "c";
        original.response.type = Integer.class.getName();
        original.response.timestamp = Instant.now().toEpochMilli();
        original.response.value = 42;

        final String strJson = mapper.writeValueAsString(original);
        int idx = -1;
        int typeKeyOccurrences = -1;
        do {
            typeKeyOccurrences++;
            idx = strJson.indexOf("\"type\"", idx + 1);
        } while (idx != -1);

        // We should find "type" twice: root level type and response value type
        assertEquals(2, typeKeyOccurrences);

        // Parse it back
        final TypedResponse<?> parsed = mapper.readValue(strJson, TypedResponse.class);
        assertEquals(original.error, parsed.error);
        assertEquals(original.requestId, parsed.requestId);
        assertEquals(original.statusCode, parsed.statusCode);
        assertEquals(original.response.getClass(), parsed.response.getClass());
    }

    @Test
    void testProvidersList() throws JsonProcessingException {
        final ResultListProvidersDTO original = new ResultListProvidersDTO();
        original.statusCode = 200;
        original.uri = "/";
        original.providers = List.of("toto", "titi", "tutu");

        final String strJson = mapper.writeValueAsString(original);

        // Test direct conversion
        final ResultListProvidersDTO typedParsed = mapper.readValue(strJson, ResultListProvidersDTO.class);
        assertEquals(original.type, typedParsed.type);
        assertEquals(original.statusCode, typedParsed.statusCode);
        assertEquals(original.uri, typedParsed.uri);
        assertEquals(original.providers, typedParsed.providers);

        // Test via abstract
        final AbstractResultDTO abstractParsed = mapper.readValue(strJson, AbstractResultDTO.class);
        assertEquals(original.type, abstractParsed.type);
        assertEquals(original.statusCode, abstractParsed.statusCode);
        assertEquals(original.uri, abstractParsed.uri);
        assertEquals(original.providers, ((ResultListProvidersDTO) abstractParsed).providers);
    }

    @Test
    void testSubscribeRequestNoFilter() throws JsonProcessingException {
        String request =
                """
                {
                  "operation": "SUBSCRIBE",
                  "requestId": "1234",
                  "uri": "/fizz/buzz/fizzbuzz/meta"
                }
                """;
        QuerySubscribeDTO query = mapper.readValue(request, QuerySubscribeDTO.class);
        
        assertEquals(EQueryType.SUBSCRIBE, query.operation);
        assertEquals("1234", query.requestId);
        assertNull(query.filter);
        assertNotNull(query.uri);
        assertEquals("fizz", query.uri.provider);
        assertEquals("buzz", query.uri.service);
        assertEquals("fizzbuzz", query.uri.resource);
        assertEquals("meta", query.uri.metadata);
    }
    
    @Test
    void testSubscribeRequestLDAP() throws JsonProcessingException {
        String request =
                """
                {
                  "operation": "SUBSCRIBE",
                  "requestId": "1234",
                  "filter": "(foo.bar=foobar)"
                }
                """;
        QuerySubscribeDTO query = mapper.readValue(request, QuerySubscribeDTO.class);
        
        assertEquals(EQueryType.SUBSCRIBE, query.operation);
        assertEquals("1234", query.requestId);
        assertEquals("(foo.bar=foobar)", query.filter);
        assertNull(query.uri);
    }

    @Test
    void testSubscribeRequestResourceSelector() throws JsonProcessingException {
        String request =
                """
                {
                  "operation": "SUBSCRIBE",
                  "requestId": "1234",
                  "filterLanguage": "sensinact.resource.selector",
                  "filter": {
                    "provider": {
                      "value": "foo"
                    }
                  }
                }
                """;
        QuerySubscribeDTO query = mapper.readValue(request, QuerySubscribeDTO.class);
        
        assertEquals(EQueryType.SUBSCRIBE, query.operation);
        assertEquals("1234", query.requestId);
        assertEquals("""
                {"provider":{"value":"foo"}}""", query.filter);
        assertNull(query.uri);
    }

    @Test
    void testSubscribeRequestMultipleResourceSelector() throws JsonProcessingException {
        String request =
                """
                {
                  "operation": "SUBSCRIBE",
                  "requestId": "1234",
                  "filterLanguage": "sensinact.resource.selector",
                  "filter": [
                    {
                      "provider": {
                        "value": "foo"
                      }
                    },
                    {
                      "provider": {
                        "value": "bar"
                      }
                    }
                  ]
                }
                """;
        QuerySubscribeDTO query = mapper.readValue(request, QuerySubscribeDTO.class);
        
        assertEquals(EQueryType.SUBSCRIBE, query.operation);
        assertEquals("1234", query.requestId);
        assertEquals("""
                [{"provider":{"value":"foo"}},{"provider":{"value":"bar"}}]""", query.filter);
        assertNull(query.uri);
    }
}
