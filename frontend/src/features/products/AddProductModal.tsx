import { useState } from 'react';
import { Modal, Button, TextField, Input, Label, Dropdown } from '@heroui/react';
import { createProduct } from '../../services/productApi';

const CATEGORIES = [
  'Electronics',
  'Clothing',
  'Home & Garden',
  'Sports',
  'Books',
  'Other'
];

interface AddProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function AddProductModal({ isOpen, onClose, onCreated }: AddProductModalProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('Electronics');
  const [price, setPrice] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async () => {
    const trimmedName = name.trim();
    const trimmedCategory = category.trim();
    const parsedPrice = Number(price);

    if (!trimmedName || !trimmedCategory || !Number.isFinite(parsedPrice) || parsedPrice < 0) {
      setError('Please provide a valid name, category, and non-negative price.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      await createProduct({
        name: trimmedName,
        description: description.trim(),
        category: trimmedCategory,
        price: parsedPrice,
        stock: 0,
      });
      setName('');
      setDescription('');
      setCategory('Electronics');
      setPrice('');
      onCreated();
    } catch (err) {
      setError('Something went wrong. Please try again.');
      console.error(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal>
      <Modal.Backdrop isOpen={isOpen} onOpenChange={(open: boolean) => { if (!open) onClose(); }}>
        <Modal.Container size="lg">
          <Modal.Dialog>
            <Modal.CloseTrigger />
            <Modal.Header>
              <Modal.Heading>Add Product</Modal.Heading>
            </Modal.Header>
            <Modal.Body>
              <div className="flex flex-col gap-4">
                <TextField isRequired>
                  <Label>Name</Label>
                  <Input value={name} onChange={(e) => setName(e.target.value)} />
                </TextField>

                <TextField isRequired>
                  <Label>Category</Label>
                  <Dropdown>
                    <Dropdown.Trigger>
                      <button className="flex items-center justify-between w-full h-10 px-3 py-2 border rounded-medium border-default-200 bg-white text-sm text-left outline-none cursor-pointer">
                        <span>{category}</span>
                        <span className="text-default-400 text-xs">▼</span>
                      </button>
                    </Dropdown.Trigger>
                    <Dropdown.Popover>
                      <Dropdown.Menu onAction={(key) => setCategory(String(key))}>
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
                  <Input value={description} onChange={(e) => setDescription(e.target.value)} />
                </TextField>

                <TextField isRequired>
                  <Label>Price</Label>
                  <Input type="number" min="0" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} />
                </TextField>
                {error && <p className="text-danger text-sm font-medium">{error}</p>}
              </div>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="outline" onPress={onClose}>Cancel</Button>
              <Button variant="primary" onPress={handleSubmit} isDisabled={isSubmitting}>
                {isSubmitting ? 'Saving...' : 'Save'}
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}