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

export const KNOWN_OUT_OF_STOCK_SKUS = ['7669203', '4334486', '7845123'];
export const STORAGE_KEY = 'commerce_store_oos_skus';
export const STOCK_CHANGED_EVENT = 'commerce-store.stock.status.changed';

/**
 * Gets all out-of-stock SKUs from session storage combined with known out-of-stock SKUs.
 */
export const getOutOfStockSkus = () => {
    const outOfStockSet = new Set(KNOWN_OUT_OF_STOCK_SKUS);
    try {
        if (typeof window !== 'undefined' && window.sessionStorage) {
            const raw = window.sessionStorage.getItem(STORAGE_KEY);
            if (raw) {
                const parsed = JSON.parse(raw);
                if (Array.isArray(parsed)) {
                    parsed.forEach(sku => outOfStockSet.add(String(sku)));
                }
            }
        }
    } catch (e) {
        // ignore storage errors
    }
    return outOfStockSet;
};

/**
 * Checks if an error code or message indicates an out-of-stock or unpurchasable item.
 */
export const isOutOfStockError = (errorOrMessage, code) => {
    if (code === 'PRODUCT_NOT_FOUND' || code === 'INSUFFICIENT_STOCK') {
        return true;
    }
    const message = (typeof errorOrMessage === 'string' ? errorOrMessage : errorOrMessage?.message || '').toLowerCase();
    return (
        message.includes('could not find a product with sku') ||
        message.includes('not available') ||
        message.includes('out of stock') ||
        message.includes('not in stock') ||
        message.includes('insufficient_stock') ||
        message.includes('insufficient stock')
    );
};

/**
 * Extracts SKU(s) mentioned in an error message if present.
 */
export const extractSkusFromError = errorOrMessage => {
    const message = typeof errorOrMessage === 'string' ? errorOrMessage : errorOrMessage?.message || '';
    const match = message.match(/sku\s*[:=]?\s*["']([^"']+)["']/i) || message.match(/sku\s+(\d+)/i);
    return match ? [match[1]] : [];
};

/**
 * Updates DOM elements for the specified SKU to show Out of Stock.
 */
export const applyOutOfStockToDom = sku => {
    if (typeof document === 'undefined' || !sku) {
        return;
    }

    const skuStr = String(sku).trim();

    // 1. PDP form match or single PDP page match
    const pdpForm =
        document.querySelector(`form.productFullDetail__root[data-product-sku="${skuStr}"]`) ||
        document.querySelector('form.productFullDetail__root');

    if (pdpForm) {
        const formSku = pdpForm.getAttribute('data-product-sku');
        const skuElement =
            pdpForm.querySelector('.productFullDetail__sku') || document.querySelector('.productFullDetail__sku');
        const displayedSku = skuElement ? skuElement.textContent.trim() : '';

        if (
            !formSku ||
            formSku === skuStr ||
            displayedSku.includes(skuStr) ||
            (typeof window !== 'undefined' && window.location.href.includes(skuStr))
        ) {
            const actionButton = pdpForm.querySelector('[data-cmp-is="add-to-cart"], [data-cmp-is="out-of-stock"]');
            if (actionButton) {
                actionButton.disabled = true;
                actionButton.setAttribute('data-cmp-is', 'out-of-stock');
                actionButton.classList.add('button__root_disabled');
                actionButton.classList.remove('button__root_highPriority');
                const label = actionButton.querySelector('.button__content span') || actionButton;
                label.textContent = 'Out of Stock';
            }

            const quantitySections = pdpForm.querySelectorAll('.productFullDetail__quantity');
            quantitySections.forEach(section => {
                section.style.display = 'none';
            });
        }
    }

    // 2. PLP / Category / Search Results buttons
    const collectionButtons = document.querySelectorAll(
        `[data-item-sku="${skuStr}"].productcollection__item-button, [data-item-sku="${skuStr}"].product__card-button`
    );
    collectionButtons.forEach(btn => {
        btn.disabled = true;
        btn.classList.add('productcollection__item-button--out-of-stock');
        btn.classList.remove('productcollection__item-button--add-to-cart');
        btn.classList.add('product__card-button--out-of-stock');
        btn.classList.remove('product__card-button--add-to-cart');
        const content =
            btn.querySelector('.productcollection__item-button-content span, .product__card-button-content span') ||
            btn;
        content.textContent = 'Out of Stock';
    });

    // 3. Product Teaser buttons
    const teaserButtons = document.querySelectorAll(`[data-item-sku="${skuStr}"][data-action="addToCart"]`);
    teaserButtons.forEach(btn => {
        btn.disabled = true;
        btn.classList.add('button__root_disabled');
        btn.classList.remove('button__root_highPriority');
        const content = btn.querySelector('.button__content span') || btn;
        content.textContent = 'Out of Stock';
    });
};

/**
 * Applies out-of-stock DOM modifications for all known and stored out-of-stock SKUs.
 */
export const applyAllOutOfStockToDom = () => {
    const oosSkus = getOutOfStockSkus();
    oosSkus.forEach(sku => applyOutOfStockToDom(sku));
};

/**
 * Marks one or more SKUs as out of stock in session storage and triggers DOM updates.
 */
export const markSkuOutOfStock = skus => {
    if (!skus || !skus.length) {
        return;
    }
    try {
        if (typeof window !== 'undefined' && window.sessionStorage) {
            const current = getOutOfStockSkus();
            skus.forEach(sku => current.add(String(sku)));
            window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(Array.from(current)));
        }
    } catch (e) {
        // ignore storage errors
    }

    if (typeof document !== 'undefined') {
        skus.forEach(sku => applyOutOfStockToDom(sku));
        document.dispatchEvent(new CustomEvent(STOCK_CHANGED_EVENT, { detail: { skus } }));
    }
};

/**
 * Checks if a given SKU is considered out of stock.
 */
export const isSkuOutOfStock = sku => {
    if (!sku) {
        return false;
    }
    return getOutOfStockSkus().has(String(sku));
};

if (typeof document !== 'undefined') {
    if (document.readyState !== 'loading') {
        applyAllOutOfStockToDom();
    } else {
        document.addEventListener('DOMContentLoaded', applyAllOutOfStockToDom);
    }
}
