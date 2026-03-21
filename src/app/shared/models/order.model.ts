import type { Product } from './product.model';

export interface OrderAddress {
    id?: number;
    firstName: string;
    lastName: string;
    email: string;
    telephone: string;
    address: string;
    city: string;
    state: string;
    postalCode: string;
}

export interface ProductOrder {
    id: number;
    orderId: string;
    orderDate: string;
    product: Product;
    price: number;
    quantity: number;
    userId?: string;
    status: string;
    paymentType: string;
    orderAddress?: OrderAddress;
}

export interface PlaceOrderRequest {
    firstName: string;
    lastName: string;
    email: string;
    telephone: string;
    address: string;
    city: string;
    state: string;
    postalCode: string;
    paymentType: string;
}

export type OrderStatusName = 'Pending' | 'In Progress' | 'Completed' | 'Cancelled' | 'Shipped' | 'Delivered';

export interface OrderStatusEnum {
    id: number;
    name: OrderStatusName;
}
