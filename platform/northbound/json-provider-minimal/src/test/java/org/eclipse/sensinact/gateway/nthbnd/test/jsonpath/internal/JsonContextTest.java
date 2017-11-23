package org.eclipse.sensinact.gateway.nthbnd.test.jsonpath.internal;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import org.assertj.core.api.Assertions;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.builder.JsonOrgNodeBuilder;
import org.eclipse.sensinact.gateway.nthbnd.test.jsonpath.BaseTestConfiguration;
import org.eclipse.sensinact.gateway.nthbnd.test.jsonpath.BaseTestJson;
import org.junit.Test;

import java.util.List;

public class JsonContextTest extends BaseTestConfiguration {

	JsonOrgNodeBuilder builder = new JsonOrgNodeBuilder();
	
	@Test
    public void cached_path_with_predicates() {

        Filter feq = Filter.filter(builder.where("category").eq("reference"));
        Filter fne = Filter.filter(builder.where("category").ne("reference"));
        
        DocumentContext JsonDoc = JsonPath.parse(BaseTestJson.JSON_DOCUMENT);

        List<String> eq = JsonDoc.read("$.store.book[?].category", feq);
        List<String> ne = JsonDoc.read("$.store.book[?].category", fne);

        Assertions.assertThat(eq).contains("reference");
        Assertions.assertThat(ne).doesNotContain("reference");
    }

}
