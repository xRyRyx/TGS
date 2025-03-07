package com.tgs.app.data.model

data class User(
    val email : String? = null,
    val fullName : String? = null,
    val fcmtoken : String? = null,
    val phoneNumber : String? = null,
    val houseNumber : String? = null,
    val streetName : String? = null,
    val province : String? = null,
    val municipality : String? = null,
    val barangay : String? = null
)

