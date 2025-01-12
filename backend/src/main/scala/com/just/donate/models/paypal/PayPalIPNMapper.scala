package com.just.donate.models.paypal

import cats.data.Chain
import cats.effect.IO
import com.just.donate.utils.Money

object PayPalIPNMapper:
  def mapToPayPalIPN(urlForm: Map[String, Chain[String]]): IO[PayPalIPN] = IO {
    PayPalIPN(
      // Transaction fields
      txnId = urlForm.getOrElse("txn_id", throw new Exception("Missing txn_id")).toString,
      txnType = urlForm.getOrElse("txn_type", throw new Exception("Missing txn_type")).toString,
      paymentStatus = urlForm.getOrElse("payment_status", throw new Exception("Missing payment_status")).toString,
      paymentDate = urlForm.getOrElse("payment_date", throw new Exception("Missing payment_date")).toString,
      mcGross = Money(urlForm.getOrElse("mc_gross", throw new Exception("Missing mc_gross")).toString),
      mcFee = Money(urlForm.getOrElse("mc_fee", throw new Exception("Missing mc_fee")).toString),
      invoice = urlForm.get("invoice").map(_.toString),

      // Payer fields
      payerId = urlForm.getOrElse("payer_id", throw new Exception("Missing payer_id")).toString,
      payerEmail = urlForm.getOrElse("payer_email", throw new Exception("Missing payer_email")).toString,
      payerStatus = urlForm.getOrElse("payer_status", throw new Exception("Missing payer_status")).toString,
      firstName = urlForm.getOrElse("first_name", throw new Exception("Missing first_name")).toString,
      lastName = urlForm.getOrElse("last_name", throw new Exception("Missing last_name")).toString,

      // Business / Receiver info
      receiverId = urlForm.getOrElse("receiver_id", throw new Exception("Missing receiver_id")).toString,
      receiverEmail = urlForm.getOrElse("receiver_email", throw new Exception("Missing receiver_email")).toString,
      business = urlForm.getOrElse("business", throw new Exception("Missing business")).toString,

      // Address / Shipping fields
      addressName = urlForm.getOrElse("address_name", throw new Exception("Missing address_name")).toString,
      addressStreet = urlForm.getOrElse("address_street", throw new Exception("Missing address_street")).toString,
      addressCity = urlForm.getOrElse("address_city", throw new Exception("Missing address_city")).toString,
      addressState = urlForm.getOrElse("address_state", throw new Exception("Missing address_state")).toString,
      addressZip = urlForm.getOrElse("address_zip", throw new Exception("Missing address_zip")).toString,
      addressCountry =
        urlForm.getOrElse("address_country", throw new Exception("Missing address_country")).toString,
      addressCountryCode =
        urlForm.getOrElse("address_country_code", throw new Exception("Missing address_country_code")).toString,

      // Additional fields
      notifyVersion = urlForm.getOrElse("notify_version", throw new Exception("Missing notify_version")).toString,
      protectionEligibility =
        urlForm.getOrElse("protection_eligibility", throw new Exception("Missing protection_eligibility")).toString,
      verifySign = urlForm.getOrElse("verify_sign", throw new Exception("Missing verify_sign")).toString,
      ipnTrackId = urlForm.getOrElse("ipn_track_id", throw new Exception("Missing ipn_track_id")).toString,

      // Currency, quantity, and other details
      mcCurrency = urlForm.getOrElse("mc_currency", throw new Exception("Missing mc_currency")).toString,
      quantity = Integer.parseInt(
        urlForm.getOrElse("quantity", throw new Exception("Missing or invalid quantity")).toString
      ),
      itemName = urlForm.getOrElse("item_name", throw new Exception("Missing item_name")).toString
    )
  }
