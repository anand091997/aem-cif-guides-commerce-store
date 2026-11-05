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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.via.ResourceSuperType;

import com.adobe.cq.commerce.core.components.models.common.ProductListItem;
import com.adobe.cq.commerce.core.components.models.experiencefragment.CommerceExperienceFragmentContainer;
import com.adobe.cq.commerce.core.components.models.productlist.CategoryRetriever;
import com.adobe.cq.commerce.core.components.models.productlist.ProductList;
import com.adobe.cq.commerce.core.components.storefrontcontext.CategoryStorefrontContext;
import com.adobe.cq.commerce.core.search.models.SearchResultsSet;
import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;

/**
 * Venia Product List Sling Model extending CIF ProductList to include stock status in category queries.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { ProductList.class, Component.class },
    resourceType = CommerceStoreProductListImpl.RESOURCE_TYPE
)
public class CommerceStoreProductListImpl implements ProductList, Component {

    protected static final String RESOURCE_TYPE = "commerce-store/components/commerce/productlist";

    @Self
    @Via(type = ResourceSuperType.class)
    private ProductList productList;

    @PostConstruct
    public void initModel() {
        if (productList != null && productList.getCategoryRetriever() != null) {
            productList.getCategoryRetriever().extendProductQueryWith(p -> p.stockStatus());
        }
    }

    @Override
    public boolean showTitle() {
        return productList != null && productList.showTitle();
    }

    @Override
    public String getTitle() {
        return productList != null ? productList.getTitle() : null;
    }

    @Override
    public String getImage() {
        return productList != null ? productList.getImage() : null;
    }

    @Override
    public boolean showImage() {
        return productList != null && productList.showImage();
    }

    @Override
    public CategoryRetriever getCategoryRetriever() {
        return productList != null ? productList.getCategoryRetriever() : null;
    }

    @Override
    public Boolean isStaged() {
        return productList != null ? productList.isStaged() : Boolean.FALSE;
    }

    @Override
    public CategoryStorefrontContext getStorefrontContext() {
        return productList != null ? productList.getStorefrontContext() : null;
    }

    @Override
    public List<CommerceExperienceFragmentContainer> getExperienceFragments() {
        return productList != null ? productList.getExperienceFragments() : Collections.emptyList();
    }

    @Override
    public Collection<ProductListItem> getProducts() {
        return productList != null ? productList.getProducts() : Collections.emptyList();
    }

    @Override
    public SearchResultsSet getSearchResultsSet() {
        return productList != null ? productList.getSearchResultsSet() : null;
    }

    @Override
    public boolean loadClientPrice() {
        return productList != null && productList.loadClientPrice();
    }

    @Override
    public String getPaginationType() {
        return productList != null ? productList.getPaginationType() : null;
    }

    @Override
    public boolean isAddToCartEnabled() {
        return productList != null && productList.isAddToCartEnabled();
    }

    @Override
    public boolean isAddToWishListEnabled() {
        return productList != null && productList.isAddToWishListEnabled();
    }

    @Override
    public String getMetaDescription() {
        return productList != null ? productList.getMetaDescription() : null;
    }

    @Override
    public String getMetaKeywords() {
        return productList != null ? productList.getMetaKeywords() : null;
    }

    @Override
    public String getMetaTitle() {
        return productList != null ? productList.getMetaTitle() : null;
    }

    @Override
    public String getCanonicalUrl() {
        return productList != null ? productList.getCanonicalUrl() : null;
    }

    @Override
    public Map<Locale, String> getAlternateLanguageLinks() {
        return productList != null ? productList.getAlternateLanguageLinks() : Collections.emptyMap();
    }

    @Override
    public String getId() {
        return (productList instanceof Component) ? ((Component) productList).getId() : null;
    }

    @Override
    public ComponentData getData() {
        return (productList instanceof Component) ? ((Component) productList).getData() : null;
    }

    @Override
    public String getAppliedCssClasses() {
        return (productList instanceof Component) ? ((Component) productList).getAppliedCssClasses() : null;
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}

