import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router';
import { Button, Card, Chip, Spinner } from '@heroui/react';
import { getOrder, cancelOrder } from '../../services/orderApi';
import type { Order } from '../../types/Order';

export default function OrderDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [isCancelling, setIsCancelling] = useState(false);

  const loadOrderData = async () => {
    if (!id) return;

    setIsLoading(true);
    setError('');

    try {
      const data = await getOrder(id);
      setOrder(data);
    } catch (err) {
      setError('Unable to load order.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadOrderData();
  }, [id]);

  const handleCancelOrder = async () => {
    if (!id || isCancelling) return;

    setIsCancelling(true);
    setError('');

    try {
      await cancelOrder(id);
      await loadOrderData();
    } catch (err) {
      setError('Something went wrong. Please try again.');
      console.error(err);
    } finally {
      setIsCancelling(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center gap-2 text-default-500">
        <Spinner size="sm" />
        <span>Loading order...</span>
      </div>
    );
  }

  if (!order) {
    return <p className="text-default-500">No order found.</p>;
  }

  return (
    <div className="max-w-2xl">
      <Button variant="ghost" onPress={() => navigate('/orders')} className="mb-4">
        ← Back to History
      </Button>

      <Card>
        <Card.Header className="flex justify-between items-center">
          <Card.Title>Order Details #{order.id}</Card.Title>
          <div className="flex items-center gap-2">
            <Chip color={order.status === 'CREATED' ? 'success' : 'danger'}>
              <Chip.Label>{order.status}</Chip.Label>
            </Chip>
            {order.status === 'CREATED' && (
              <Button size="sm" variant="danger" onPress={handleCancelOrder} isDisabled={isCancelling}>
                {isCancelling ? 'Cancelling...' : 'Cancel Order'}
              </Button>
            )}
          </div>
        </Card.Header>
        <Card.Content className="flex flex-col gap-4">
          {error && <p className="text-danger text-sm font-medium">{error}</p>}
          <div>
            <p className="text-sm text-default-500">Product Name</p>
            <p className="font-medium text-lg">{order.productName}</p>
          </div>
          <div>
            <p className="text-sm text-default-500">Quantity</p>
            <p className="font-medium">{order.quantity}</p>
          </div>
          <div>
            <p className="text-sm text-default-500">Unit Price</p>
            <p>${order.unitPrice?.toFixed(2)}</p>
          </div>
          <div className="border-t border-default-200 pt-3">
            <p className="text-sm text-default-500">Total</p>
            <p className="font-semibold text-xl text-primary">${order.totalAmount?.toFixed(2)}</p>
          </div>
        </Card.Content>
      </Card>
    </div>
  );
}