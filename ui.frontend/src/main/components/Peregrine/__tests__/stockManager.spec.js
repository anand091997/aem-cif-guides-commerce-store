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
import {
    getOutOfStockSkus,
    isOutOfStockError,
    extractSkusFromError,
    applyOutOfStockToDom,
    markSkuOutOfStock,
    isSkuOutOfStock,
    KNOWN_OUT_OF_STOCK_SKUS
} from '../stockManager';

describe('stockManager', () => {
    beforeEach(() => {
        sessionStorage.clear();
        document.body.innerHTML = '';
    });

    afterEach(() => {
        sessionStorage.clear();
        document.body.innerHTML = '';
    });

    it('identifies known out-of-stock SKUs', () => {
        KNOWN_OUT_OF_STOCK_SKUS.forEach(sku => {
            expect(isSkuOutOfStock(sku)).toBe(true);
        });
        expect(isSkuOutOfStock('unknown-sku')).toBe(false);
    });

    it('identifies out-of-stock errors and extracts SKUs', () => {
        expect(isOutOfStockError('Could not find a product with SKU "7845123"', 'PRODUCT_NOT_FOUND')).toBe(true);
        expect(isOutOfStockError('The requested qty is not available', 'INSUFFICIENT_STOCK')).toBe(true);
        expect(isOutOfStockError('Random error', null)).toBe(false);

        expect(extractSkusFromError('Could not find a product with SKU "7845123"')).toEqual(['7845123']);
        expect(extractSkusFromError("Could not find a product with SKU '4334486'")).toEqual(['4334486']);
        expect(extractSkusFromError('No sku here')).toEqual([]);
    });

    it('marks SKU as out of stock and stores in sessionStorage', () => {
        markSkuOutOfStock(['dynamic-oos-1', 'dynamic-oos-2']);
        expect(isSkuOutOfStock('dynamic-oos-1')).toBe(true);
        expect(isSkuOutOfStock('dynamic-oos-2')).toBe(true);
        expect(getOutOfStockSkus().has('dynamic-oos-1')).toBe(true);
    });

    it('updates DOM elements for PDP and collection buttons', () => {
        document.body.innerHTML = `
            <form class="productFullDetail__root" data-product-sku="7669203">
                <section class="productFullDetail__quantity">
                    <select><option value="1">1</option></select>
                </section>
                <button class="button__root_highPriority button__root clickable__root button__filled" type="button" data-cmp-is="add-to-cart">
                    <span class="button__content"><span>Add to Cart</span></span>
                </button>
            </form>
            <button data-item-sku="7669203" class="productcollection__item-button productcollection__item-button--add-to-cart">
                <span class="productcollection__item-button-content"><span>Add to Cart</span></span>
            </button>
        `;

        applyOutOfStockToDom('7669203');

        const pdpButton = document.querySelector('form.productFullDetail__root [data-cmp-is="out-of-stock"]');
        expect(pdpButton).not.toBeNull();
        expect(pdpButton.disabled).toBe(true);
        expect(pdpButton.textContent).toContain('Out of Stock');

        const qty = document.querySelector('.productFullDetail__quantity');
        expect(qty.style.display).toBe('none');

        const plpButton = document.querySelector('.productcollection__item-button');
        expect(plpButton.disabled).toBe(true);
        expect(plpButton.classList.contains('productcollection__item-button--out-of-stock')).toBe(true);
        expect(plpButton.textContent).toContain('Out of Stock');
    });
});
