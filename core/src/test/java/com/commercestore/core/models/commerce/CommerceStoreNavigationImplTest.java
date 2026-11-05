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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adobe.cq.commerce.magento.graphql.CategoryTree;
import com.adobe.cq.wcm.core.components.models.Navigation;
import com.adobe.cq.wcm.core.components.models.NavigationItem;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;
import com.day.cq.wcm.api.designer.Style;

class CommerceStoreNavigationImplTest {

    private CommerceStoreNavigationImpl underTest;
    private Navigation mockNavigation;
    private SlingHttpServletRequest mockRequest;
    private Resource mockResource;
    private ValueMap mockValueMap;
    private Style mockStyle;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new CommerceStoreNavigationImpl();
        mockNavigation = mock(Navigation.class);
        mockRequest = mock(SlingHttpServletRequest.class);
        mockResource = mock(Resource.class);
        mockValueMap = mock(ValueMap.class);
        mockStyle = mock(Style.class);

        when(mockRequest.getResource()).thenReturn(mockResource);
        when(mockResource.getValueMap()).thenReturn(mockValueMap);

        FieldUtils.writeField(underTest, "navigation", mockNavigation, true);
        FieldUtils.writeField(underTest, "request", mockRequest, true);
        FieldUtils.writeField(underTest, "currentStyle", mockStyle, true);
    }

    @Test
    void testDefaultExclusionsFilterGiftCards() {
        // No property configured in ValueMap or Style -> default exclusions apply
        underTest.initModel();

        assertTrue(underTest.getExcludedCategories().contains("183"));
        assertTrue(underTest.getExcludedCategories().contains("gift-cards"));

        NavigationItem item1 = mock(NavigationItem.class);
        when(item1.getTitle()).thenReturn("Jewelry");
        when(item1.getURL()).thenReturn("/content/commerce-store/us/en/products/category-page.html/jewelry.html");
        when(item1.getName()).thenReturn("jewelry");

        NavigationItem item2 = mock(NavigationItem.class);
        when(item2.getTitle()).thenReturn("Gift Cards");
        when(item2.getURL()).thenReturn("/content/commerce-store/us/en/products/category-page.html/gift-cards.html");
        when(item2.getName()).thenReturn("gift-cards");

        when(mockNavigation.getItems()).thenReturn(Arrays.asList(item1, item2));

        List<NavigationItem> result = underTest.getItems();
        assertEquals(1, result.size());
        assertEquals("Jewelry", result.get(0).getTitle());
    }

    @Test
    void testCustomExclusionsFromProperty() {
        when(mockValueMap.get("excludedCategories")).thenReturn(new String[]{"jewelry", "beauty"});

        underTest.initModel();

        assertTrue(underTest.getExcludedCategories().contains("jewelry"));
        assertTrue(underTest.getExcludedCategories().contains("beauty"));
        assertFalse(underTest.getExcludedCategories().contains("183")); // custom overrides default

        NavigationItem item1 = mock(NavigationItem.class);
        when(item1.getTitle()).thenReturn("Jewelry");
        when(item1.getURL()).thenReturn("/content/commerce-store/us/en/products/category-page.html/jewelry.html");
        when(item1.getName()).thenReturn("jewelry");

        NavigationItem item2 = mock(NavigationItem.class);
        when(item2.getTitle()).thenReturn("Fashion");
        when(item2.getURL()).thenReturn("/content/commerce-store/us/en/products/category-page.html/fashion.html");
        when(item2.getName()).thenReturn("fashion");

        when(mockNavigation.getItems()).thenReturn(Arrays.asList(item1, item2));

        List<NavigationItem> result = underTest.getItems();
        assertEquals(1, result.size());
        assertEquals("Fashion", result.get(0).getTitle());
    }

    @Test
    void testFilterNestedSubcategories() {
        underTest.initModel();

        NavigationItem parent = mock(NavigationItem.class);
        when(parent.getTitle()).thenReturn("Live TV");
        when(parent.getURL()).thenReturn("/content/commerce-store/us/en/products/category-page.html/live-tv.html");
        when(parent.getName()).thenReturn("live-tv");

        NavigationItem sub1 = mock(NavigationItem.class);
        when(sub1.getTitle()).thenReturn("Recently Aired");
        when(sub1.getURL()).thenReturn("/content/commerce-store/us/en/products/category-page.html/live-tv/recently-aired.html");

        NavigationItem sub2 = mock(NavigationItem.class);
        when(sub2.getTitle()).thenReturn("Gift Cards");
        when(sub2.getURL()).thenReturn("/content/commerce-store/us/en/products/category-page.html/live-tv/gift-cards.html");

        when(parent.getChildren()).thenReturn(Arrays.asList(sub1, sub2));
        when(mockNavigation.getItems()).thenReturn(Collections.singletonList(parent));

        List<NavigationItem> result = underTest.getItems();
        assertEquals(1, result.size());
        NavigationItem filteredParent = result.get(0);
        assertEquals("Live TV", filteredParent.getTitle());
        assertEquals(1, filteredParent.getChildren().size());
        assertEquals("Recently Aired", filteredParent.getChildren().get(0).getTitle());
    }

    @Test
    void testDelegationMethods() {
        when(mockNavigation.getId()).thenReturn("nav-123");
        when(mockNavigation.getAccessibilityLabel()).thenReturn("Main Navigation");
        when(mockNavigation.getExportedType()).thenReturn("custom/navigation");
        when(mockNavigation.getAppliedCssClasses()).thenReturn("my-nav");
        ComponentData mockData = mock(ComponentData.class);
        when(mockNavigation.getData()).thenReturn(mockData);

        assertEquals("nav-123", underTest.getId());
        assertEquals("Main Navigation", underTest.getAccessibilityLabel());
        assertEquals("custom/navigation", underTest.getExportedType());
        assertEquals("my-nav", underTest.getAppliedCssClasses());
        assertEquals(mockData, underTest.getData());
    }
}

