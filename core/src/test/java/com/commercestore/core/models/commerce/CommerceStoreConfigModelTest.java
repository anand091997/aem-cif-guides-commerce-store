/*
 *  Copyright 2026 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.commercestore.core.models.commerce;

import java.util.Collections;
import java.util.Map;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.models.common.SiteStructure;
import com.adobe.cq.commerce.core.components.services.ComponentsConfiguration;
import com.adobe.cq.commerce.graphql.client.HttpMethod;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AemContextExtension.class)
class CommerceStoreConfigModelTest {

    private final AemContext context = new AemContext();

    @Test
    void exportsConfiguredStore() {
        context.addModelsForClasses(CommerceStoreConfigModel.class);
        Page page = context.create().page("/content/commerce-store/language-masters/en");
        context.currentPage(page);
        context.currentResource(page.getContentResource());

        ValueMap values = new ValueMapDecorator(Map.of(
            "magentoStore", "venia",
            "magentoGraphqlEndpoint", "/api/graphql",
            "enableClientSidePriceLoading", Boolean.TRUE,
            "jcr:language", "en_US"));
        context.registerAdapter(Resource.class, ComponentsConfiguration.class,
            (java.util.function.Function<Resource, ComponentsConfiguration>) resource -> new ComponentsConfiguration(values));

        SiteStructure siteStructure = mock(SiteStructure.class);
        when(siteStructure.getLandingPage()).thenReturn(page);
        context.registerAdapter(SlingHttpServletRequest.class, SiteStructure.class, siteStructure);

        MagentoGraphqlClient client = mock(MagentoGraphqlClient.class);
        com.adobe.cq.commerce.graphql.client.GraphqlClientConfiguration configuration =
            mock(com.adobe.cq.commerce.graphql.client.GraphqlClientConfiguration.class);
        when(configuration.httpMethod()).thenReturn(HttpMethod.GET);
        when(client.getConfiguration()).thenReturn(configuration);
        when(client.getHttpHeaderMap()).thenReturn(Map.of("Store", new String[] {"venia"}));
        context.registerAdapter(SlingHttpServletRequest.class, MagentoGraphqlClient.class, client);

        CommerceStoreConfigModel model = context.request().adaptTo(CommerceStoreConfigModel.class);

        assertNotNull(model);
        String json = model.getJson();
        assertTrue(json.contains("\"storeView\":\"venia\""));
        assertTrue(json.contains("\"graphqlEndpoint\":\"/api/graphql\""));
        assertTrue(json.contains("\"graphqlMethod\":\"POST\""));
        assertTrue(json.contains("\"locale\":\"en-us\""));
        assertTrue(json.contains("\"enableClientSidePriceLoading\":true"));
        assertTrue(json.contains("/content/commerce-store/language-masters/en.html"));
    }

    @Test
    void serializesGraphqlClientHeaders() {
        MagentoGraphqlClient client = mock(MagentoGraphqlClient.class);
        com.adobe.cq.commerce.graphql.client.GraphqlClientConfiguration configuration =
            mock(com.adobe.cq.commerce.graphql.client.GraphqlClientConfiguration.class);
        when(configuration.httpMethod()).thenReturn(HttpMethod.GET);
        when(client.getConfiguration()).thenReturn(configuration);
        when(client.getHttpHeaderMap()).thenReturn(Map.of(
            "Store", new String[] {"venia"},
            "X-Multi", new String[] {"a", "b"}));

        Map<String, Object> config = new java.util.LinkedHashMap<>();
        CommerceStoreConfigModel.applyClient(client, config);
        String json = CommerceStoreConfigModel.toJson(config);

        assertTrue(json.contains("\"graphqlMethod\":\"GET\""));
        assertTrue(json.contains("\"Store\":\"venia\""));
        assertTrue(json.contains("\"X-Multi\":[\"a\",\"b\"]"));
    }

    @Test
    void exportsDefaultsWhenCommerceConfigIsMissing() {
        context.addModelsForClasses(CommerceStoreConfigModel.class);
        Page page = context.create().page("/content/commerce-store/us/en");
        context.currentPage(page);
        context.currentResource(page.getContentResource());

        SiteStructure siteStructure = mock(SiteStructure.class);
        when(siteStructure.getLandingPage()).thenReturn(null);
        context.registerAdapter(SlingHttpServletRequest.class, SiteStructure.class, siteStructure);
        context.registerAdapter(SlingHttpServletRequest.class, MagentoGraphqlClient.class,
            (java.util.function.Function<SlingHttpServletRequest, MagentoGraphqlClient>) request -> {
                MagentoGraphqlClient client = mock(MagentoGraphqlClient.class);
                when(client.getConfiguration()).thenReturn(null);
                when(client.getHttpHeaderMap()).thenReturn(Collections.emptyMap());
                return client;
            });

        CommerceStoreConfigModel model = context.request().adaptTo(CommerceStoreConfigModel.class);

        assertNotNull(model);
        assertTrue(model.getJson().contains("\"graphqlEndpoint\":\"/api/graphql\""));
        assertTrue(model.getJson().contains("\"graphqlMethod\":\"POST\""));
        assertFalse(model.getJson().contains("storeRootUrl"));
        assertFalse(model.getJson().contains("headers"));
    }
}
