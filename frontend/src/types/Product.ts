export interface Product {
  id: number;
  name: string;
  category: string;
  description?: string | null;
  price: number;
  stock: number;
  inStock?: boolean;
}
