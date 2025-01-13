package com.just.donate.models.paypal

import cats.data.Chain
import cats.effect.IO
import com.just.donate.utils.Money

object PayPalIPNMapper:
  def mapToPayPalIPN(urlForm: Map[String, Chain[String]]): IO[PayPalIPN] = IO {

    val customInfo = urlForm.get("custom").flatMap(_.headOption).getOrElse("").split("/")

    PayPalIPN(
      // Transaction fields
      txnId = urlForm
        .get("txn_id")         // Option[Chain[String]]
        .flatMap(_.headOption)   // Option[String] - Get the first value in the Chain
        .getOrElse(""),       // Default to "1" if no value is found,
      txnType = urlForm.get("txn_type").flatMap(_.headOption).getOrElse(""),
      paymentStatus = urlForm.get("payment_status").flatMap(_.headOption).getOrElse(""),
      paymentDate = urlForm.get("payment_date").flatMap(_.headOption).getOrElse(""),
      mcGross = Money(urlForm.get("mc_gross").flatMap(_.headOption).getOrElse("0.00")),
      mcFee = Money(urlForm.get("mc_fee").flatMap(_.headOption).getOrElse("0.00")),
      invoice = urlForm.get("invoice").map(_.toString),

      // Payer fields
      payerId = urlForm.get("payer_id").flatMap(_.headOption).getOrElse(""),
      payerEmail = urlForm.get("payer_email").flatMap(_.headOption).getOrElse(""),
      payerStatus = urlForm.get("payer_status").flatMap(_.headOption).getOrElse(""),
      firstName = urlForm.get("first_name").flatMap(_.headOption).getOrElse(""),
      lastName = urlForm.get("last_name").flatMap(_.headOption).getOrElse(""),
      organisationName = customInfo.lift(0).getOrElse(""),
      notificationEmail = customInfo.lift(1).getOrElse(""),

      // Business / Receiver info
      receiverId = urlForm.get("receiver_id").flatMap(_.headOption).getOrElse(""),
      receiverEmail = urlForm.get("receiver_email").flatMap(_.headOption).getOrElse(""),
      business = urlForm.get("business").flatMap(_.headOption).getOrElse(""),

      // Address / Shipping fields
      addressName = urlForm.get("address_name").flatMap(_.headOption).getOrElse(""),
      addressStreet = urlForm.get("address_street").flatMap(_.headOption).getOrElse(""),
      addressCity = urlForm.get("address_city").flatMap(_.headOption).getOrElse(""),
      addressState = urlForm.get("address_state").flatMap(_.headOption).getOrElse(""),
      addressZip = urlForm.get("address_zip").flatMap(_.headOption).getOrElse(""),
      addressCountry =
        urlForm.get("address_country").flatMap(_.headOption).getOrElse(""),
      addressCountryCode =
        urlForm.get("address_country_code").flatMap(_.headOption).getOrElse(""),

      // Additional fields
      notifyVersion = urlForm.get("notify_version").flatMap(_.headOption).getOrElse(""),
      protectionEligibility =
        urlForm.get("protection_eligibility").flatMap(_.headOption).getOrElse(""),
      verifySign = urlForm.get("verify_sign").flatMap(_.headOption).getOrElse(""),
      ipnTrackId = urlForm.get("ipn_track_id").flatMap(_.headOption).getOrElse(""),

      // Currency, quantity, and other details
      mcCurrency = urlForm.get("mc_currency").flatMap(_.headOption).getOrElse(""),
      quantity =  urlForm
      .get("quantity")         // Option[Chain[String]]
      .flatMap(_.headOption)   // Option[String] - Get the first value in the Chain
      .getOrElse("1")          // Default to "1" if no value is found
      .toInt,
    itemName = urlForm.get("item_name").flatMap(_.headOption).getOrElse(""),
    )
  }
