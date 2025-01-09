"use client";

import { useSearchParams } from "next/navigation";

export default function PaypalConfirmationPage() {
  const router = useSearchParams();

  console.log(router);

  return (
    <div>
      <h1>PayPal IPN Handler</h1>
      <p>Transaction ID (tx): {router.get('tx')}</p>
    </div>
  );
}
