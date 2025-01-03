[![Backend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend-ci.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend-ci.yml) [![Frontend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-ci.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-ci.yml) [![Frontend Deployment](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-deployment-ci.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-deployment-ci.yml)

# just-donate

## Development

-   To run the MongoDB container run:

    ```bash
    docker run -p 27017:27017 --rm --name just-donate-mongodb -d mongo:latest
    ```

-   To run the backend run:

    ```bash
    cd backend
    gradle run
    ```

-   To run the frontend run:

    ```bash
    cd frontend
    npm install
    npm run dev
    ```

Running the backend will require some configuration.
These can be set via a configuration file or via environment variables.
The available config options can be found in the reference configuration at `backend/src/main/resources/reference.conf`.
The reference file can be copied to `backend/src/main/resources/application.conf`
and `backend/src/main/resources/application.dev.conf`.
The application will read the `dev` config if the environment variable `env` is set to `dev` or `development`.
The other file will be read if the variable is not set or set to `prod` or `production`.

## Deployment

Current Deployment of [Donation Tracking](https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/) reachable at [https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/](https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/)

## Architecture

### Structures

**DonationQueue**

We differ between two types of DonationQueues:

1. **UnboundDonationQueue** - A queue that is used to store donations that are not bound to a purpose. This queue can go into minus as long as the account is covering the expenses by other means.
2. **BoundDonationQueue** - A queue that is used to store donations that are bound to a purpose. This queue can only go into minus if the something upstream is covering the expenses.
