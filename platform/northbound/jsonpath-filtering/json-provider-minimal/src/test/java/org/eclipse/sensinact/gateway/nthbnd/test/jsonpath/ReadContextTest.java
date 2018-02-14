package org.eclipse.sensinact.gateway.nthbnd.test.jsonpath;

import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static com.jayway.jsonpath.JsonPath.using;

public class ReadContextTest extends BaseTestConfiguration {

    @Test
    public void json_can_be_fetched_as_string() throws JSONException {

        String expected = new JSONObject(
        	"{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"display-price\":8.95}"
        		).toString();

        String jsonString1 = using(JSON_ORG_CONFIGURATION).parse(BaseTestJson.JSON_BOOK_DOCUMENT, false).jsonString();
        
        Assertions.assertThat(jsonString1).isEqualTo(expected);
    }

}
