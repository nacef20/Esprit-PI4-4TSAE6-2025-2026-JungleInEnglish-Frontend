import type { Product } from './product.model';


export interface Cart {
    id: number;
    product: Product;
    quantity: number;
    totalPrice?: number;
    totalOrderPrice?: number;
    userId?: string;
}
