export interface Product {
    id: number;
    title: string;
    description?: string;
    category?: string;
    price: number;
    stock: number;
    image?: string;
    discount: number;
    discountPrice?: number;
    isActive?: boolean;
}
export type ProductCreate = Omit<Product, 'id' | 'discountPrice'>;
export type ProductUpdate = Partial<ProductCreate>;
