[![Backend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend.yml) [![Frontend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend.yml)  [![Frontend Deployment](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/deployment-ci.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/deployment-ci.yml)

# just-donate

## Architecture

### Structures

**DonationQueue**

We differ between two types of DonationQueues:

1. **UnboundDonationQueue** - A queue that is used to store donations that are not bound to a purpose. This queue can go into minus as long as the account is covering the expenses by other means.
2. **BoundDonationQueue** - A queue that is used to store donations that are bound to a purpose. This queue can only go into minus if the something upstream is covering the expenses.
