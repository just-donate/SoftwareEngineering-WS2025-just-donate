'use client';

import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
} from '@/components/organization/ui/card';
import { Button } from '@/components/organization/ui/button';
import Link from 'next/link';
import { useTheme } from '@/contexts/ThemeContext';
import withAuth from '../api/RequiresAuth';

function DashboardPage() {
  const { theme } = useTheme();

  return (
    <div>
      <h1 className={`text-2xl font-bold mb-4 ${theme.text}`}>
        Organization Dashboard
      </h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Card className={theme.card}>
          <CardHeader>
            <CardTitle className={theme.text}>Earmarkings</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/earmarkings">
              <Button className={theme.primary}>Manage Earmarkings</Button>
            </Link>
          </CardContent>
        </Card>
        <Card className={theme.card}>
          <CardHeader>
            <CardTitle className={theme.text}>Bank Accounts</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/bank-accounts">
              <Button className={theme.primary}>Manage Bank Accounts</Button>
            </Link>
          </CardContent>
        </Card>
        <Card className={theme.card}>
          <CardHeader>
            <CardTitle className={theme.text}>Transactions</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/transactions">
              <Button className={theme.primary}>Create Transaction</Button>
            </Link>
          </CardContent>
        </Card>
        <Card className={theme.card}>
          <CardHeader>
            <CardTitle className={theme.text}>Donations</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/donations">
              <Button className={theme.primary}>Manage Donations</Button>
            </Link>
          </CardContent>
        </Card>
        <Card className={theme.card}>
          <CardHeader>
            <CardTitle className={theme.text}>Gallery</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/gallery">
              <Button className={theme.primary}>View Gallery</Button>
            </Link>
          </CardContent>
        </Card>
        <Card className={theme.card}>
          <CardHeader>
            <CardTitle className={theme.text}>Tracking Page</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/manage-tracking">
              <Button className={theme.primary}>Customize Tracking Page</Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>User Management</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/users">
              <Button className={theme.primary}>Manage Users</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default withAuth(DashboardPage);
