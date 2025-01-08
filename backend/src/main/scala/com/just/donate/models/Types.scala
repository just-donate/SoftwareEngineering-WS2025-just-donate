package com.just.donate.models

/**
 * Single source of truth for all types used in the application
 */
object Types:

  type DonationGetter = String => Option[Donation]

