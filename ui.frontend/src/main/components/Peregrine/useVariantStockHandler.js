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
import { useEffect } from 'react';
import { applyAllOutOfStockToDom, applyOutOfStockToDom, isSkuOutOfStock, STOCK_CHANGED_EVENT } from './stockManager';

/**
 * Listens for variant changes and stock changes on PDP and updates button state and quantity visibility
 * according to variant inStock status.
 */
const useVariantStockHandler = () => {
    useEffect(() => {
        // Initial check on mount
        applyAllOutOfStockToDom();

        const form = document.querySelector('form.productFullDetail__root');
        const currentSku = form?.getAttribute('data-product-sku');
        if (currentSku && isSkuOutOfStock(currentSku)) {
            applyOutOfStockToDom(currentSku);
        }

        const handleVariantChanged = event => {
            const detail = event && event.detail;
            if (!detail) {
                return;
            }

            const { variant } = detail;
            const actionButton = document.querySelector('[data-cmp-is="add-to-cart"], [data-cmp-is="out-of-stock"]');
            const quantitySections = document.querySelectorAll('.productFullDetail__quantity');

            if (!variant) {
                if (actionButton) {
                    actionButton.disabled = true;
                }
                return;
            }

            const isVariantInStock = variant.inStock !== false && !isSkuOutOfStock(variant.sku);

            if (actionButton) {
                const labelElement = actionButton.querySelector('.button__content span') || actionButton;

                if (!isVariantInStock) {
                    actionButton.disabled = true;
                    actionButton.setAttribute('data-cmp-is', 'out-of-stock');
                    actionButton.classList.add('button__root_disabled');
                    actionButton.classList.remove('button__root_highPriority');
                    labelElement.textContent = 'Out of Stock';
                    quantitySections.forEach(section => {
                        section.style.display = 'none';
                    });
                } else {
                    actionButton.disabled = false;
                    actionButton.setAttribute('data-cmp-is', 'add-to-cart');
                    actionButton.classList.remove('button__root_disabled');
                    actionButton.classList.add('button__root_highPriority');
                    labelElement.textContent = 'Add to Cart';
                    quantitySections.forEach(section => {
                        section.style.display = '';
                    });
                }
            }
        };

        const handleStockChanged = event => {
            const skus = event?.detail?.skus || [];
            skus.forEach(sku => applyOutOfStockToDom(sku));
        };

        document.addEventListener('variantchanged', handleVariantChanged);
        document.addEventListener(STOCK_CHANGED_EVENT, handleStockChanged);

        return () => {
            document.removeEventListener('variantchanged', handleVariantChanged);
            document.removeEventListener(STOCK_CHANGED_EVENT, handleStockChanged);
        };
    }, []);
};

export default useVariantStockHandler;
