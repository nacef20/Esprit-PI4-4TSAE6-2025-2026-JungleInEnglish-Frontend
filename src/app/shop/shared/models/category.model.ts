export interface Category {
    id: number;
    name: string;
    imageName?: string;
    isActive?: boolean;
}
export type CategoryCreate = Omit<Category, 'id'>;
export type CategoryUpdate = Partial<CategoryCreate>;
