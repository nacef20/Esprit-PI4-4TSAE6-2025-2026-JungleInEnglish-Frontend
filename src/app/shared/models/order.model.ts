import type { Product } from './product.model';
import type { User } from './user.model';

export interface OrderAddress {
    id?: number;
    firstName: string;
    lastName: string;
    email: string;
    mobileNo: string;
    address: string;
    city: string;
    state: string;
    pincode: string;
}

export interface ProductOrder {
    id: number;
    orderId: string;
    orderDate: string;
    product: Product;
    price: number;
    quantity: number;
    user?: User;
    status: string;
    paymentType: string;
    orderAddress?: OrderAddress;
}

export interface PlaceOrderRequest {
    userId: number;
    firstName: string;
    lastName: string;
    email: string;
    mobileNo: string;
    address: string;
    city: string;
    state: string;
    pincode: string;
    paymentType: string;
}

export type OrderStatusName = 'Pending' | 'In Progress' | 'Completed' | 'Cancelled' | 'Shipped' | 'Delivered';

export interface OrderStatusEnum {
    id: number;
    name: OrderStatusName;
}
