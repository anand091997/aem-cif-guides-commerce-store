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

import com.adobe.cq.commerce.core.components.models.common.Price;
import com.adobe.cq.commerce.core.components.models.product.Product;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractProductRetriever;
import com.adobe.cq.commerce.core.components.storefrontcontext.ProductStorefrontContext;
import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;

class CommerceStoreProductImplTest {

    private CommerceStoreProductImpl underTest;
    private Product mockProduct;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new CommerceStoreProductImpl();
        mockProduct = mock(Product.class, withSettings().extraInterfaces(Component.class));
        FieldUtils.writeField(underTest, "product", mockProduct, true);
    }

    @Test
    void testGetInStock_NormalInStockProduct() {
        when(mockProduct.getInStock()).thenReturn(true);
        when(mockProduct.getSku()).thenReturn("in-stock-sku");

        assertTrue(underTest.getInStock());
    }

    @Test
    void testGetInStock_NormalOutOfStockProduct() {
        when(mockProduct.getInStock()).thenReturn(false);
        when(mockProduct.getSku()).thenReturn("regular-oos-sku");

        assertFalse(underTest.getInStock());
    }

    @Test
    void testGetInStock_KnownOutOfStockSkusAreAlwaysOutOfStock() {
        for (String sku : CommerceStoreProductImpl.OUT_OF_STOCK_SKUS) {
            when(mockProduct.getInStock()).thenReturn(true);
            when(mockProduct.getSku()).thenReturn(sku);

            assertFalse(underTest.getInStock(), "SKU " + sku + " should be reported as out of stock");
        }
    }

    @Test
    void testGetInStock_NullProductOrNullStock() {
        when(mockProduct.getInStock()).thenReturn(null);
        assertFalse(underTest.getInStock());

        CommerceStoreProductImpl nullDelegateModel = new CommerceStoreProductImpl();
        assertFalse(nullDelegateModel.getInStock());
    }

    @Test
    void testDelegationMethods() {
        Price mockPrice = mock(Price.class);
        AbstractProductRetriever mockRetriever = mock(AbstractProductRetriever.class);
        ProductStorefrontContext mockContext = mock(ProductStorefrontContext.class);
        ComponentData mockData = mock(ComponentData.class);

        when(mockProduct.getFound()).thenReturn(true);
        when(mockProduct.getName()).thenReturn("Product Name");
        when(mockProduct.getDescription()).thenReturn("Product Description");
        when(mockProduct.getSku()).thenReturn("test-sku");
        when(mockProduct.getPriceRange()).thenReturn(mockPrice);
        when(mockProduct.isConfigurable()).thenReturn(true);
        when(mockProduct.isGroupedProduct()).thenReturn(false);
        when(mockProduct.isVirtualProduct()).thenReturn(false);
        when(mockProduct.isBundleProduct()).thenReturn(false);
        when(mockProduct.isGiftCardProduct()).thenReturn(false);
        when(mockProduct.isStaged()).thenReturn(false);
        when(mockProduct.getVariantsJson()).thenReturn("{}");
        when(mockProduct.getJsonLd()).thenReturn("{jsonld}");
        when(mockProduct.getVariants()).thenReturn(Collections.emptyList());
        when(mockProduct.getGroupedProductItems()).thenReturn(Collections.emptyList());
        when(mockProduct.getAssets()).thenReturn(Collections.emptyList());
        when(mockProduct.getAssetsJson()).thenReturn("[]");
        when(mockProduct.getVariantAttributes()).thenReturn(Collections.emptyList());
        when(mockProduct.loadClientPrice()).thenReturn(true);
        when(mockProduct.getProductRetriever()).thenReturn(mockRetriever);
        when(mockProduct.getStorefrontContext()).thenReturn(mockContext);
        when(mockProduct.getAddToWishListEnabled()).thenReturn(true);
        when(mockProduct.getVisibleSections()).thenReturn(Collections.singleton("ACTIONS"));
        when(mockProduct.getMetaDescription()).thenReturn("Meta Description");
        when(mockProduct.getMetaKeywords()).thenReturn("Keywords");
        when(mockProduct.getMetaTitle()).thenReturn("Meta Title");
        when(mockProduct.getCanonicalUrl()).thenReturn("https://example.com/p/test.html");
        when(mockProduct.getAlternateLanguageLinks()).thenReturn(Collections.singletonMap(Locale.US, "https://example.com/en/test.html"));
        when(((Component) mockProduct).getId()).thenReturn("product-id-123");
        when(((Component) mockProduct).getData()).thenReturn(mockData);
        when(((Component) mockProduct).getAppliedCssClasses()).thenReturn("custom-class");

        assertTrue(underTest.getFound());
        assertEquals("Product Name", underTest.getName());
        assertEquals("Product Description", underTest.getDescription());
        assertEquals("test-sku", underTest.getSku());
        assertEquals(mockPrice, underTest.getPriceRange());
        assertTrue(underTest.isConfigurable());
        assertFalse(underTest.isGroupedProduct());
        assertFalse(underTest.isVirtualProduct());
        assertFalse(underTest.isBundleProduct());
        assertFalse(underTest.isGiftCardProduct());
        assertFalse(underTest.isStaged());
        assertEquals("{}", underTest.getVariantsJson());
        assertEquals("{jsonld}", underTest.getJsonLd());
        assertNotNull(underTest.getVariants());
        assertNotNull(underTest.getGroupedProductItems());
        assertNotNull(underTest.getAssets());
        assertEquals("[]", underTest.getAssetsJson());
        assertNotNull(underTest.getVariantAttributes());
        assertTrue(underTest.loadClientPrice());
        assertEquals(mockRetriever, underTest.getProductRetriever());
        assertEquals(mockContext, underTest.getStorefrontContext());
        assertTrue(underTest.getAddToWishListEnabled());
        assertEquals(1, underTest.getVisibleSections().size());
        assertEquals("Meta Description", underTest.getMetaDescription());
        assertEquals("Keywords", underTest.getMetaKeywords());
        assertEquals("Meta Title", underTest.getMetaTitle());
        assertEquals("https://example.com/p/test.html", underTest.getCanonicalUrl());
        assertEquals(1, underTest.getAlternateLanguageLinks().size());
        assertEquals("product-id-123", underTest.getId());
        assertEquals(mockData, underTest.getData());
        assertEquals("custom-class", underTest.getAppliedCssClasses());
        assertEquals("commerce-store/components/commerce/product", underTest.getExportedType());
    }

    @Test
    void testDelegationMethodsWithNullProduct() throws Exception {
        FieldUtils.writeField(underTest, "product", null, true);

        assertFalse(underTest.getFound());
        assertNull(underTest.getName());
        assertNull(underTest.getDescription());
        assertNull(underTest.getSku());
        assertNull(underTest.getPriceRange());
        assertFalse(underTest.isConfigurable());
        assertFalse(underTest.isGroupedProduct());
        assertFalse(underTest.isVirtualProduct());
        assertFalse(underTest.isBundleProduct());
        assertFalse(underTest.isGiftCardProduct());
        assertFalse(underTest.isStaged());
        assertNull(underTest.getVariantsJson());
        assertNull(underTest.getJsonLd());
        assertTrue(underTest.getVariants().isEmpty());
        assertTrue(underTest.getGroupedProductItems().isEmpty());
        assertTrue(underTest.getAssets().isEmpty());
        assertNull(underTest.getAssetsJson());
        assertTrue(underTest.getVariantAttributes().isEmpty());
        assertFalse(underTest.loadClientPrice());
        assertNull(underTest.getProductRetriever());
        assertNull(underTest.getStorefrontContext());
        assertFalse(underTest.getAddToWishListEnabled());
        assertTrue(underTest.getVisibleSections().isEmpty());
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

