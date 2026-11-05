/*******************************************************************************
 *
 *    Copyright 2026 Adobe. All rights reserved.
 *    This file is licensed to you under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License. You may obtain a copy
 *    of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software distributed under
 *    the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 *    OF ANY KIND, either express or implied. See the License for the specific language
 *    governing permissions and limitations under the License.
 *
 ******************************************************************************/
package com.commercestore.core.models.commerce;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.via.ResourceSuperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.magento.graphql.CategoryTree;
import com.adobe.cq.wcm.core.components.commons.link.Link;
import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.Navigation;
import com.adobe.cq.wcm.core.components.models.NavigationItem;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.designer.Style;

/**
 * Commerce Store Navigation Sling Model extending CIF Navigation to filter out unwanted categories.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { Navigation.class, Component.class },
    resourceType = CommerceStoreNavigationImpl.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CommerceStoreNavigationImpl implements Navigation, Component {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceStoreNavigationImpl.class);

    protected static final String RESOURCE_TYPE = "commerce-store/components/commerce/navigation";
    protected static final String PN_EXCLUDED_CATEGORIES = "excludedCategories";

    private static final Set<String> DEFAULT_EXCLUSIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("183", "MTgz", "gift-cards", "Gift Cards"))
    );

    @Self
    @Via(type = ResourceSuperType.class)
    private Navigation navigation;

    @Self
    private SlingHttpServletRequest request;

    @ScriptVariable
    private Style currentStyle;

    private Set<String> excludedCategories;

    @PostConstruct
    public void initModel() {
        Set<String> exclusions = new HashSet<>();

        // 1. Read from component resource properties
        if (request != null && request.getResource() != null) {
            ValueMap properties = request.getResource().getValueMap();
            addExclusionsFromProperty(properties.get(PN_EXCLUDED_CATEGORIES), exclusions);
        }

        // 2. Read from policy (currentStyle) if not found in component
        if (exclusions.isEmpty() && currentStyle != null) {
            addExclusionsFromProperty(currentStyle.get(PN_EXCLUDED_CATEGORIES), exclusions);
        }

        // 3. Fallback to default exclusions (e.g. 183 / Gift Cards)
        if (exclusions.isEmpty()) {
            exclusions.addAll(DEFAULT_EXCLUSIONS);
        }

        this.excludedCategories = Collections.unmodifiableSet(exclusions);
        LOGGER.debug("CommerceStoreNavigationImpl initialized with excluded categories: {}", this.excludedCategories);
    }

    private void addExclusionsFromProperty(Object propValue, Set<String> exclusions) {
        if (propValue instanceof String[]) {
            for (String item : (String[]) propValue) {
                if (StringUtils.isNotBlank(item)) {
                    exclusions.add(item.trim());
                }
            }
        } else if (propValue instanceof String) {
            String str = (String) propValue;
            for (String item : str.split("[,;]")) {
                if (StringUtils.isNotBlank(item)) {
                    exclusions.add(item.trim());
                }
            }
        }
    }

    @Override
    public List<NavigationItem> getItems() {
        if (navigation == null) {
            return Collections.emptyList();
        }
        List<NavigationItem> originalItems = navigation.getItems();
        if (originalItems == null || originalItems.isEmpty()) {
            return Collections.emptyList();
        }
        return filterItems(originalItems);
    }

    private List<NavigationItem> filterItems(List<NavigationItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        List<NavigationItem> filtered = new ArrayList<>();
        for (NavigationItem item : items) {
            if (isExcluded(item)) {
                LOGGER.info("Filtered out category item from navigation: title='{}', url='{}'", item.getTitle(), item.getURL());
                continue;
            }
            List<NavigationItem> childItems = item.getChildren();
            List<NavigationItem> filteredChildren = (childItems != null && !childItems.isEmpty())
                ? filterItems(childItems)
                : Collections.emptyList();

            filtered.add(new FilteredNavigationItem(item, filteredChildren));
        }
        return filtered;
    }

    private boolean isExcluded(NavigationItem item) {
        if (item == null || excludedCategories == null || excludedCategories.isEmpty()) {
            return false;
        }

        String title = item.getTitle();
        String url = item.getURL();
        String name = item.getName();
        String categoryId = extractCategoryId(item);
        String categoryUid = extractCategoryUid(item);

        for (String excluded : excludedCategories) {
            if (StringUtils.isBlank(excluded)) {
                continue;
            }
            String clean = excluded.trim();

            // Match Category ID (e.g. "183")
            if (categoryId != null && categoryId.equalsIgnoreCase(clean)) {
                return true;
            }

            // Match Category UID (e.g. "MTgz")
            if (categoryUid != null && categoryUid.equalsIgnoreCase(clean)) {
                return true;
            }

            // Match Title (e.g. "Gift Cards")
            if (title != null && title.equalsIgnoreCase(clean)) {
                return true;
            }

            // Match Name (e.g. "gift-cards")
            if (name != null && name.equalsIgnoreCase(clean)) {
                return true;
            }

            // Match URL slug / path (e.g. gift-cards, gift-cards.html)
            if (url != null) {
                String lowerUrl = url.toLowerCase();
                String lowerClean = clean.toLowerCase();
                if (lowerUrl.endsWith("/" + lowerClean + ".html")
                        || lowerUrl.contains("/" + lowerClean + "/")
                        || lowerUrl.endsWith("/" + lowerClean)
                        || lowerUrl.contains("/" + lowerClean + ".html")) {
                    return true;
                }
            }

            // If clean is numeric (e.g. "183"), check against base64 encoded UID (e.g. "MTgz")
            if (clean.matches("\\d+")) {
                try {
                    String base64 = Base64.getEncoder().encodeToString(clean.getBytes(StandardCharsets.UTF_8));
                    if (base64.equals(categoryUid)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    private String extractCategoryId(NavigationItem item) {
        CategoryTree cat = extractCategoryTree(item);
        if (cat != null && cat.getId() != null) {
            return String.valueOf(cat.getId());
        }
        return null;
    }

    private String extractCategoryUid(NavigationItem item) {
        CategoryTree cat = extractCategoryTree(item);
        if (cat != null && cat.getUid() != null) {
            return cat.getUid().toString();
        }
        return null;
    }

    private CategoryTree extractCategoryTree(NavigationItem item) {
        if (item == null) {
            return null;
        }
        try {
            Class<?> clazz = item.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(item);
                    if (val instanceof CategoryTree) {
                        return (CategoryTree) val;
                    }
                    if (val != null && val.getClass().getName().contains("NavigationItem")) {
                        Class<?> innerClazz = val.getClass();
                        while (innerClazz != null && innerClazz != Object.class) {
                            for (Field innerField : innerClazz.getDeclaredFields()) {
                                innerField.setAccessible(true);
                                Object innerVal = innerField.get(val);
                                if (innerVal instanceof CategoryTree) {
                                    return (CategoryTree) innerVal;
                                }
                            }
                            innerClazz = innerClazz.getSuperclass();
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable t) {
            LOGGER.trace("Could not extract CategoryTree from NavigationItem", t);
        }
        return null;
    }

    @Override
    public String getId() {
        return navigation != null ? navigation.getId() : null;
    }

    @Override
    public ComponentData getData() {
        return navigation != null ? navigation.getData() : null;
    }

    @Override
    public String getAccessibilityLabel() {
        return navigation != null ? navigation.getAccessibilityLabel() : null;
    }

    @Override
    public String getExportedType() {
        return navigation != null ? navigation.getExportedType() : RESOURCE_TYPE;
    }

    @Override
    public String getAppliedCssClasses() {
        return navigation != null ? navigation.getAppliedCssClasses() : null;
    }

    public Set<String> getExcludedCategories() {
        return excludedCategories;
    }

    private static class FilteredNavigationItem implements NavigationItem {
        private final NavigationItem delegate;
        private final List<NavigationItem> children;

        public FilteredNavigationItem(NavigationItem delegate, List<NavigationItem> children) {
            this.delegate = delegate;
            this.children = children != null ? children : Collections.emptyList();
        }

        @Override
        public List<NavigationItem> getChildren() {
            return children;
        }

        @Override
        public Page getPage() {
            return delegate.getPage();
        }

        @Override
        public boolean isActive() {
            return delegate.isActive();
        }

        @Override
        public boolean isCurrent() {
            return delegate.isCurrent();
        }

        @Override
        public int getLevel() {
            return delegate.getLevel();
        }

        @Override
        public Link getLink() {
            return delegate.getLink();
        }

        @Override
        public String getURL() {
            return delegate.getURL();
        }

        @Override
        public String getTitle() {
            return delegate.getTitle();
        }

        @Override
        public String getDescription() {
            return delegate.getDescription();
        }

        @Override
        public Calendar getLastModified() {
            return delegate.getLastModified();
        }

        @Override
        public String getPath() {
            return delegate.getPath();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Resource getTeaserResource() {
            return delegate.getTeaserResource();
        }

        @Override
        public String getId() {
            return delegate.getId();
        }

        @Override
        public ComponentData getData() {
            return delegate.getData();
        }

        @Override
        public String getAppliedCssClasses() {
            return delegate.getAppliedCssClasses();
        }

        @Override
        public String getExportedType() {
            return delegate.getExportedType();
        }
    }
}

