# just-donate

![Backend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend-ci.yml/badge.svg?branch=main)
![Frontend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-ci.yml/badge.svg?branch=main)
![Frontend Deployment](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-deployment-ci.yml/badge.svg?branch=main)

## Table of Contents
1. [Introduction](#1-introduction)
2. [Overview](#2-overview)  
3. [Architecture](#3-architecture)  
4. [Domain Model Class Diagram](#4-domain-model-class-diagram)  
5. [API Routes Explanation](#5-api-routes-explanation)  
6. [Deployment Instructions](#6-deployment-instructions)  
7. [Development and Testing](#7-development-and-testing)  
8. [Additional Resources](#8-additional-resources)

---

## 1. Introduction

**just-donate** is a donation tracking platform that connects donors and organizations with unparalleled transparency. Donors get real-time insights into how their contributions are being used, while organizations can effortlessly manage donations, withdrawals, and fund transfers—ensuring that every penny is accounted for.

By centralizing these processes, just-donate streamlines the donation cycle end-to-end:
- **Donors**: Monitor donation statuses, view earmarked funds, and receive updates on how their money is spent.  
- **Organizations**: Handle multiple accounts, accept donations, process withdrawals, and track the flow of funds across earmarked projects or general accounts.

Below are two sample screenshots demonstrating the user experience:

**Donor Dashboard**  
![Donor Dashboard Screenshot](https://github.com/user-attachments/assets/051ae6b4-9c99-43de-80fa-d778b3c8f96a)


**Organization Management**  
![Organization Management Screenshot](https://github.com/user-attachments/assets/2effe039-a655-440d-b53f-ea8869a8e880)


---

## 2. Overview

The system is divided into two primary parts:

- **Frontend**: A Next.js application (using React and TailwindCSS) that provides the user interface for donors and organizations. Each organisation can host their own frontend under their domain and customize it according to their wishes.
- **Backend**: A Scala-based service built with http4s that exposes REST endpoints for authentication, organization management, donation processing, withdrawals, fund transfers, and PayPal IPN handling. All frontends communicate with the same backend instance.
- **Persistency**: A MongoDB database stores the organisations, donations, users as well as all related data like logs. 

Both components are containerized using Docker. The frontend is deployed as a static site (via GitHub Pages under [Frontend Deployment](https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/)) and the backend is deployed on Google Cloud (Cloud Run) under [Backend Deployment](https://just-donate-api-556297337052.europe-west3.run.app).

---

## 3. Architecture

Below is a high-level PlantUML diagram illustrating the overall system architecture:

![(Architecture Diagram)](https://github.com/user-attachments/assets/c8b4bba7-131b-42a7-b450-81abaa9da1c5)

**Explanation**:
- **Frontend**: A Next.js application built with React that is packaged into a Docker image and deployed as a static site.
- **Backend**: A Scala-based HTTP service packaged via sbt-assembly and Docker, exposing endpoints for authentication, organization management, donations, withdrawals, transfers, notifications, and PayPal IPN handling.
- **External Services**: Connects to MongoDB for data persistence, interacts with PayPal for IPN validation, and sends emails via SMTP.
- **Deployment**: The backend is hosted on Google Cloud (e.g., via Kubernetes/GKE or Cloud Run) and the frontend is deployed as a static site.

---

## 4. Domain Model Class Diagram

Below is a PlantUML class diagram for the main domain models:

![(Class Diagram)](https://github.com/user-attachments/assets/2e6f92f9-feca-450a-93a4-822dc35aee42)

**Explanation**:
- **Organisation**: Contains accounts, donations, expenses, donors, and earmarkings. Methods manage operations such as donations, withdrawals, and transfers.  
- **Account**: Manages both bound (earmarked) and unbound donation flows using a DonationQueue.  
- **Donation** and **DonationPart**: A Donation can be split into DonationPart objects for handling partial donations and earmarking.  
- **StatusUpdate**: Tracks donation lifecycle changes (RECEIVED, CONFIRMED, USED).  
- **Donor**, **Earmarking**, **Expense**, **ThemeConfig**, and **Money**: Supporting domain objects and a value object for currency amounts.

---

## 5. API Routes Explanation

A detailed explanation of every API route is also available in the backend over [Swagger UI](https://just-donate-api-556297337052.europe-west3.run.app/api-docs/swagger-ui).

### Authentication Routes

- **Login**  
  - **Route**: POST /login  
  - **Function**: Validates user credentials.  
  - **Process**:  
    1. Receives JSON payload with "username", "password", and "orgId".  
    2. Queries user repository.  
    3. Verifies password using secure hash functions (CryptoUtils).  
    4. Generates and returns a JWT token as an HTTP-only cookie (jwtToken).  
  - **Errors**: Returns 403 for invalid credentials, 401/400 for malformed requests.

- **Logout**  
  - **Route**: POST /logout  
  - **Function**: Logs out the user by expiring the authentication cookie.  
  - **Process**:  
    1. Replaces jwtToken cookie with an expired cookie (maxAge=0).  
    2. Returns HTTP 200.

- **Check Authentication**  
  - **Route**: GET /check-auth  
  - **Function**: Verifies the validity of the jwtToken cookie.  
  - **Process**:  
    1. Extracts jwtToken.  
    2. Validates JWT signature and expiration.  
    3. Returns HTTP 200 if valid; otherwise 403/400.

### Organisation Routes

- **Create Organisation**  
  - **Route**: POST /organisation  
  - **Function**: Creates a new organization.  
  - **Process**:  
    1. Accepts JSON body (e.g., {"name": "OrgName"}).  
    2. Saves Organisation via repository.  
    3. Returns the organisation’s ID/name (HTTP 200).

- **List Organisations**  
  - **Route**: GET /organisation/list  
  - **Function**: Returns a list of all organization IDs.  
  - **Process**:  
    1. Queries repository for all organisations.  
    2. Returns JSON array of IDs (HTTP 200).

- **Get Organisation Details**  
  - **Route**: GET /organisation/{organisationId}  
  - **Function**: Returns details of a specific organization.  
  - **Process**:  
    1. Extracts organisationId.  
    2. Returns the org’s ID/name or 404 if not found.

- **Delete Organisation**  
  - **Route**: DELETE /organisation/{organisationId}  
  - **Function**: Deletes the specified organization.  
  - **Process**:  
    1. Removes the organisation from the repository.  
    2. Returns HTTP 200 (idempotent).

- **Add Earmarking**  
  - **Route**: POST /organisation/{organisationId}/earmarking  
  - **Function**: Adds a new earmarking.  
  - **Process**:  
    1. Accepts JSON with "name" and "description".  
    2. Loads org, adds earmarking, updates repo.  
    3. Returns HTTP 200.

- **List Earmarkings**  
  - **Route**: GET /organisation/{organisationId}/earmarking/list  
  - **Function**: Returns earmarkings for the organization.  
  - **Process**:  
    1. Loads org.  
    2. Returns earmarkings (HTTP 200) or 404 if not found.

- **Add Account**  
  - **Route**: POST /organisation/{organisationId}/account  
  - **Function**: Creates a new account in an organization.  
  - **Process**:  
    1. JSON payload with "name" and "balance".  
    2. Updates org and repository.  
    3. Returns HTTP 200.

- **Delete Account**  
  - **Route**: DELETE /organisation/{organisationId}/account/{accountName}  
  - **Function**: Removes an account.  
  - **Process**:  
    1. Loads org.  
    2. Deletes account, updates repo.  
    3. Returns HTTP 200.

### Donation Routes

- **Make Donation**  
  - **Route**: POST /donate/{organisationId}/account/{accountName}  
  - **Function**: Registers a new donation.  
  - **Process**:  
    1. JSON payload with donor info, amount, optional earmarking.  
    2. Creates a new donor if necessary.  
    3. Creates a Donation and DonationPart.  
    4. Adds donation to account.  
    5. Generates a tracking ID/link.  
    6. Sends tracking link via email.  
    7. Returns HTTP 200 or 400 on error.

- **Get Donations**  
  - **Routes**:  
    - GET /donate/{organisationId}/donor/{donorId}  
    - GET /donate/{organisationId}/donations  
  - **Function**: Returns donations by donor or all donations for the org.  
  - **Process**:  
    1. Loads org.  
    2. Filters or returns all donations.  
    3. Returns donation info (HTTP 200).

### Transfer Routes

- **Transfer Funds**  
  - **Route**: POST /transfer/{organisationId}  
  - **Function**: Transfers funds between accounts.  
  - **Process**:  
    1. JSON payload with "fromAccount", "toAccount", "amount".  
    2. Verifies accounts exist and have sufficient funds.  
    3. Withdraws and deposits the amount.  
    4. Returns HTTP 200 or error (400/404).

### Withdrawal Routes

- **Withdraw Funds**  
  - **Route**: POST /withdraw/{organisationId}/account/{accountName}  
  - **Function**: Processes a withdrawal from the specified account.  
  - **Process**:  
    1. JSON payload with "amount", "description", optional "earmarking".  
    2. Loads org/account.  
    3. Withdraws earmarked or unbound funds.  
    4. Records an expense and sends notifications as needed.  
    5. Returns HTTP 200 or 400 if errors occur.

### Notification & PayPal IPN Routes

- **Send Notification**  
  - **Route**: POST /notify/{donor}  
  - **Function**: Sends an email to the specified donor.  
  - **Process**:  
    1. Path param “donor” as email.  
    2. Optionally accepts a JSON "message".  
    3. Uses email service to send it.  
    4. Returns HTTP 200.

- **PayPal IPN Processing**  
  - **Route**: POST /paypal-ipn  
  - **Function**: Processes PayPal IPN callbacks.  
  - **Process**:  
    1. Receives form data from PayPal.  
    2. Returns HTTP 200 immediately to acknowledge.  
    3. Validates IPN by calling PayPal.  
    4. If “VERIFIED” and payment is “Completed”, records donation and sends email.  
    5. Logs errors if “INVALID”.

---

## 6. Deployment Instructions

### Backend (Google Cloud)

1. **Docker Build and Push**  
   ```bash
   cd backend
   sbt assembly
   docker build -t gcr.io/<YOUR_PROJECT>/backend:latest .
   docker push gcr.io/<YOUR_PROJECT>/backend:latest
   ```

2. **Deploy to Google Cloud**  
   - Deploy via Kubernetes (GKE) or Google Cloud Run with the relevant YAML or CLI commands.  
   - Configure environment variables (MONGO_URI, JWT_SECRET_KEY, MAIL_SMTP_HOST, etc.) in your deployment.

### Frontend

1. **Deploy**  
   - Deploy as a static site using Next.js export on GitHub Pages, Vercel, or another provider.

---

## 7. Development and Testing

### Backend
```bash
cd backend
sbt run # Run the app or
sbt test # Test the app
```

### Frontend
```bash
cd frontend
npm install
npm run dev # Run the app or
npm run test # Test the app
```

**Continuous Integration**:
- GitHub Actions are set up to run backend/frontend pipelines and SonarCloud checks.  
- On updates to `main`, both the backend and frontend deploy automatically.

---

## 8. Additional Resources

- **Diagrams**: Refer to the PlantUML diagrams for architecture and domain model overviews.  
- **Design Documents**: Additional artifacts (personas, context maps, story maps) are in the documentation folder (e.g., `blatt05.md`, `blatt06.md`).  
- **Testing**:  
  - Backend uses MUnit tests (in `/src/test`).  
  - Frontend uses Jest.
