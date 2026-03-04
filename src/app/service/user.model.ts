export interface User {
  id: number;
  name: string;
  email: string;
  password?: string;
  role?: string;
  // Add other fields as needed based on your backend User entity
} 