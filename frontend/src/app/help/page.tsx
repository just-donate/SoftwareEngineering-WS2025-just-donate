'use client';

import React, { useEffect } from 'react';
import { useTheme } from '@/contexts/ThemeContext';

export default function HelpPage() {
  const { theme } = useTheme();

  // Enable smooth scrolling when clicking on anchor links
  useEffect(() => {
    document.documentElement.style.scrollBehavior = 'smooth';
    return () => {
      document.documentElement.style.scrollBehavior = 'auto';
    };
  }, []);

  // Table of Contents items with section IDs and titles.
  const tocItems = [
    { id: 'getting-started', title: 'Getting Started' },
    { id: 'tracking-donation', title: 'Tracking Your Donation' },
    { id: 'registration-login', title: 'User Registration and Login' },
    { id: 'dashboard', title: 'Navigating the Dashboard' },
    { id: 'additional-features', title: 'Additional Features' },
    { id: 'contact-support', title: 'Contact Support' },
  ];

  return (
    <div className={`min-h-screen bg-gray-50 ${theme.text}`}>
      <div className="container mx-auto flex flex-col md:flex-row p-6">
        {/* Left sidebar with Table of Contents */}
        <nav className="md:w-1/4 mb-6 md:mb-0 md:pr-6">
          <div className="sticky top-6">
            <h2 className="text-2xl font-bold mb-4">Contents</h2>
            <ul className="space-y-2">
              {tocItems.map((item) => (
                <li key={item.id}>
                  <a
                    href={`#${item.id}`}
                    className="text-blue-600 hover:underline cursor-pointer"
                  >
                    {item.title}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        </nav>

        {/* Main content */}
        <div className="md:w-3/4">
          <section id="getting-started" className="mb-8">
            <h2 className="text-3xl font-bold mb-4">Getting Started</h2>
            <ol className="list-decimal ml-5 space-y-2">
              <li>
                Open your browser and navigate to:{' '}
                <a
                  href="https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="underline text-blue-600"
                >
                  https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/
                </a>
              </li>
              <li>
                On the homepage, read the introduction explaining how Just
                Donate works.
              </li>
              <li>
                Use the search bar to enter your unique tracking ID (or tracking
                link) that you received via email.
              </li>
            </ol>
          </section>

          <section id="tracking-donation" className="mb-8">
            <h2 className="text-3xl font-bold mb-4">Tracking Your Donation</h2>
            <p className="mb-4">
              After entering your tracking ID or link and clicking
              &quot;Search&quot;, you will be taken to your donation tracking
              page where you can:
            </p>
            <ul className="list-disc ml-5 space-y-2">
              <li>
                View the donation amount and its current status (e.g.,
                &quot;Received&quot;, &quot;Processing&quot;, or &quot;Fully
                Utilized&quot;).
              </li>
              <li>
                Review any status updates or notifications about the use of your
                donation.
              </li>
              <li>
                Access further details using the navigation options provided on
                the tracking page.
              </li>
            </ul>
          </section>

          <section id="registration-login" className="mb-8">
            <h2 className="text-3xl font-bold mb-4">
              User Registration and Login
            </h2>
            <p className="mb-2">
              <strong>Registration:</strong> If you are a first-time user, click
              the <strong>Register</strong> button and provide your email and a
              password.
            </p>
            <p className="mb-2">
              <strong>Login:</strong> Use the <strong>Login</strong> page to
              enter your registered email, password, and (if necessary) your
              Organisation ID. Successful authentication will take you to your
              dashboard.
            </p>
            <p>
              If your credentials are invalid, you will see an error prompting
              you to check your details.
            </p>
          </section>

          <section id="dashboard" className="mb-8">
            <h2 className="text-3xl font-bold mb-4">
              Navigating the Dashboard
            </h2>
            <p className="mb-4">
              Once logged in, your dashboard provides an overview of your
              donation status along with additional features such as:
            </p>
            <ul className="list-disc ml-5 space-y-2">
              <li>
                <strong>Donation Status:</strong> Check your donation amount,
                current status, and updates.
              </li>
              <li>
                <strong>Account Management:</strong> (For users with account
                access) See which accounts (e.g., &quot;Paypal&quot;,
                &quot;Bank&quot;) have been credited.
              </li>
              <li>
                <strong>Timeline and Notifications:</strong> View a timeline of
                status updates and receive email notifications regarding your
                donation.
              </li>
              <li>
                <strong>Navigation:</strong> Use the top menu to quickly switch
                between your dashboard, donation details, and help page.
              </li>
            </ul>
          </section>

          <section id="additional-features" className="mb-8">
            <h2 className="text-3xl font-bold mb-4">Additional Features</h2>
            <ul className="list-disc ml-5 space-y-2">
              <li>
                <strong>Forgot Tracking Link:</strong> If you have forgotten
                your tracking link, click the &quot;Forgot Tracking Link?&quot;
                button on the homepage, enter your email address, and a new
                tracking link will be sent to you.
              </li>
              <li>
                <strong>Notifications:</strong> The system will send email
                notifications when there are updates regarding your
                donation&#39;s status.
              </li>
            </ul>
          </section>

          <section id="contact-support" className="mb-8">
            <h2 className="text-3xl font-bold mb-4">Contact Support</h2>
            <p>
              If you need further assistance, please contact our support team
              at:{' '}
              <a
                href="mailto:contact.just.donate@gmail.com"
                className="underline text-blue-600"
              >
                contact.just.donate@gmail.com
              </a>
              .
            </p>
          </section>
        </div>
      </div>
    </div>
  );
}
