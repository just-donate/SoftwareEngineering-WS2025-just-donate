package com.just.donate.models.paypal

import com.just.donate.utils.Money

case class PayPalIPN(
                      // Transaction fields
                      txnId: String,
                      txnType: String,
                      paymentStatus: String,
                      paymentDate: String,     // Consider using a date/time type if parsing the value
                      mcGross: Money, // Money
                      mcFee: Money, // Money
                      invoice: Option[String], // Some values may be empty, so using Option

                      // Payer fields
                      payerId: String,
                      payerEmail: String,
                      payerStatus: String,
                      firstName: String,
                      lastName: String,
                      notificationEmail: String,

                      // Business / Receiver info
                      receiverId: String,
                      receiverEmail: String,
                      business: String,
                      organisationName: String,

                      // Address / Shipping fields
                      addressName: String,
                      addressStreet: String,
                      addressCity: String,
                      addressState: String,
                      addressZip: String,
                      addressCountry: String,
                      addressCountryCode: String,

                      // Additional fields for completeness (customizing as needed)
                      notifyVersion: String,
                      protectionEligibility: String,
                      verifySign: String,
                      ipnTrackId: String,

                      // Currency, quantity and other details
                      mcCurrency: String,
                      quantity: Int, // Int
                      itemName: String
                    )
