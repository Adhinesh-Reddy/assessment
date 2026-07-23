import { useState } from 'react';
import { Outlet } from 'react-router';
import TopBar from './TopBar';
import Sidebar from './Sidebar';
import type { User } from '../../types/User';

const USERS: User[] = [
  { id: 1, name: 'Alice Martin', email: 'alice@marketnode.com' },
  { id: 2, name: 'Bob Chen', email: 'bob@marketnode.com' },
  { id: 3, name: 'Carol Smith', email: 'carol@marketnode.com' },
];

export default function AppLayout() {
  const [currentUser, setCurrentUser] = useState<User>(USERS[0]);

  return (
    <div className="flex flex-col h-screen">
      <TopBar currentUser={currentUser} users={USERS} onUserChange={setCurrentUser} />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-y-auto bg-default-100 p-6">
          <Outlet context={{ currentUser }} />
        </main>
      </div>
    </div>
  );
}