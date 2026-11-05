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
import React from 'react';
import { IntlProvider } from 'react-intl';
import { renderHook } from '@testing-library/react-hooks';
import Icon from '@magento/venia-ui/lib/components/Icon';
import { Check, AlertCircle } from 'react-feather';

import useCommerceStoreAddToCartEvent from '../useCommerceStoreAddToCartEvent';

const mockAddToast = jest.fn();
jest.mock('@magento/peregrine', () => ({
    useToasts: () => [{}, { addToast: mockAddToast }]
}));

let capturedAddToCartApi = null;
jest.mock('@adobe/aem-core-cif-react-components', () => ({
    useAddToCart: jest.fn(),
    useAddToCartEvent: jest.fn(({ addToCartApi }) => {
        capturedAddToCartApi = addToCartApi;
    })
}));

const mockAddProductsToCartMutation = jest.fn();
jest.mock('@apollo/client', () => ({
    ...jest.requireActual('@apollo/client'),
    useMutation: () => [mockAddProductsToCartMutation]
}));

import { useAddToCart } from '@adobe/aem-core-cif-react-components';

const successIcon = <Icon size={20} src={Check} />;
const errorIcon = <Icon size={20} src={AlertCircle} />;

describe('useCommerceStoreAddToCartEvent', () => {
    let mockDefaultAddToCartApi;
    let dispatchEventSpy;

    beforeEach(() => {
        jest.clearAllMocks();
        sessionStorage.clear();
        capturedAddToCartApi = null;
        dispatchEventSpy = jest.spyOn(document, 'dispatchEvent');

        mockDefaultAddToCartApi = {
            addPhysicalProductItems: jest.fn().mockResolvedValue({}),
            addVirtualProductItems: jest.fn().mockResolvedValue({}),
            addBundledProductItems: jest.fn().mockResolvedValue({}),
            addGiftCardProductItems: jest.fn().mockResolvedValue({}),
            addPhysicalAndVirtualProductItems: jest.fn().mockResolvedValue({})
        };

        useAddToCart.mockReturnValue([{ cartId: 'test-cart-id' }, mockDefaultAddToCartApi]);
    });

    afterEach(() => {
        dispatchEventSpy.mockRestore();
    });

    const wrapper = ({ children }) => <IntlProvider locale="en">{children}</IntlProvider>;

    it('intercepts addPhysicalProductItems and shows success toast + opens cart', async () => {
        renderHook(() => useCommerceStoreAddToCartEvent(), { wrapper });

        expect(capturedAddToCartApi).toBeDefined();

        await capturedAddToCartApi.addPhysicalProductItems([{ data: { sku: '7573952', quantity: 1 } }]);

        expect(mockDefaultAddToCartApi.addPhysicalProductItems).toHaveBeenCalledWith([
            { data: { sku: '7573952', quantity: 1 } }
        ]);
        expect(mockAddToast).toHaveBeenCalledWith({
            type: 'info',
            icon: successIcon,
            message: 'Product was added to your cart.',
            dismissable: true,
            timeout: 4000
        });
        expect(dispatchEventSpy).toHaveBeenCalledWith(expect.objectContaining({ type: 'aem.cif.open-cart' }));
    });

    it('intercepts addPhysicalProductItems and shows friendly out-of-stock toast on failure', async () => {
        const outOfStockError = new Error('Product that you are trying to add is not available.');
        outOfStockError.graphQLErrors = [{ message: 'Product that you are trying to add is not available.' }];
        mockDefaultAddToCartApi.addPhysicalProductItems.mockRejectedValue(outOfStockError);

        renderHook(() => useCommerceStoreAddToCartEvent(), { wrapper });

        await capturedAddToCartApi.addPhysicalProductItems([{ data: { sku: '4334486', quantity: 1 } }]);

        expect(mockAddToast).toHaveBeenCalledWith({
            type: 'error',
            icon: errorIcon,
            message: 'This product is currently out of stock.',
            dismissable: true,
            timeout: 7000
        });
    });

    it('handles addProductsToCart with out-of-stock user_errors and displays friendly message', async () => {
        mockAddProductsToCartMutation.mockResolvedValue({
            data: {
                addProductsToCart: {
                    user_errors: [{ message: 'Could not find a product with SKU "7845123"', code: 'PRODUCT_NOT_FOUND' }]
                }
            }
        });

        renderHook(() => useCommerceStoreAddToCartEvent(), { wrapper });

        await capturedAddToCartApi.addProductsToCart([{ sku: '7845123', quantity: 1 }]);

        expect(mockAddToast).toHaveBeenCalledWith({
            type: 'error',
            icon: errorIcon,
            message: 'This product is currently out of stock.',
            dismissable: true,
            timeout: 7000
        });
    });

    it('handles addProductsToCart success', async () => {
        mockAddProductsToCartMutation.mockResolvedValue({
            data: {
                addProductsToCart: {
                    user_errors: []
                }
            }
        });

        renderHook(() => useCommerceStoreAddToCartEvent(), { wrapper });

        await capturedAddToCartApi.addProductsToCart([{ sku: '7573952', quantity: 1 }]);

        expect(mockAddToast).toHaveBeenCalledWith({
            type: 'info',
            icon: successIcon,
            message: 'Product was added to your cart.',
            dismissable: true,
            timeout: 4000
        });
        expect(dispatchEventSpy).toHaveBeenCalledWith(expect.objectContaining({ type: 'aem.cif.open-cart' }));
    });

    it('handles missing cartId gracefully', async () => {
        useAddToCart.mockReturnValue([{ cartId: null }, mockDefaultAddToCartApi]);

        renderHook(() => useCommerceStoreAddToCartEvent(), { wrapper });

        await capturedAddToCartApi.addPhysicalProductItems([{ data: { sku: '7573952', quantity: 1 } }]);

        expect(mockDefaultAddToCartApi.addPhysicalProductItems).not.toHaveBeenCalled();
        expect(mockAddToast).toHaveBeenCalledWith(
            expect.objectContaining({
                type: 'error',
                message: 'Product could not be added to your cart.'
            })
        );
    });
});
