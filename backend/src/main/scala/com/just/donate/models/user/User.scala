package com.just.donate.models.user

case class User(
                 email: String, 
                 password: String, 
                 role: String = Roles.GUEST.toString, 
                 active: Boolean = true,
                 orgId: String
               )
