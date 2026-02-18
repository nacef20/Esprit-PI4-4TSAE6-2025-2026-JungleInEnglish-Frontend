import type { Product } from './product.model';
import type { User } from './user.model';

export interface Cart {
    id: number;
    product: Product;
    quantity: number;
    totalPrice?: number;
    totalOrderPrice?: number;
    user?: User;
}
