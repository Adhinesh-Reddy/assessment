import { useLocation, useNavigate } from 'react-router';
import { ListBox } from '@heroui/react';

export default function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();

  // Highlight based on current URL path
  const currentKey = location.pathname.startsWith('/products') 
    ? 'products' 
    : location.pathname.startsWith('/store') 
      ? 'store' 
      : location.pathname.startsWith('/orders') 
        ? 'orders' 
        : '';

  return (
    <aside className="w-56 border-r border-default-200 bg-default-50 min-h-0 shrink-0">
      <div className="p-3 flex flex-col gap-4">
        {/* Back Office (IMS) Section */}
        <div>
          <p className="text-xs font-semibold text-default-400 uppercase tracking-wider px-2 mb-2">
            Back Office (IMS)
          </p>
          <ListBox
            aria-label="IMS Navigation"
            selectionMode="single"
            selectedKeys={new Set([currentKey])}
          >
            <ListBox.Item 
              key="products" 
              id="products" 
              onPress={() => navigate('/products')}
            >
              Products
            </ListBox.Item>
            
          </ListBox>
        </div>

        {/* Storefront (OMS) Section */}
        <div>
          <p className="text-xs font-semibold text-default-400 uppercase tracking-wider px-2 mb-2">
            Storefront (OMS)
          </p>
          <ListBox
            aria-label="OMS Navigation"
            selectionMode="single"
            selectedKeys={new Set([currentKey])}
          >
            <ListBox.Item 
              key="store" 
              id="store" 
              onPress={() => navigate('/store')}
            >
              Browse Catalog
            </ListBox.Item>
            <ListBox.Item 
              key="orders" 
              id="orders" 
              onPress={() => navigate('/orders')}
            >
              Order History
            </ListBox.Item>
          </ListBox>
        </div>
      </div>
    </aside>
  );
}