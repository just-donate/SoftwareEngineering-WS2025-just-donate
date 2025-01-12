package com.just.donate.models.user

import org.bson.types.ObjectId

case class User(
                 email: String, 
                 password: String, 
                 role: Roles = Roles.GUEST, 
                 active: Boolean = true,
                 orgId: String
               )
