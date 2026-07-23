import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router';
import { Button, Card, TextField, Input, Label, Chip, Dropdown, Spinner } from '@heroui/react';
import { getProduct, updateProduct, adjustStock } from '../../services/productApi';
import type { Product } from '../../types/Product';

const CATEGORIES = [
  'Electronics',
  'Clothing',
  'Home & Garden',
  'Sports',
  'Books',
  'Other'
];

interface EditFormData {
  name: string;
  description: string;
  category: string;
  price: number;
}

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [product, setProduct] = useState<Product | null>(null);
  const [editData, setEditData] = useState<EditFormData | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [stockAmount, setStockAmount] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isAdjusting, setIsAdjusting] = useState(false);

  const loadProductData = async () => {
    if (!id) return;

    setIsLoading(true);
    setError('');

    try {
      const data = await getProduct(id);
      setProduct(data);
    } catch (err) {
      setError('Unable to load product.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadProductData();
  }, [id]);

  const handleEdit = () => {
    if (!product) return;

    setEditData({
      name: product.name,
      description: product.description || '',
      category: product.category || '',
      price: product.price,
    });
    setIsEditing(true);
    setError('');
  };

  const handleCancelEdit = () => {
    setEditData(null);
    setIsEditing(false);
    setError('');
  };

  const handleUpdate = async () => {
    if (!id || !editData || isSaving) return;

    const trimmedName = editData.name.trim();
    const trimmedCategory = editData.category.trim();
    const parsedPrice = Number(editData.price);

    if (!trimmedName || !trimmedCategory || !Number.isFinite(parsedPrice) || parsedPrice < 0) {
      setError('Please provide a valid name, category, and non-negative price.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      const updated = await updateProduct(id, {
        name: trimmedName,
        category: trimmedCategory,
        description: editData.description.trim(),
        price: parsedPrice,
      });
      setProduct(updated);
      setIsEditing(false);
    } catch (err) {
      setError('Something went wrong. Please try again.');
      console.error(err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleStockAdjust = async () => {
    setError('');
    if (!id || isAdjusting) return;

    setIsAdjusting(true);

    try {
      const data = await adjustStock(id, stockAmount);
      setProduct(data);
      setStockAmount('');
    } catch (err) {
      try {
        const parsedError = JSON.parse((err as Error).message);
        setError(parsedError.message || 'Error');
      } catch {
        setError((err as Error).message || 'Error');
      }
    } finally {
      setIsAdjusting(false);
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

  return (
    <div className="max-w-2xl">
      <Button variant="ghost" onPress={() => navigate('/products')} className="mb-4">
        ← Back to products
      </Button>

      <Card className="mb-6">
        <Card.Header className="flex justify-between items-center">
          <Card.Title>Product Detail</Card.Title>
          <div className="flex items-center gap-2">
            <Chip size="sm" color={product.stock > 0 ? 'success' : 'danger'}>
              <Chip.Label>Stock: {product.stock}</Chip.Label>
            </Chip>
            {!isEditing && (
              <Button size="sm" variant="outline" onPress={handleEdit}>Edit</Button>
            )}
          </div>
        </Card.Header>
        <Card.Content className="flex flex-col gap-4">
          {isEditing ? (
            <>
              <TextField isRequired>
                <Label>Name</Label>
                <Input
                  value={editData?.name ?? ''}
                  onChange={(e) => setEditData((current) => current ? { ...current, name: e.target.value } : current)}
                />
              </TextField>

              <TextField isRequired>
                <Label>Category</Label>
                <Dropdown>
                  <Dropdown.Trigger>
                    <button className="flex items-center justify-between w-full h-10 px-3 py-2 border rounded-medium border-default-200 bg-white text-sm text-left outline-none cursor-pointer">
                      <span className="capitalize">{editData?.category?.toLowerCase() || 'Select Category'}</span>
                      <span className="text-default-400 text-xs">▼</span>
                    </button>
                  </Dropdown.Trigger>
                  <Dropdown.Popover>
                    <Dropdown.Menu onAction={(key) => setEditData((current) => current ? { ...current, category: String(key).toUpperCase() } : current)}>
                      {CATEGORIES.map((cat) => (
                        <Dropdown.Item key={cat} id={cat}>
                          {cat}
                        </Dropdown.Item>
                      ))}
                    </Dropdown.Menu>
                  </Dropdown.Popover>
                </Dropdown>
              </TextField>

              <TextField>
                <Label>Description</Label>
                <Input
                  value={editData?.description ?? ''}
                  onChange={(e) => setEditData((current) => current ? { ...current, description: e.target.value } : current)}
                />
              </TextField>

              <TextField isRequired>
                <Label>Price</Label>
                <Input
                  type="number"
                  min="0"
                  step="0.01"
                  value={String(editData?.price ?? '')}
                  onChange={(e) => setEditData((current) => current ? { ...current, price: Number(e.target.value) } : current)}
                />
              </TextField>
              {error && <p className="text-danger text-sm font-medium">{error}</p>}
              <div className="flex gap-2 pt-2">
                <Button variant="primary" onPress={handleUpdate} isDisabled={isSaving}>
                  {isSaving ? 'Saving...' : 'Save Changes'}
                </Button>
                <Button variant="outline" onPress={handleCancelEdit}>Cancel</Button>
              </div>
            </>
          ) : (
            <>
              <div>
                <p className="text-sm text-default-500">Name</p>
                <p className="font-medium">{product.name}</p>
              </div>
              <div>
                <p className="text-sm text-default-500">Category</p>
                <p className="capitalize">{product.category?.toLowerCase() || '—'}</p>
              </div>
              <div>
                <p className="text-sm text-default-500">Description</p>
                <p>{product.description || '—'}</p>
              </div>
              <div>
                <p className="text-sm text-default-500">Price</p>
                <p className="font-medium">${product.price?.toFixed(2)}</p>
              </div>
            </>
          )}
        </Card.Content>
      </Card>

      <Card>
        <Card.Header>
          <Card.Title>Adjust Stock</Card.Title>
        </Card.Header>
        <Card.Content>
          <div className="flex gap-3 items-end">
            <TextField>
              <Label>Amount</Label>
              <Input
                placeholder="e.g. 5 or -3"
                value={stockAmount}
                onChange={(e) => setStockAmount(e.target.value)}
              />
            </TextField>
            <Button variant="primary" onPress={handleStockAdjust} isDisabled={isAdjusting}>
              {isAdjusting ? 'Adjusting...' : 'Adjust'}
            </Button>
          </div>
          {error && <p className="text-danger text-sm font-medium mt-2">{error}</p>}
        </Card.Content>
      </Card>
    </div>
  );
}