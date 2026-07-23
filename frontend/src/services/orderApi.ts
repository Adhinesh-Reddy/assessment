import type { Order } from '../types/Order';

export interface PlaceOrderPayload {
  userId: number;
  productId: number;
  quantity: number;
}

export async function placeOrder(payload: PlaceOrderPayload): Promise<Order> {
  const res = await fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || 'Failed to place order due to insufficient stock or invalid criteria');
  }
  return res.json();
}

export async function getOrders(): Promise<Order[]> {
  const res = await fetch('/api/orders');
  if (!res.ok) throw new Error('Failed to fetch order history');
  return res.json();
}

export async function getOrder(id: number | string): Promise<Order> {
  const res = await fetch(`/api/orders/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch details for order ${id}`);
  return res.json();
}

export async function cancelOrder(id: number | string): Promise<Order> {
  const res = await fetch(`/api/orders/${id}/cancel`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error(`Failed to cancel order ${id}`);
  return res.json();
}