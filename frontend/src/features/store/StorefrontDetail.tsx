import { useEffect, useState } from 'react';
import { useParams, useNavigate, useOutletContext } from 'react-router';
import { Button, Card, TextField, Input, Label, Chip, Spinner } from '@heroui/react';
import { getStoreProduct } from '../../services/productApi';
import { placeOrder } from '../../services/orderApi';
import type { Product } from '../../types/Product';
import type { User } from '../../types/User';

export default function StorefrontDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { currentUser } = useOutletContext<{ currentUser: User }>();

  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState('1');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const loadProduct = async () => {
      if (!id) return;

      setIsLoading(true);
      setError('');

      try {
        const data = await getStoreProduct(id);
        setProduct(data);
      } catch (err) {
        setError('Unable to load data.');
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    void loadProduct();
  }, [id]);

  const handlePlaceOrder = async () => {
    setError('');
    const qty = parseInt(quantity, 10);

    if (Number.isNaN(qty) || qty < 1) {
      setError('Quantity must be at least 1.');
      return;
    }

    if (!product || isSubmitting) return;

    setIsSubmitting(true);

    try {
      await placeOrder({
        userId: currentUser.id,
        productId: product.id,
        quantity: qty,
      });
      navigate('/orders');
    } catch (err) {
      try {
        const parsedError = JSON.parse((err as Error).message);

        if (parsedError.errors && parsedError.errors.quantity) {
          setError(parsedError.errors.quantity);
        } else {
          setError(parsedError.message || 'Failed to place order.');
        }
      } catch {
        setError((err as Error).message || 'Failed to place order.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center gap-2 text-default-500">
        <Spinner size="sm" />
        <span>Loading product...</span>
      </div>
    );
  }

  if (!product) {
    return <p className="text-default-500">No product found.</p>;
  }

  const isInStock = product.inStock;

  return (
    <div className="max-w-2xl">
      <Button variant="ghost" onPress={() => navigate('/store')} className="mb-4">
        ← Back to Catalog
      </Button>

      <Card className="mb-6">
        <Card.Header className="flex justify-between items-center">
          <Card.Title>Product Detail</Card.Title>
          <Chip size="sm" color={isInStock ? 'success' : 'danger'}>
            <Chip.Label>{isInStock ? 'In Stock' : 'Out of Stock'}</Chip.Label>
          </Chip>
        </Card.Header>
        <Card.Content className="flex flex-col gap-4">
          <div>
            <p className="text-sm text-default-500">Name</p>
            <p className="font-medium text-lg">{product.name}</p>
          </div>
          <div>
            <p className="text-sm text-default-500">Description</p>
            <p>{product.description || '—'}</p>
          </div>
          <div>
            <p className="text-sm text-default-500">Price</p>
            <p className="font-medium text-lg">${product.price?.toFixed(2)}</p>
          </div>
        </Card.Content>
      </Card>

      <Card>
        <Card.Header>
          <Card.Title>Purchase Options</Card.Title>
        </Card.Header>
        <Card.Content className="flex flex-col gap-3">
          <div className="flex gap-3 items-end">
            <TextField>
              <Label>Quantity</Label>
              <Input
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                disabled={!isInStock}
              />
            </TextField>
            <Button variant="primary" onPress={handlePlaceOrder} isDisabled={!isInStock || isSubmitting}>
              {isSubmitting ? 'Placing...' : 'Place Order'}
            </Button>
          </div>
          {error && <p className="text-danger text-sm font-medium">{error}</p>}
        </Card.Content>
      </Card>
    </div>
  );
}