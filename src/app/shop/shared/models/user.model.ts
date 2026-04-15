/**
 * Modèle utilisateur aligné sur l'entité JPA UserDtls du backend.
 */
export interface User {
  id: number;
  name: string;
  email: string;
  mobileNumber?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  profileImage?: string;
  role?: string;
  isEnable?: boolean;
  accountNonLocked?: boolean;
}

/**
 * DTO pour la création d'un utilisateur (POST /api/users).
 */
export interface UserCreateDto {
  name: string;
  email: string;
  password: string;
}

/**
 * DTO pour la mise à jour d'un utilisateur (PUT /api/users/{id}).
 */
export type UserUpdateDto = Partial<Omit<User, 'id' | 'role' | 'isEnable' | 'accountNonLocked'>>;

