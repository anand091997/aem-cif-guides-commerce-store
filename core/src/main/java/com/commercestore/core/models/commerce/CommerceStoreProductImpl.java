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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.via.ResourceSuperType;

import com.adobe.cq.commerce.core.components.models.common.Price;
import com.adobe.cq.commerce.core.components.models.product.Asset;
import com.adobe.cq.commerce.core.components.models.product.GroupItem;
import com.adobe.cq.commerce.core.components.models.product.Product;
import com.adobe.cq.commerce.core.components.models.product.Variant;
import com.adobe.cq.commerce.core.components.models.product.VariantAttribute;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractProductRetriever;
import com.adobe.cq.commerce.core.components.storefrontcontext.ProductStorefrontContext;
import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;

/**
 * Commerce Store Product Sling Model extending CIF Product to handle stock validation and out-of-stock SKUs.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { Product.class, Component.class },
    resourceType = CommerceStoreProductImpl.RESOURCE_TYPE
)
public class CommerceStoreProductImpl implements Product, Component {

    protected static final String RESOURCE_TYPE = "commerce-store/components/commerce/product";

    public static final Set<String> OUT_OF_STOCK_SKUS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("7669203", "4334486", "7845123"))
    );

    @Self
    @Via(type = ResourceSuperType.class)
    private Product product;

    @Override
    public Boolean getInStock() {
        if (product == null || !Boolean.TRUE.equals(product.getInStock())) {
            return false;
        }
        if (product.getSku() != null && OUT_OF_STOCK_SKUS.contains(product.getSku())) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean getFound() {
        return product != null && product.getFound();
    }

    @Override
    public String getName() {
        return product != null ? product.getName() : null;
    }

    @Override
    public String getDescription() {
        return product != null ? product.getDescription() : null;
    }

    @Override
    public String getSku() {
        return product != null ? product.getSku() : null;
    }

    @Override
    public Price getPriceRange() {
        return product != null ? product.getPriceRange() : null;
    }

    @Override
    public Boolean isConfigurable() {
        return product != null && product.isConfigurable();
    }

    @Override
    public Boolean isGroupedProduct() {
        return product != null && product.isGroupedProduct();
    }

    @Override
    public Boolean isVirtualProduct() {
        return product != null && product.isVirtualProduct();
    }

    @Override
    public Boolean isBundleProduct() {
        return product != null && product.isBundleProduct();
    }

    @Override
    public Boolean isGiftCardProduct() {
        return product != null && product.isGiftCardProduct();
    }

    @Override
    public Boolean isStaged() {
        return product != null && product.isStaged();
    }

    @Override
    public String getVariantsJson() {
        return product != null ? product.getVariantsJson() : null;
    }

    @Override
    public String getJsonLd() {
        return product != null ? product.getJsonLd() : null;
    }

    @Override
    public List<Variant> getVariants() {
        return product != null ? product.getVariants() : Collections.emptyList();
    }

    @Override
    public List<GroupItem> getGroupedProductItems() {
        return product != null ? product.getGroupedProductItems() : Collections.emptyList();
    }

    @Override
    public List<Asset> getAssets() {
        return product != null ? product.getAssets() : Collections.emptyList();
    }

    @Override
    public String getAssetsJson() {
        return product != null ? product.getAssetsJson() : null;
    }

    @Override
    public List<VariantAttribute> getVariantAttributes() {
        return product != null ? product.getVariantAttributes() : Collections.emptyList();
    }

    @Override
    public Boolean loadClientPrice() {
        return product != null && product.loadClientPrice();
    }

    @Override
    public AbstractProductRetriever getProductRetriever() {
        return product != null ? product.getProductRetriever() : null;
    }

    @Override
    public ProductStorefrontContext getStorefrontContext() {
        return product != null ? product.getStorefrontContext() : null;
    }

    @Override
    public boolean getAddToWishListEnabled() {
        return product != null && product.getAddToWishListEnabled();
    }

    @Override
    public Set<String> getVisibleSections() {
        return product != null ? product.getVisibleSections() : Collections.emptySet();
    }

    @Override
    public String getMetaDescription() {
        return product != null ? product.getMetaDescription() : null;
    }

    @Override
    public String getMetaKeywords() {
        return product != null ? product.getMetaKeywords() : null;
    }

    @Override
    public String getMetaTitle() {
        return product != null ? product.getMetaTitle() : null;
    }

    @Override
    public String getCanonicalUrl() {
        return product != null ? product.getCanonicalUrl() : null;
    }

    @Override
    public Map<Locale, String> getAlternateLanguageLinks() {
        return product != null ? product.getAlternateLanguageLinks() : Collections.emptyMap();
    }

    @Override
    public String getId() {
        return (product instanceof Component) ? ((Component) product).getId() : null;
    }

    @Override
    public ComponentData getData() {
        return (product instanceof Component) ? ((Component) product).getData() : null;
    }

    @Override
    public String getAppliedCssClasses() {
        return (product instanceof Component) ? ((Component) product).getAppliedCssClasses() : null;
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}

