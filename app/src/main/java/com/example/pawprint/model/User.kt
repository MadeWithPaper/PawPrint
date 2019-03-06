package com.example.pawprint.model

import java.io.Serializable

data class User (
    var name : String,
    var email : String,
    var historyList : List<String>
) : Serializable {
    constructor() : this(name = "", email = "", historyList = listOfNotNull() )

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "historyList" to historyList
        )
    }
}