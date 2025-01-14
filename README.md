─────────────────────────────────────────────────────────────  
just-donate  
─────────────────────────────────────────────────────────────

![Backend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend-ci.yml/badge.svg?branch=main)
![Frontend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-ci.yml/badge.svg?branch=main)
![Frontend Deployment](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-deployment-ci.yml/badge.svg?branch=main)

─────────────────────────────────────────────────────────────  
Table of Contents
─────────────────────────────────────────────────────────────

1. Overview  
2. Architecture  
3. Domain Model Class Diagram  
4. API Routes Explanation  
5. Deployment Instructions  
6. Development and Testing  
7. Additional Resources  

─────────────────────────────────────────────────────────────  
1. Overview
   
─────────────────────────────────────────────────────────────

just-donate is a donation tracking platform that enables donors and organizations to interact transparently. Donors can see exactly how their contributions are used, while organizations can manage incoming donations, execute withdrawals and transfers, and send notifications regarding donation status changes. The system is divided into two primary parts:

• Frontend: A Next.js application (using React and TailwindCSS) that provides the user interface for donors and organizations.  
• Backend: A Scala-based service built with http4s that exposes REST endpoints for authentication, organization management, donation processing, withdrawals, fund transfers, and PayPal IPN handling.

Both components are containerized using Docker. The frontend is deployed as a static site (via GitHub Pages under [Frontend Deployment](https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/)) and the backend is deployed on Google Cloud (Cloud Run) under [Backend Deployment](https://just-donate-api-556297337052.europe-west3.run.app).

─────────────────────────────────────────────────────────────  
2. Architecture
─────────────────────────────────────────────────────────────

Below is a high-level PlantUML diagram that illustrates the overall system architecture:

----------------------------------------------------

![image](https://github.com/user-attachments/assets/c8b4bba7-131b-42a7-b450-81abaa9da1c5)

----------------------------------------------------

Explanation:
- Frontend: A Next.js application built with React that is packaged into a Docker image and deployed as a static site.
- Backend: A Scala-based HTTP service packaged via sbt-assembly and Docker, exposing endpoints for authentication, organisation management, donations, withdrawals, transfers, notifications, and PayPal IPN handling.
- External Services: The backend connects to a MongoDB instance for data persistence, interacts with PayPal for IPN validation, and sends emails via SMTP.
- Deployment: The backend is hosted on Google Cloud (e.g., via Kubernetes/GKE or Cloud Run) and the frontend is deployed as a static site.

─────────────────────────────────────────────────────────────  
3. Domain Model Class Diagram  
─────────────────────────────────────────────────────────────

Below is a PlantUML class diagram for the main domain models:

----------------------------------------------------

![image](https://github.com/user-attachments/assets/2e6f92f9-feca-450a-93a4-822dc35aee42)

----------------------------------------------------

Explanation:
• Organisation: Represents an organization that contains accounts, donations, expenses, donors, and earmarkings. Methods are provided for managing these components and processing operations such as donations, withdrawals, and transfers.
• Account: Manages both bound (earmarked) and unbound donation flows using a DonationQueue.
• Donation and DonationPart: A Donation represents the overall donation record and can be split into DonationPart objects for handling partial donations and earmarking.
• StatusUpdate: Captures the changes in a donation’s lifecycle (e.g., RECEIVED, CONFIRMED, USED).
• Donor, Earmarking, Expense, ThemeConfig, and Money: Represent supporting domain objects and a value object for currency amounts.

─────────────────────────────────────────────────────────────  
4. API Routes Explanation  
─────────────────────────────────────────────────────────────

Below is a detailed explanation of every API route:

────────────────────────────  
Authentication Routes  
────────────────────────────  
• Login  
  - Route: POST /login  
  - Function: Validates user credentials.  
  - Process:
    1. Receives a JSON payload with "username", "password", and "orgId".  
    2. The user repository is queried for the given username.
    3. The password is verified using secure hash functions (CryptoUtils).
    4. On success, a JWT token is generated with a set expiration time and claims (username and orgId).  
    5. The token is set as an HTTP-only cookie (jwtToken) in the response.
    6. Returns HTTP 200 with a success message.
  - Errors: Returns 403 Forbidden if credentials are invalid; returns 401 or 400 for malformed requests.

• Logout  
  - Route: POST /logout  
  - Function: Logs out the user by expiring the authentication cookie.
  - Process:
    1. The jwtToken cookie is replaced with an expired cookie (maxAge=0).
    2. Returns HTTP 200 with a "Logout successful" message.

• Check Authentication  
  - Route: GET /check-auth  
  - Function: Verifies the presence and validity of the jwtToken cookie.
  - Process:
    1. Extracts the jwtToken cookie from the request.
    2. Validates the JWT (checks signature and expiration).
    3. Returns HTTP 200 with a JSON message "Authenticated" if valid; otherwise returns 403 or 400.

────────────────────────────  
Organisation Routes  
────────────────────────────  
• Create Organisation  
  - Route: POST /organisation  
  - Function: Creates a new organization.
  - Process:
    1. Accepts a JSON body (e.g., { "name": "OrgName" }).
    2. Constructs a new Organisation object and saves it using the repository.
    3. Returns the organisation’s unique ID and name (HTTP 200).
  
• List Organisations  
  - Route: GET /organisation/list  
  - Function: Retrieves a list of all organization IDs.
  - Process:
    1. Queries the repository for all organisations.
    2. Returns a JSON array of organization IDs (HTTP 200).

• Get Organisation Details  
  - Route: GET /organisation/{organisationId}  
  - Function: Retrieves details of a specific organization.
  - Process:
    1. Extracts "organisationId" from the URL.
    2. Looks up the organisation in the repository.
    3. Returns an object with the organization’s ID and name (HTTP 200) or a 404 if not found.

• Delete Organisation  
  - Route: DELETE /organisation/{organisationId}  
  - Function: Deletes the specified organization.
  - Process:
    1. The organisation is removed from the repository.
    2. Returns HTTP 200. The operation is idempotent.

• Add Earmarking  
  - Route: POST /organisation/{organisationId}/earmarking  
  - Function: Adds a new earmarking to the organization.
  - Process:
    1. Accepts a JSON payload with "name" and "description" of the earmarking.
    2. Loads the organisation, adds the earmarking, and updates the repository.
    3. Returns HTTP 200 on success.

• List Earmarkings  
  - Route: GET /organisation/{organisationId}/earmarking/list  
  - Function: Returns all earmarkings for the specified organization.
  - Process:
    1. Loads the organization.
    2. Maps the earmarkings to a response format.
    3. Returns the list (HTTP 200) or 404 if the organization is not found.

• Add Account  
  - Route: POST /organisation/{organisationId}/account  
  - Function: Creates a new account within an organization.
  - Process:
    1. Accepts a JSON payload containing the "name" of the account and the initial "balance".
    2. Loads the organization and adds the account.
    3. Updates the organization in the repository and returns HTTP 200.

• Delete Account  
  - Route: DELETE /organisation/{organisationId}/account/{accountName}  
  - Function: Removes an account from an organization.
  - Process:
    1. Loads the organization.
    2. Deletes the specified account.
    3. Updates the repository and returns HTTP 200.

────────────────────────────  
Donation Routes  
────────────────────────────  
• Make Donation  
  - Route: POST /donate/{organisationId}/account/{accountName}  
  - Function: Registers a donation made to a specific account.
  - Process:
    1. Accepts a JSON payload with donor details ("donorName" and "donorEmail"), "amount" (Money), and an optional "earmarking".  
    2. Checks if the donor already exists in the organization; if not, a new donor is created.
    3. Creates a Donation instance (which returns both a Donation and a DonationPart).  
    4. The specified account is updated with the donation (as an unbound donation or as earmarked donation).
    5. Generates a tracking ID (typically using the donor ID) and constructs a tracking link using the frontend URL.
    6. Triggers the email service to send the tracking link to the donor.
    7. Returns HTTP 200 on success or 400 (Bad Request) if any error occurs.

• Get Donations  
  - Routes:  
    - GET /donate/{organisationId}/donor/{donorId} – Returns donations specific to a donor.
    - GET /donate/{organisationId}/donations – Returns all donations for the organization.
  - Process:
    1. Loads the organization from the repository.
    2. Filters donations by donorId (if provided) or returns all donations.
    3. Maps each donation to a response format that includes donation ID, date, amount, earmarking (if any), and status updates.
    4. Returns the data with HTTP 200.

────────────────────────────  
Transfer Routes  
────────────────────────────  
• Transfer Funds  
  - Route: POST /transfer/{organisationId}  
  - Function: Transfers funds between two accounts within the same organization.
  - Process:
    1. Accepts a JSON payload with "fromAccount", "toAccount", and "amount" (Money).
    2. Loads the organization and verifies the existence of both accounts.
    3. Checks that the source account has sufficient funds and that the amount is positive (and that the source and destination are distinct).
    4. Proceeds to withdraw the specified amount from the source (potentially splitting donation parts) and adds it to the destination account.
    5. Returns HTTP 200 with the updated organization and any relevant email notifications; otherwise, returns an error (e.g., 400 if insufficient funds or 404 if an account is not found).

────────────────────────────  
Withdrawal Routes  
────────────────────────────  
• Withdraw Funds  
  - Route: POST /withdraw/{organisationId}/account/{accountName}  
  - Function: Processes a withdrawal request from a given account.
  - Process:
    1. Receives a JSON payload with "amount" (Money), "description" for the withdrawal, and an optional "earmarking".  
    2. Loads the organization and the relevant account.
    3. The withdrawal operation withdraws the specified amount from the account. If the withdrawal is earmarked, it only uses the earmarked portion.
    4. If sufficient funds are unavailable, the operation returns an error.  
    5. On success, updates the account’s balance and records an expense. It may also trigger email notifications to donors whose contributions have been utilized fully.
    6. Returns HTTP 200 on success or 400 (Bad Request) if errors occur.

────────────────────────────  
Notification & PayPal IPN Routes  
────────────────────────────  
• Send Notification  
  - Route: POST /notify/{donor}  
  - Function: Sends an email notification to a donor.
  - Process:
    1. The path parameter “donor” specifies the recipient (typically the donor’s email).
    2. Optionally accepts a JSON payload with a “message”.
    3. Uses the email service to send the provided message.
    4. Returns HTTP 200 confirming that the notification was sent.

• PayPal IPN Processing  
  - Route: POST /paypal-ipn  
  - Function: Receives and processes PayPal’s Instant Payment Notifications (IPN).
  - Process:
    1. Receives URL-encoded form data from PayPal containing transaction details.
    2. Immediately returns HTTP 200 to acknowledge receipt and avoid timeouts.
    3. Asynchronously, the backend validates the IPN data by sending it to PayPal for verification (with retry logic).  
    4. If PayPal returns “VERIFIED”:  
       - Maps the incoming data into a PayPalIPN domain model.
       - Checks the payment status (ensuring it is “Completed”) and ensures no duplicate IPN records are stored.
       - Invokes the donation route (using the item name as earmarking if provided) to record the donation.
       - Sends an email containing the tracking ID and link to the donor.
    5. If validation fails or returns “INVALID”, logs the error accordingly.

─────────────────────────────────────────────────────────────  
5. Deployment Instructions  
─────────────────────────────────────────────────────────────

Backend Deployment (Google Cloud):
1. Docker Build and Push  
   - Navigate to the backend directory, compile the application (using sbt assembly), and build the Docker image:
     
     cd backend  
     sbt assembly  
     docker build -t gcr.io/<YOUR_PROJECT>/backend:latest .  
     docker push gcr.io/<YOUR_PROJECT>/backend:latest  

2. Deploy to Google Cloud  
   - Use Kubernetes (GKE) or Google Cloud Run by writing appropriate deployment YAML files or via the GCP console/CLI.  
   - Set environment variables (e.g., MONGO_URI, JWT_SECRET_KEY, MAIL_SMTP_HOST) as deployment configuration.

Frontend Deployment:

1. Deploy  
   - Deploy as a static site using Next.js export on GitHub Pages, Vercel, or another hosting provider.

─────────────────────────────────────────────────────────────  
6. Development and Testing  
─────────────────────────────────────────────────────────────

Backend:
- Run the backend service and tests using sbt:
  
  cd backend  
  sbt run  
  sbt test

Frontend:
- Run the development server and tests:
  
  cd frontend  
  npm install  
  npm run dev  
  npm run test

Continuous Integration:
- GitHub Actions are set up to run the backend and frontend pipelines as well as SonarCloud analysis.
- On updates to main both backend and frontend are automatically deployed

─────────────────────────────────────────────────────────────  
7. Additional Resources  
─────────────────────────────────────────────────────────────

- Diagrams:  
  Refer to the PlantUML diagrams above for both the high-level architecture and the domain model class relationships.

- Design Documents:  
  Additional design artefacts (personas, context maps, story maps) can be found in the documentation folder (e.g., blatt05.md, blatt06.md).

- Testing:  
  Backend tests use MUnit (located in /src/test), and frontend tests use Jest.

─────────────────────────────────────────────────────────────  
End of Documentation  
─────────────────────────────────────────────────────────────
