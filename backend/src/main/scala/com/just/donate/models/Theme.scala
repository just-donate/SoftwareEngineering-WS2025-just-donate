package com.just.donate.models

case class StatusColors(
                         announced: String,
                         pending_confirmation: String,
                         confirmed: String,
                         received: String,
                         in_transfer: String,
                         processing: String,
                         allocated: String,
                         awaiting_utilization: String,
                         used: String
                       )

// Define the main ThemeConfig case class
case class ThemeConfig(
                        primary: String,
                        secondary: String,
                        accent: String,
                        background: String,
                        card: String,
                        text: String,
                        textLight: String,
                        font: String,
                        icon: String,
                        ngoName: String,
                        ngoUrl: String,
                        helpUrl: String,
                        statusColors: StatusColors
                      )