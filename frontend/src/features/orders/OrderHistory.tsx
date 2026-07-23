import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Table, Chip, Spinner } from '@heroui/react';
import { getOrders } from '../../services/orderApi';
import type { Order } from '../../types/Order';

export default function OrderHistory() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const loadOrders = async () => {
      setIsLoading(true);
      setError('');

      try {
        const data = await getOrders();
        setOrders(data);
      } catch (err) {
        setError('Unable to load data.');
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    void loadOrders();
  }, []);

  return (
    <div>
      <div className="mb-4">
        <h1 className="text-2xl font-semibold">Order History</h1>
      </div>

      {isLoading ? (
        <div className="flex items-center gap-2 text-default-500">
          <Spinner size="sm" />
          <span>Loading orders...</span>
        </div>
      ) : error ? (
        <p className="text-danger text-sm font-medium">{error}</p>
      ) : orders.length === 0 ? (
        <p className="text-default-500">No orders found.</p>
      ) : (
        <Table>
          <Table.ScrollContainer>
            <Table.Content aria-label="Order history table">
              <Table.Header>
                <Table.Column isRowHeader>ORDER ID</Table.Column>
                <Table.Column>PRODUCT</Table.Column>
                <Table.Column>QUANTITY</Table.Column>
                <Table.Column>TOTAL</Table.Column>
                <Table.Column>STATUS</Table.Column>
                <Table.Column>DATE</Table.Column>
              </Table.Header>
              <Table.Body>
                {orders.map((o) => (
                  <Table.Row key={o.id} className="cursor-pointer" onAction={() => navigate(`/orders/${o.id}`)}>
                    <Table.Cell>{o.id}</Table.Cell>
                    <Table.Cell className="font-medium">{o.productName}</Table.Cell>
                    <Table.Cell>{o.quantity}</Table.Cell>
                    <Table.Cell>${o.totalAmount?.toFixed(2)}</Table.Cell>
                    <Table.Cell>
                      <Chip size="sm" color={o.status === 'CREATED' ? 'success' : 'danger'}>
                        <Chip.Label>{o.status}</Chip.Label>
                      </Chip>
                    </Table.Cell>
                    <Table.Cell className="text-default-400 text-sm">
                      {o.date || '—'}
                    </Table.Cell>
                  </Table.Row>
                ))}
              </Table.Body>
            </Table.Content>
          </Table.ScrollContainer>
        </Table>
      )}
    </div>
  );
}