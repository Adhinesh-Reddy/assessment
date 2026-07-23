import { Routes, Route, Navigate } from 'react-router';
import AppLayout from '../components/layout/AppLayout';

// IMS Features
import ProductListPage from '../features/products/ProductListPage';
import ProductDetailPage from '../features/products/ProductDetailPage';

// OMS Features
import StorefrontCatalog from '../features/store/StorefrontCatalog';
import StorefrontDetail from '../features/store/StorefrontDetail';
import OrderHistory from '../features/orders/OrderHistory';
import OrderDetail from '../features/orders/OrderDetail';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/products" replace />} />

      <Route element={<AppLayout />}>
        {/* Inventory Management System (IMS) */}
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />

        {/* Order Management System (OMS) */}
        <Route path="/store" element={<StorefrontCatalog />} />
        <Route path="/store/products/:id" element={<StorefrontDetail />} />
        <Route path="/orders" element={<OrderHistory />} />
        <Route path="/orders/:id" element={<OrderDetail />} />
      </Route>

      <Route path="*" element={<Navigate to="/products" replace />} />
    </Routes>
  );
}