'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { cn } from "@/lib/utils"

const navItems = [
  { href: '/dashboard', label: 'Dashboard' },
  { href: '/earmarkings', label: 'Earmarkings' },
  { href: '/bank-accounts', label: 'Bank Accounts' },
  { href: '/transactions', label: 'Transactions' },
  { href: '/donations', label: 'Donations' },
  { href: '/gallery', label: 'Gallery' },
]

export function NavBar() {
  const pathname = usePathname()

  return (
    <nav className="bg-secondary">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <Link href="/" className="text-primary text-lg font-semibold">
              Donation Platform
            </Link>
          </div>
          <div className="hidden md:block">
            <div className="ml-10 flex items-baseline space-x-4">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "px-3 py-2 rounded-md text-sm font-medium",
                    pathname === item.href
                      ? "bg-primary text-primary-foreground"
                      : "text-secondary-foreground hover:bg-primary/20 hover:text-primary-foreground"
                  )}
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}

