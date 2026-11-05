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
import { gql, useMutation } from '@apollo/client';
import React, { useCallback, useMemo } from 'react';
import { defineMessages, useIntl } from 'react-intl';
import { AlertCircle, Check } from 'react-feather';
import { useAddToCart, useAddToCartEvent } from '@adobe/aem-core-cif-react-components';
import { useToasts } from '@magento/peregrine';
import { MiniCartFragment } from '@magento/peregrine/lib/talons/MiniCart/miniCartFragments.gql';
import Icon from '@magento/venia-ui/lib/components/Icon';
import { extractSkusFromError, isOutOfStockError, markSkuOutOfStock } from './stockManager';

const ADD_PRODUCTS_TO_CART = gql`
    mutation AddProductsToCartWithUserErrors($cartId: String!, $cartItems: [CartItemInput!]!) {
        addProductsToCart(cartId: $cartId, cartItems: $cartItems) {
            cart {
                id
                items {
                    uid
                    quantity
                    product {
                        sku
                        name
                    }
                }
                ...MiniCartFragment
            }
            user_errors {
                message
                code
            }
        }
    }
    ${MiniCartFragment}
`;

const messages = defineMessages({
    cartSuccess: {
        id: 'toast.cart.success',
        defaultMessage: 'Product was added to your cart.'
    },
    cartError: {
        id: 'toast.cart.error',
        defaultMessage: 'Product could not be added to your cart.'
    },
    outOfStockError: {
        id: 'toast.cart.outOfStock',
        defaultMessage: 'This product is currently out of stock.'
    }
});

const successIcon = <Icon size={20} src={Check} />;
const errorIcon = <Icon size={20} src={AlertCircle} />;

const collectUserErrors = result => {
    const payload = result?.data?.addProductsToCart;
    if (!payload) {
        return [];
    }
    return payload.user_errors || [];
};

/**
 * CIF add-to-cart mutations omit user_errors, so Commerce stock/validation failures are silent.
 * Wrap all addToCartApi mutation methods (physical, virtual, bundle, and products)
 * to surface GraphQL/user errors (e.g. out of stock) and successes via toasts,
 * while automatically locking out-of-stock items to "Out of Stock".
 */
const useCommerceStoreAddToCartEvent = () => {
    const { formatMessage } = useIntl();
    const [, { addToast }] = useToasts();
    const [{ cartId }, defaultAddToCartApi] = useAddToCart();
    const [addProductsToCartMutation] = useMutation(ADD_PRODUCTS_TO_CART);

    const notifyCartResult = useCallback(
        (userErrors = [], graphQLErrors = [], fallbackSkus = []) => {
            const allErrors = [...(userErrors || []), ...(graphQLErrors || [])];

            if (allErrors.length > 0) {
                const oosError = allErrors.find(e => isOutOfStockError(e?.message, e?.code));

                if (oosError) {
                    const extractedSkus = extractSkusFromError(oosError);
                    const skusToMark = extractedSkus.length > 0 ? extractedSkus : fallbackSkus;

                    if (skusToMark && skusToMark.length > 0) {
                        markSkuOutOfStock(skusToMark);
                    } else if (typeof document !== 'undefined') {
                        const currentSku = document.querySelector('form.productFullDetail__root')?.dataset?.productSku;
                        if (currentSku) {
                            markSkuOutOfStock([currentSku]);
                        }
                    }

                    addToast({
                        type: 'error',
                        icon: errorIcon,
                        message: formatMessage(messages.outOfStockError),
                        dismissable: true,
                        timeout: 7000
                    });
                    return;
                }

                const message =
                    userErrors?.length > 0
                        ? userErrors.map(e => e.message).join(' ')
                        : graphQLErrors[0]?.message || formatMessage(messages.cartError);

                addToast({
                    type: 'error',
                    icon: errorIcon,
                    message,
                    dismissable: true,
                    timeout: 7000
                });
                return;
            }

            addToast({
                type: 'info',
                icon: successIcon,
                message: formatMessage(messages.cartSuccess),
                dismissable: true,
                timeout: 4000
            });
            document.dispatchEvent(new CustomEvent('aem.cif.open-cart'));
        },
        [addToast, formatMessage]
    );

    const wrapApiMethod = useCallback(
        apiFn => {
            if (typeof apiFn !== 'function') {
                return apiFn;
            }
            return async (...args) => {
                const arg = args[0];
                const requestedSkus = Array.isArray(arg)
                    ? arg.map(i => i?.data?.sku || i?.sku).filter(Boolean)
                    : arg?.data?.sku
                    ? [arg.data.sku]
                    : arg?.sku
                    ? [arg.sku]
                    : [];

                if (!cartId) {
                    notifyCartResult([], [{ message: formatMessage(messages.cartError) }], requestedSkus);
                    return;
                }
                try {
                    const result = await apiFn(...args);
                    notifyCartResult([], result?.errors, requestedSkus);
                    return result;
                } catch (error) {
                    const graphQLErrors = error?.graphQLErrors?.length
                        ? error.graphQLErrors
                        : [{ message: error?.message || formatMessage(messages.cartError) }];
                    notifyCartResult([], graphQLErrors, requestedSkus);
                    return { errors: graphQLErrors };
                }
            };
        },
        [cartId, formatMessage, notifyCartResult]
    );

    const addProductsToCart = useCallback(
        async cartItems => {
            const requestedSkus = (cartItems || []).map(item => item?.sku).filter(Boolean);
            if (!cartId) {
                notifyCartResult([], [{ message: formatMessage(messages.cartError) }], requestedSkus);
                return;
            }
            try {
                const result = await addProductsToCartMutation({
                    variables: {
                        cartId,
                        cartItems
                    }
                });
                notifyCartResult(collectUserErrors(result), result?.errors, requestedSkus);
                return result;
            } catch (error) {
                const graphQLErrors = error?.graphQLErrors?.length
                    ? error.graphQLErrors
                    : [{ message: error?.message || formatMessage(messages.cartError) }];
                notifyCartResult([], graphQLErrors, requestedSkus);
                return { errors: graphQLErrors };
            }
        },
        [addProductsToCartMutation, cartId, formatMessage, notifyCartResult]
    );

    const addToCartApi = useMemo(() => {
        return {
            ...defaultAddToCartApi,
            addProductsToCart,
            addPhysicalProductItems: wrapApiMethod(defaultAddToCartApi.addPhysicalProductItems),
            addVirtualProductItems: wrapApiMethod(defaultAddToCartApi.addVirtualProductItems),
            addBundledProductItems: wrapApiMethod(defaultAddToCartApi.addBundledProductItems),
            addGiftCardProductItems: wrapApiMethod(defaultAddToCartApi.addGiftCardProductItems),
            addPhysicalAndVirtualProductItems: wrapApiMethod(defaultAddToCartApi.addPhysicalAndVirtualProductItems)
        };
    }, [addProductsToCart, defaultAddToCartApi, wrapApiMethod]);

    useAddToCartEvent({ addToCartApi });
};

export default useCommerceStoreAddToCartEvent;
