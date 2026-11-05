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

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.via.ResourceSuperType;

import com.adobe.cq.commerce.core.components.models.common.ProductListItem;
import com.adobe.cq.commerce.core.components.models.productcarousel.ProductCarousel;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractProductsRetriever;
import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;

/**
 * Commerce Store Product Carousel Sling Model extending CIF ProductCarousel to include stock status in product queries.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { ProductCarousel.class, Component.class },
    resourceType = CommerceStoreProductCarouselImpl.RESOURCE_TYPE
)
public class CommerceStoreProductCarouselImpl implements ProductCarousel, Component {

    protected static final String RESOURCE_TYPE = "commerce-store/components/commerce/productcarousel";

    @Self
    @Via(type = ResourceSuperType.class)
    private ProductCarousel productCarousel;

    @PostConstruct
    public void initModel() {
        if (productCarousel != null && productCarousel.getProductsRetriever() != null) {
            productCarousel.getProductsRetriever().extendProductQueryWith(p -> p.stockStatus());
        }
    }

    @Override
    public List<ProductListItem> getProducts() {
        return productCarousel != null ? productCarousel.getProducts() : Collections.emptyList();
    }

    @Override
    public List<ProductListItem> getProductIdentifiers() {
        return productCarousel != null ? productCarousel.getProductIdentifiers() : Collections.emptyList();
    }

    @Override
    public AbstractProductsRetriever getProductsRetriever() {
        return productCarousel != null ? productCarousel.getProductsRetriever() : null;
    }

    @Override
    public boolean isConfigured() {
        return productCarousel != null && productCarousel.isConfigured();
    }

    @Override
    public String getTitleType() {
        return productCarousel != null ? productCarousel.getTitleType() : null;
    }

    @Override
    public boolean isAddToCartEnabled() {
        return productCarousel != null && productCarousel.isAddToCartEnabled();
    }

    @Override
    public boolean isAddToWishListEnabled() {
        return productCarousel != null && productCarousel.isAddToWishListEnabled();
    }

    @Override
    public String getLinkTarget() {
        return productCarousel != null ? productCarousel.getLinkTarget() : null;
    }

    @Override
    public String getId() {
        return (productCarousel instanceof Component) ? ((Component) productCarousel).getId() : null;
    }

    @Override
    public ComponentData getData() {
        return (productCarousel instanceof Component) ? ((Component) productCarousel).getData() : null;
    }

    @Override
    public String getAppliedCssClasses() {
        return (productCarousel instanceof Component) ? ((Component) productCarousel).getAppliedCssClasses() : null;
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}

