[![Backend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend-ci.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend-ci.yml) [![Frontend Pipeline](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-ci.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-ci.yml)  [![Frontend Deployment](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-deployment-ci.yml/badge.svg?branch=main)](https://github.com/just-donate/SoftwareEngineering-WS2025-just-donate/actions/workflows/frontend-deployment-ci.yml)

# just-donate

## Deployment

Current Deployment of [Donation Tracking](https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/) reachable at [https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/](https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/)

## Architecture

### Structures

**DonationQueue**

We differ between two types of DonationQueues:

1. **UnboundDonationQueue** - A queue that is used to store donations that are not bound to a purpose. This queue can go into minus as long as the account is covering the expenses by other means.
2. **BoundDonationQueue** - A queue that is used to store donations that are bound to a purpose. This queue can only go into minus if the something upstream is covering the expenses.
