import type { Product } from '../types/Product';

export interface CreateProductPayload {
  name: string;
  category: string;
  description?: string;
  price: number;
  stock: number;
}

export interface UpdateProductPayload {
  name?: string;
  category?: string;
  description?: string;
  price?: number;
  stock?: number;
}

// --- Back Office (IMS) Endpoints ---
export async function getProducts(): Promise<Product[]> {
  const res = await fetch('/api/products');
  if (!res.ok) throw new Error('Failed to fetch product catalog');
  return res.json();
}

export async function getProduct(id: number | string): Promise<Product> {
  const res = await fetch(`/api/products/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch details for product ${id}`);
  return res.json();
}

export async function createProduct(payload: CreateProductPayload): Promise<Product> {
  const res = await fetch('/api/products', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error('Failed to create new product');
  return res.json();
}

export async function updateProduct(id: number | string, payload: UpdateProductPayload): Promise<Product> {
  const res = await fetch(`/api/products/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(`Failed to update product ${id}`);
  return res.json();
}

export async function adjustStock(id: number | string, amount: number | string): Promise<Product> {
  const res = await fetch(`/api/products/${id}/stock?amount=${amount}`, {
    method: 'PATCH',
  });
  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || `Failed to adjust stock for product ${id}`);
  }
  return res.json();
}

// --- Storefront (OMS) Endpoints ---
export async function getStoreProducts(): Promise<Product[]> {
  const res = await fetch('/api/store/products');
  if (!res.ok) throw new Error('Failed to fetch storefront catalog');
  return res.json();
}

export async function getStoreProduct(id: number | string): Promise<Product> {
  const res = await fetch(`/api/store/products/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch storefront details for product ${id}`);
  return res.json();
}