'use client';

import Link from 'next/link';
import { Button } from '../../../components/organization/ui/button';
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
} from '../../../components/organization/ui/card';
import withAuth from '../api/RequiresAuth';

function DashboardPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Organization Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Card>
          <CardHeader>
            <CardTitle>Earmarkings</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/earmarkings">
              <Button>Manage Earmarkings</Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Bank Accounts</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/bank-accounts">
              <Button>Manage Bank Accounts</Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Transactions</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/transactions">
              <Button>Create Transaction</Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Donations</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/donations">
              <Button>Manage Donations</Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Gallery</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/gallery">
              <Button>View Gallery</Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Tracking Page</CardTitle>
          </CardHeader>
          <CardContent>
            <Link href="/organization/manage-tracking">
              <Button>Customize Tracking Page</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default withAuth(DashboardPage)
