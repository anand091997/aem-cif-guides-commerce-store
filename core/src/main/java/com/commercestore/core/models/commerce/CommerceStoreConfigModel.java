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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.models.common.SiteStructure;
import com.adobe.cq.commerce.core.components.services.ComponentsConfiguration;
import com.adobe.cq.commerce.graphql.client.HttpMethod;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

/**
 * Writes the {@code store-config} meta tag the CIF storefront reads.
 * The CIF page model cannot supply it here: its Core Components delegate recurses on this SDK.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class CommerceStoreConfigModel {

    private static final String STORE_CODE_PROPERTY = "magentoStore";
    private static final String GRAPHQL_ENDPOINT_PROPERTY = "magentoGraphqlEndpoint";
    private static final String ENABLE_CLIENTSIDE_PRICE_LOADING_PROPERTY = "enableClientSidePriceLoading";
    private static final String DEFAULT_GRAPHQL_ENDPOINT = "/api/graphql";

    @Self
    private SlingHttpServletRequest request;
    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private MagentoGraphqlClient magentoGraphqlClient;
    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private SiteStructure siteStructure;
    @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Page currentPage;
    @SlingObject
    private Resource resource;

    private String json = "{}";

    @PostConstruct
    void init() {
        Map<String, Object> config = new LinkedHashMap<>();
        String language = null;
        if (currentPage != null && currentPage.getLanguage(false) != null) {
            language = currentPage.getLanguage(false).toLanguageTag();
        }

        Resource configResource = resource;
        if (currentPage != null && currentPage.getContentResource() != null) {
            configResource = currentPage.getContentResource();
        }
        ComponentsConfiguration properties = configResource == null ? null : configResource.adaptTo(ComponentsConfiguration.class);
        if (properties != null) {
            String storeView = properties.get(STORE_CODE_PROPERTY, String.class);
            if (storeView != null && !storeView.isEmpty()) {
                config.put("storeView", storeView);
            }
            config.put("graphqlEndpoint", properties.get(GRAPHQL_ENDPOINT_PROPERTY, DEFAULT_GRAPHQL_ENDPOINT));
            config.put("enableClientSidePriceLoading",
                properties.get(ENABLE_CLIENTSIDE_PRICE_LOADING_PROPERTY, Boolean.FALSE));
            String locale = properties.get(JcrConstants.JCR_LANGUAGE, String.class);
            if (locale != null && !locale.isEmpty()) {
                language = locale.replace('_', '-').toLowerCase(Locale.ROOT);
            }
        } else {
            config.put("graphqlEndpoint", DEFAULT_GRAPHQL_ENDPOINT);
            config.put("enableClientSidePriceLoading", Boolean.FALSE);
        }

        applyClient(magentoGraphqlClient, config);
        if (language != null && !language.isEmpty()) {
            config.put("locale", language);
        }

        String storeRootUrl = storeRootUrl();
        if (storeRootUrl != null) {
            config.put("storeRootUrl", storeRootUrl);
        }

        json = toJson(config);
    }

    static void applyClient(MagentoGraphqlClient client, Map<String, Object> config) {
        HttpMethod method = HttpMethod.POST;
        Map<String, String[]> headers = Collections.emptyMap();
        if (client != null && client.getConfiguration() != null) {
            method = client.getConfiguration().httpMethod();
            if (client.getHttpHeaderMap() != null) {
                headers = client.getHttpHeaderMap();
            }
        }
        config.put("graphqlMethod", method == null ? HttpMethod.POST.name() : method.name());
        if (!headers.isEmpty()) {
            config.put("headers", headers);
        }
    }

    static String toJson(Map<String, Object> config) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append("\":");
            appendValue(builder, entry.getValue());
        }
        builder.append('}');
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendValue(StringBuilder builder, Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            builder.append(value);
            return;
        }
        if (value instanceof Map) {
            builder.append('{');
            boolean first = true;
            for (Map.Entry<String, String[]> entry : ((Map<String, String[]>) value).entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append('"').append(escape(entry.getKey())).append("\":");
                String[] values = entry.getValue();
                if (values == null) {
                    builder.append("null");
                } else if (values.length == 1) {
                    appendValue(builder, values[0]);
                } else {
                    builder.append('[');
                    for (int i = 0; i < values.length; i++) {
                        if (i > 0) {
                            builder.append(',');
                        }
                        appendValue(builder, values[i]);
                    }
                    builder.append(']');
                }
            }
            builder.append('}');
            return;
        }
        builder.append('"').append(escape(value == null ? "" : String.valueOf(value))).append('"');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String storeRootUrl() {
        if (siteStructure == null || request == null) {
            return null;
        }
        Page storeRootPage = siteStructure.getLandingPage();
        if (storeRootPage == null) {
            return null;
        }
        String unmappedPath = storeRootPage.getPath();
        String mappedPath = request.getResourceResolver().map(request, unmappedPath);
        if (mappedPath == null || !mappedPath.startsWith("/")) {
            mappedPath = unmappedPath;
        }
        return mappedPath + ".html";
    }

    public String getJson() {
        return json;
    }
}
