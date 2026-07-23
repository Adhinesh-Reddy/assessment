export interface Order {
  id: number;
  productName: string;
  quantity: number;
  totalAmount: number;
  unitPrice: number;
  status: string;
  date?: string;
}
