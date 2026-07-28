package com.example.hampouch.data.remote

class ApiException(
    val code: String,
    override val message: String
) : Exception(message)
