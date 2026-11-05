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
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.adobe.cq.commerce.core.components.models.productcarousel.ProductCarousel;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractProductsRetriever;
import com.adobe.cq.commerce.magento.graphql.ProductInterfaceQuery;

class CommerceStoreProductCarouselImplTest {

    private CommerceStoreProductCarouselImpl underTest;
    private ProductCarousel mockProductCarousel;
    private AbstractProductsRetriever mockRetriever;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new CommerceStoreProductCarouselImpl();
        mockProductCarousel = mock(ProductCarousel.class);
        mockRetriever = mock(AbstractProductsRetriever.class);

        when(mockProductCarousel.getProductsRetriever()).thenReturn(mockRetriever);
        FieldUtils.writeField(underTest, "productCarousel", mockProductCarousel, true);
    }

    @Test
    void testInitModelExtendsProductQueryWithStockStatus() {
        underTest.initModel();

        ArgumentCaptor<Consumer<ProductInterfaceQuery>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(mockRetriever).extendProductQueryWith(captor.capture());

        ProductInterfaceQuery mockQuery = mock(ProductInterfaceQuery.class);
        captor.getValue().accept(mockQuery);
        verify(mockQuery).stockStatus();
    }

    @Test
    void testDelegationMethods() {
        when(mockProductCarousel.isConfigured()).thenReturn(true);
        when(mockProductCarousel.getProducts()).thenReturn(Collections.emptyList());
        when(mockProductCarousel.getProductIdentifiers()).thenReturn(Collections.emptyList());
        when(mockProductCarousel.getTitleType()).thenReturn("h2");
        when(mockProductCarousel.isAddToCartEnabled()).thenReturn(true);
        when(mockProductCarousel.isAddToWishListEnabled()).thenReturn(true);
        when(mockProductCarousel.getLinkTarget()).thenReturn("_blank");

        assertTrue(underTest.isConfigured());
        assertNotNull(underTest.getProducts());
        assertNotNull(underTest.getProductIdentifiers());
        assertEquals("h2", underTest.getTitleType());
        assertTrue(underTest.isAddToCartEnabled());
        assertTrue(underTest.isAddToWishListEnabled());
        assertEquals("_blank", underTest.getLinkTarget());
        assertEquals(mockRetriever, underTest.getProductsRetriever());
        assertEquals("commerce-store/components/commerce/productcarousel", underTest.getExportedType());
    }

    @Test
    void testNullSafeguards() throws Exception {
        FieldUtils.writeField(underTest, "productCarousel", null, true);

        underTest.initModel();

        assertFalse(underTest.isConfigured());
        assertTrue(underTest.getProducts().isEmpty());
        assertTrue(underTest.getProductIdentifiers().isEmpty());
        assertNull(underTest.getProductsRetriever());
        assertNull(underTest.getTitleType());
        assertFalse(underTest.isAddToCartEnabled());
        assertFalse(underTest.isAddToWishListEnabled());
        assertNull(underTest.getLinkTarget());
        assertNull(underTest.getId());
        assertNull(underTest.getData());
        assertNull(underTest.getAppliedCssClasses());
    }
}

