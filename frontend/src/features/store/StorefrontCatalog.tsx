import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Table, Chip, Spinner } from '@heroui/react';
import { getStoreProducts } from '../../services/productApi';
import type { Product } from '../../types/Product';

export default function StorefrontCatalog() {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const loadProducts = async () => {
      setIsLoading(true);
      setError('');

      try {
        const data = await getStoreProducts();
        setProducts(data);
      } catch (err) {
        setError('Unable to load data.');
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    void loadProducts();
  }, []);

  return (
    <div>
      <div className="mb-4">
        <h1 className="text-2xl font-semibold">Storefront Catalog</h1>
      </div>

      {isLoading ? (
        <div className="flex items-center gap-2 text-default-500">
          <Spinner size="sm" />
          <span>Loading catalog...</span>
        </div>
      ) : error ? (
        <p className="text-danger text-sm font-medium">{error}</p>
      ) : products.length === 0 ? (
        <p className="text-default-500">No products found.</p>
      ) : (
        <Table>
          <Table.ScrollContainer>
            <Table.Content aria-label="Store catalog table">
              <Table.Header>
                <Table.Column isRowHeader>NAME</Table.Column>
                <Table.Column>CATEGORY</Table.Column>
                <Table.Column>PRICE</Table.Column>
                <Table.Column>AVAILABILITY</Table.Column>
              </Table.Header>
              <Table.Body>
                {products.map((p) => (
                  <Table.Row key={p.id} className="cursor-pointer" onAction={() => navigate(`/store/products/${p.id}`)}>
                    <Table.Cell className="font-medium">{p.name}</Table.Cell>
                    <Table.Cell>{p.category || '—'}</Table.Cell>
                    <Table.Cell>${p.price?.toFixed(2)}</Table.Cell>
                    <Table.Cell>
                      <Chip size="sm" color={p.inStock ? 'success' : 'danger'}>
                        <Chip.Label>{p.inStock ? 'In Stock' : 'Out of Stock'}</Chip.Label>
                      </Chip>
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