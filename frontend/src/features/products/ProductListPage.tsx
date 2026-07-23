import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Table, Button, Chip, Spinner } from '@heroui/react';
import AddProductModal from './AddProductModal';
import { getProducts } from '../../services/productApi';
import type { Product } from '../../types/Product';

export default function ProductListPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [isAddOpen, setAddOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const loadProducts = async () => {
    setIsLoading(true);
    setError('');

    try {
      const data = await getProducts();
      setProducts(data);
    } catch (err) {
      setError('Unable to load products.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadProducts();
  }, []);

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-semibold">Products</h1>
        <Button variant="primary" onPress={() => setAddOpen(true)}>
          + Add Product
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center gap-2 text-default-500">
          <Spinner size="sm" />
          <span>Loading products...</span>
        </div>
      ) : error ? (
        <p className="text-danger text-sm font-medium">{error}</p>
      ) : products.length === 0 ? (
        <p className="text-default-500">No products found.</p>
      ) : (
        <Table>
          <Table.ScrollContainer>
            <Table.Content aria-label="Products table">
              <Table.Header>
                <Table.Column isRowHeader>ID</Table.Column>
                <Table.Column>NAME</Table.Column>
                <Table.Column>CATEGORY</Table.Column>
                <Table.Column>DESCRIPTION</Table.Column>
                <Table.Column>PRICE</Table.Column>
                <Table.Column>STOCK</Table.Column>
              </Table.Header>
              <Table.Body>
                {products.map((p) => (
                  <Table.Row key={p.id} className="cursor-pointer" onAction={() => navigate(`/products/${p.id}`)}>
                    <Table.Cell>{p.id}</Table.Cell>
                    <Table.Cell className="font-medium">{p.name}</Table.Cell>
                    <Table.Cell>{p.category || '—'}</Table.Cell>
                    <Table.Cell className="text-default-500">{p.description || '—'}</Table.Cell>
                    <Table.Cell>${p.price?.toFixed(2)}</Table.Cell>
                    <Table.Cell>
                      <Chip size="sm" color={p.stock > 0 ? 'success' : 'danger'}>
                        <Chip.Label>{p.stock}</Chip.Label>
                      </Chip>
                    </Table.Cell>
                  </Table.Row>
                ))}
              </Table.Body>
            </Table.Content>
          </Table.ScrollContainer>
        </Table>
      )}

      <AddProductModal
        isOpen={isAddOpen}
        onClose={() => setAddOpen(false)}
        onCreated={() => {
          setAddOpen(false);
          void loadProducts();
        }}
      />
    </div>
  );
}