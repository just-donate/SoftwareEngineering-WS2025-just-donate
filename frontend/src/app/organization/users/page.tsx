'use client';

import { useEffect, useMemo, useState, FormEvent } from 'react';
import axiosInstance from '../api/axiosInstance';
import withAuth from '../api/RequiresAuth';

interface User {
  email: string;
  role: string;
  active: boolean;
  orgId: string;
}

type SortDirection = 'asc' | 'desc';
type ConfirmationAction = 'toggleActive' | 'delete';

interface ConfirmationData {
  action: ConfirmationAction;
  user: User;
}

function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [filter, setFilter] = useState('');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
  const [error, setError] = useState('');

  // Modal state for adding a new user:
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newUserEmail, setNewUserEmail] = useState('');
  const [newUserPassword, setNewUserPassword] = useState('');
  const [newUserRole, setNewUserRole] = useState('USER');
  const [registerError, setRegisterError] = useState('');

  // Confirmation modal state for deactivation/activation and deletion:
  const [confirmationData, setConfirmationData] =
    useState<ConfirmationData | null>(null);

  // Modal state for changing password:
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [selectedUserForPassword, setSelectedUserForPassword] =
    useState<User | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await axiosInstance.get('/user/list');
      setUsers(response.data);
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Failed to fetch users.');
    }
  };

  // Filter and sort users by email.
  const filteredAndSortedUsers = useMemo(() => {
    const filtered = users.filter((user) =>
      user.email.toLowerCase().includes(filter.toLowerCase()),
    );
    return filtered.sort((a, b) => {
      if (a.email < b.email) return sortDirection === 'asc' ? -1 : 1;
      if (a.email > b.email) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }, [users, filter, sortDirection]);

  const totalItems = filteredAndSortedUsers.length;
  const totalPages = Math.ceil(totalItems / itemsPerPage);
  const currentUsers = filteredAndSortedUsers.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage,
  );

  const toggleSortDirection = () => {
    setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
  };

  // Opens the confirmation modal for toggle active or deletion.
  const openConfirmationModal = (action: ConfirmationAction, user: User) => {
    setConfirmationData({ action, user });
  };

  // Opens the change password modal.
  const openPasswordModal = (user: User) => {
    setSelectedUserForPassword(user);
    setIsPasswordModalOpen(true);
    setNewPassword('');
    setPasswordError('');
  };

  const handleConfirmAction = async () => {
    if (!confirmationData) return;
    const { action, user } = confirmationData;
    try {
      if (action === 'toggleActive') {
        const updatedPayload = { active: !user.active };
        const response = await axiosInstance.put(
          `/user/${user.email}`,
          updatedPayload,
        );
        setUsers((prev) =>
          prev.map((u) =>
            u.email === user.email ? { ...u, active: response.data.active } : u,
          ),
        );
      } else if (action === 'delete') {
        await axiosInstance.delete(`/user/${user.email}`);
        setUsers((prev) => prev.filter((u) => u.email !== user.email));
      }
    } catch (err) {
      console.error(`Error during ${action} action:`, err);
      alert(`Failed to complete action for ${user.email}`);
    } finally {
      setConfirmationData(null);
    }
  };

  const handleCancelConfirmation = () => {
    setConfirmationData(null);
  };

  const handleChangePasswordSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!selectedUserForPassword) return;
    try {
      await axiosInstance.put(`/user/${selectedUserForPassword.email}`, {
        newPassword,
      });
      alert(
        `Password change request sent for ${selectedUserForPassword.email}.`,
      );
      setIsPasswordModalOpen(false);
      setSelectedUserForPassword(null);
      setNewPassword('');
    } catch (err) {
      console.error('Error changing password:', err);
      setPasswordError(
        `Failed to change password for ${selectedUserForPassword.email}.`,
      );
    }
  };

  const handleAddUser = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setRegisterError('');
    try {
      const payload = {
        email: newUserEmail,
        password: newUserPassword,
        orgId: '591671920',
        role: newUserRole,
      };
      await axiosInstance.post('/user/register', payload);
      await fetchUsers();
      setIsAddModalOpen(false);
      setNewUserEmail('');
      setNewUserPassword('');
      setNewUserRole('USER');
    } catch (err) {
      console.error('Error registering user:', err);
      setRegisterError('Failed to register user.');
    }
  };

  const goToPage = (page: number) => {
    if (page < 1 || page > totalPages) return;
    setCurrentPage(page);
  };

  return (
    <div className="relative container mx-auto px-4 py-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">User Management</h1>
        <button
          onClick={() => {
            setIsAddModalOpen(true);
            setRegisterError('');
          }}
          className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded shadow"
        >
          Add User
        </button>
      </div>

      {error && <div className="text-red-600 mb-4">{error}</div>}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Filter by email..."
          value={filter}
          onChange={(e) => {
            setFilter(e.target.value);
            setCurrentPage(1);
          }}
          className="border rounded-md px-3 py-2 w-full max-w-md shadow-sm focus:ring focus:outline-none"
        />
      </div>

      <div className="overflow-x-auto bg-white shadow rounded-lg">
        <table className="min-w-full table-auto divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer whitespace-nowrap"
                onClick={toggleSortDirection}
              >
                Email {sortDirection === 'asc' ? '▲' : '▼'}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                Role
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {currentUsers.length > 0 ? (
              currentUsers.map((user) => (
                <tr key={user.email} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {user.email}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                    {user.role}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {user.active ? (
                      <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                        Active
                      </span>
                    ) : (
                      <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                        Inactive
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                    <button
                      onClick={() =>
                        openConfirmationModal('toggleActive', user)
                      }
                      className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-xs"
                    >
                      {user.active ? 'Deactivate' : 'Activate'}
                    </button>
                    <button
                      onClick={() => openPasswordModal(user)}
                      className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded text-xs"
                    >
                      Change Password
                    </button>
                    <button
                      onClick={() => openConfirmationModal('delete', user)}
                      className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-xs"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={4} className="px-6 py-4 text-center text-gray-500">
                  No users found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Controls */}
      <div className="mt-4 flex items-center justify-between">
        <div>
          <span className="text-sm text-gray-700">
            Showing {(currentPage - 1) * itemsPerPage + 1} to{' '}
            {Math.min(currentPage * itemsPerPage, totalItems)} of {totalItems}{' '}
            users
          </span>
        </div>
        <div className="flex space-x-2">
          <button
            onClick={() => goToPage(currentPage - 1)}
            disabled={currentPage === 1}
            className={`px-3 py-1 rounded border text-sm ${
              currentPage === 1
                ? 'text-gray-400 border-gray-300 cursor-not-allowed'
                : 'text-gray-700 border-gray-700 hover:bg-gray-100'
            }`}
          >
            Prev
          </button>
          {Array.from({ length: totalPages }, (_, idx) => idx + 1).map(
            (page) => (
              <button
                key={page}
                onClick={() => goToPage(page)}
                className={`px-3 py-1 rounded border text-sm ${
                  currentPage === page
                    ? 'bg-blue-500 text-white border-blue-500'
                    : 'text-gray-700 border-gray-700 hover:bg-gray-100'
                }`}
              >
                {page}
              </button>
            ),
          )}
          <button
            onClick={() => goToPage(currentPage + 1)}
            disabled={currentPage === totalPages}
            className={`px-3 py-1 rounded border text-sm ${
              currentPage === totalPages
                ? 'text-gray-400 border-gray-300 cursor-not-allowed'
                : 'text-gray-700 border-gray-700 hover:bg-gray-100'
            }`}
          >
            Next
          </button>
        </div>
      </div>

      {/* Add User Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">Register New User</h2>
            {registerError && (
              <div className="text-red-600 mb-2">{registerError}</div>
            )}
            <form onSubmit={handleAddUser}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700">
                  Email
                </label>
                <input
                  type="email"
                  value={newUserEmail}
                  onChange={(e) => setNewUserEmail(e.target.value)}
                  required
                  className="mt-1 block w-full border rounded-md px-3 py-2 shadow-sm focus:ring focus:outline-none"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700">
                  Password
                </label>
                <input
                  type="password"
                  value={newUserPassword}
                  onChange={(e) => setNewUserPassword(e.target.value)}
                  required
                  className="mt-1 block w-full border rounded-md px-3 py-2 shadow-sm focus:ring focus:outline-none"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700">
                  Role
                </label>
                <select
                  value={newUserRole}
                  onChange={(e) => setNewUserRole(e.target.value)}
                  className="mt-1 block w-full border rounded-md px-3 py-2 shadow-sm focus:ring focus:outline-none"
                >
                  <option value="ADMIN">ADMIN</option>
                  <option value="USER">USER</option>
                  <option value="GUEST">GUEST</option>
                </select>
              </div>
              <div className="flex justify-end space-x-4">
                <button
                  type="button"
                  onClick={() => {
                    setIsAddModalOpen(false);
                    setRegisterError('');
                    setNewUserEmail('');
                    setNewUserPassword('');
                    setNewUserRole('USER');
                  }}
                  className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded shadow text-sm"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded shadow text-sm"
                >
                  Register
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Confirmation Modal for toggle active or delete */}
      {confirmationData && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
            {confirmationData.action === 'toggleActive' ? (
              <>
                <h2 className="text-xl font-bold mb-4">
                  {confirmationData.user.active ? 'Deactivate' : 'Activate'}{' '}
                  User
                </h2>
                <p className="mb-6 text-gray-700">
                  {confirmationData.user.active
                    ? `Are you sure you want to deactivate ${confirmationData.user.email}?`
                    : `Are you sure you want to activate ${confirmationData.user.email}?`}
                </p>
              </>
            ) : (
              <>
                <h2 className="text-xl font-bold mb-4">Delete User</h2>
                <p className="mb-6 text-gray-700">
                  Are you sure you want to delete {confirmationData.user.email}?
                  This action cannot be undone.
                </p>
              </>
            )}
            <div className="flex justify-end space-x-4">
              <button
                onClick={handleCancelConfirmation}
                className="bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded shadow text-sm"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmAction}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded shadow text-sm"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Change Password Modal */}
      {isPasswordModalOpen && selectedUserForPassword && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">
              Change Password for {selectedUserForPassword.email}
            </h2>
            {passwordError && (
              <div className="text-red-600 mb-2">{passwordError}</div>
            )}
            <form onSubmit={handleChangePasswordSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700">
                  New Password
                </label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  className="mt-1 block w-full border rounded-md px-3 py-2 shadow-sm focus:ring focus:outline-none"
                />
              </div>
              <div className="flex justify-end space-x-4">
                <button
                  type="button"
                  onClick={() => {
                    setIsPasswordModalOpen(false);
                    setSelectedUserForPassword(null);
                    setNewPassword('');
                    setPasswordError('');
                  }}
                  className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded shadow text-sm"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded shadow text-sm"
                >
                  Change Password
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export const dynamic = 'force-dynamic';
export default withAuth(UsersPage);
