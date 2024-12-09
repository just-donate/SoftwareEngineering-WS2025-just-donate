[![Backend Pipeline](https://github.com/AntonKluge/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend.yml/badge.svg?branch=ci%2Fpipeline)](https://github.com/AntonKluge/SoftwareEngineering-WS2025-just-donate/actions/workflows/backend.yml)

# just-donate



## Architecture

### Structures

**DonationQueue**

We differ between two types of DonationQueues:

1. **UnboundDonationQueue** - A queue that is used to store donations that are not bound to a purpose. This queue can go into minus as long as the account is covering the expenses by other means.
2. **BoundDonationQueue** - A queue that is used to store donations that are bound to a purpose. This queue can only go into minus if the something upstream is covering the expenses.
