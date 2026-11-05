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

import java.util.Collections;
import java.util.Locale;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.adobe.cq.commerce.core.components.models.productlist.CategoryRetriever;
import com.adobe.cq.commerce.core.components.models.productlist.ProductList;
import com.adobe.cq.commerce.magento.graphql.ProductInterfaceQuery;

import java.util.function.Consumer;

class CommerceStoreProductListImplTest {

    private CommerceStoreProductListImpl underTest;
    private ProductList mockProductList;
    private CategoryRetriever mockCategoryRetriever;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new CommerceStoreProductListImpl();
        mockProductList = mock(ProductList.class);
        mockCategoryRetriever = mock(CategoryRetriever.class);

        when(mockProductList.getCategoryRetriever()).thenReturn(mockCategoryRetriever);
        FieldUtils.writeField(underTest, "productList", mockProductList, true);
    }

    @Test
    void testInitModelExtendsProductQueryWithStockStatus() {
        underTest.initModel();

        ArgumentCaptor<Consumer<ProductInterfaceQuery>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(mockCategoryRetriever).extendProductQueryWith(captor.capture());

        ProductInterfaceQuery mockQuery = mock(ProductInterfaceQuery.class);
        captor.getValue().accept(mockQuery);
        verify(mockQuery).stockStatus();
    }

    @Test
    void testDelegationMethods() {
        when(mockProductList.showTitle()).thenReturn(true);
        when(mockProductList.getTitle()).thenReturn("Category Title");
        when(mockProductList.getImage()).thenReturn("/image.jpg");
        when(mockProductList.showImage()).thenReturn(true);
        when(mockProductList.isStaged()).thenReturn(false);
        when(mockProductList.getProducts()).thenReturn(Collections.emptyList());
        when(mockProductList.getExperienceFragments()).thenReturn(Collections.emptyList());
        when(mockProductList.loadClientPrice()).thenReturn(true);
        when(mockProductList.getPaginationType()).thenReturn("button");
        when(mockProductList.isAddToCartEnabled()).thenReturn(true);
        when(mockProductList.isAddToWishListEnabled()).thenReturn(true);
        when(mockProductList.getMetaDescription()).thenReturn("Meta desc");
        when(mockProductList.getMetaKeywords()).thenReturn("Keywords");
        when(mockProductList.getMetaTitle()).thenReturn("Meta title");
        when(mockProductList.getCanonicalUrl()).thenReturn("https://example.com");
        when(mockProductList.getAlternateLanguageLinks()).thenReturn(Collections.singletonMap(Locale.US, "https://example.com/en"));

        assertTrue(underTest.showTitle());
        assertEquals("Category Title", underTest.getTitle());
        assertEquals("/image.jpg", underTest.getImage());
        assertTrue(underTest.showImage());
        assertFalse(underTest.isStaged());
        assertNotNull(underTest.getProducts());
        assertNotNull(underTest.getExperienceFragments());
        assertTrue(underTest.loadClientPrice());
        assertEquals("button", underTest.getPaginationType());
        assertTrue(underTest.isAddToCartEnabled());
        assertTrue(underTest.isAddToWishListEnabled());
        assertEquals("Meta desc", underTest.getMetaDescription());
        assertEquals("Keywords", underTest.getMetaKeywords());
        assertEquals("Meta title", underTest.getMetaTitle());
        assertEquals("https://example.com", underTest.getCanonicalUrl());
        assertEquals(1, underTest.getAlternateLanguageLinks().size());
        assertEquals("commerce-store/components/commerce/productlist", underTest.getExportedType());
        assertEquals(mockCategoryRetriever, underTest.getCategoryRetriever());
    }

    @Test
    void testNullProductListSafeguards() throws Exception {
        FieldUtils.writeField(underTest, "productList", null, true);

        underTest.initModel();

        assertFalse(underTest.showTitle());
        assertNull(underTest.getTitle());
        assertNull(underTest.getImage());
        assertFalse(underTest.showImage());
        assertNull(underTest.getCategoryRetriever());
        assertFalse(underTest.isStaged());
        assertNull(underTest.getStorefrontContext());
        assertTrue(underTest.getExperienceFragments().isEmpty());
        assertTrue(underTest.getProducts().isEmpty());
        assertNull(underTest.getSearchResultsSet());
        assertFalse(underTest.loadClientPrice());
        assertNull(underTest.getPaginationType());
        assertFalse(underTest.isAddToCartEnabled());
        assertFalse(underTest.isAddToWishListEnabled());
        assertNull(underTest.getMetaDescription());
        assertNull(underTest.getMetaKeywords());
        assertNull(underTest.getMetaTitle());
        assertNull(underTest.getCanonicalUrl());
        assertTrue(underTest.getAlternateLanguageLinks().isEmpty());
        assertNull(underTest.getId());
        assertNull(underTest.getData());
        assertNull(underTest.getAppliedCssClasses());
    }
}

