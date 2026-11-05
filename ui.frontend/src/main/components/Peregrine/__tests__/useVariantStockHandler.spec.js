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
import { renderHook } from '@testing-library/react-hooks';
import useVariantStockHandler from '../useVariantStockHandler';

describe('useVariantStockHandler', () => {
    let button;
    let buttonContent;
    let quantitySection;

    beforeEach(() => {
        document.body.innerHTML = '';

        button = document.createElement('button');
        button.setAttribute('data-cmp-is', 'add-to-cart');
        button.className = 'button__root_highPriority button__root clickable__root button__filled';
        buttonContent = document.createElement('span');
        buttonContent.className = 'button__content';
        const label = document.createElement('span');
        label.textContent = 'Add to Cart';
        buttonContent.appendChild(label);
        button.appendChild(buttonContent);
        document.body.appendChild(button);

        quantitySection = document.createElement('section');
        quantitySection.className = 'productFullDetail__quantity productFullDetail__section';
        document.body.appendChild(quantitySection);
    });

    afterEach(() => {
        document.body.innerHTML = '';
    });

    it('disables button and sets Out of Stock when selected variant is out of stock', () => {
        renderHook(() => useVariantStockHandler());

        document.dispatchEvent(
            new CustomEvent('variantchanged', {
                detail: {
                    variant: {
                        sku: 'test-oos',
                        inStock: false
                    }
                }
            })
        );

        expect(button.disabled).toBe(true);
        expect(button.getAttribute('data-cmp-is')).toBe('out-of-stock');
        expect(button.classList.contains('button__root_disabled')).toBe(true);
        expect(button.classList.contains('button__root_highPriority')).toBe(false);
        expect(buttonContent.querySelector('span').textContent).toBe('Out of Stock');
        expect(quantitySection.style.display).toBe('none');
    });

    it('enables button and restores Add to Cart when selected variant is in stock', () => {
        renderHook(() => useVariantStockHandler());

        // First set out of stock
        document.dispatchEvent(
            new CustomEvent('variantchanged', {
                detail: {
                    variant: {
                        sku: 'test-oos',
                        inStock: false
                    }
                }
            })
        );

        // Then switch to in stock
        document.dispatchEvent(
            new CustomEvent('variantchanged', {
                detail: {
                    variant: {
                        sku: 'test-in-stock',
                        inStock: true
                    }
                }
            })
        );

        expect(button.disabled).toBe(false);
        expect(button.getAttribute('data-cmp-is')).toBe('add-to-cart');
        expect(button.classList.contains('button__root_disabled')).toBe(false);
        expect(button.classList.contains('button__root_highPriority')).toBe(true);
        expect(buttonContent.querySelector('span').textContent).toBe('Add to Cart');
        expect(quantitySection.style.display).toBe('');
    });

    it('disables button when variant is cleared or null', () => {
        renderHook(() => useVariantStockHandler());

        document.dispatchEvent(
            new CustomEvent('variantchanged', {
                detail: {
                    variant: null
                }
            })
        );

        expect(button.disabled).toBe(true);
    });
});
