package com.scf.secondbloom.data.remote

sealed class RemodelApiException(message: String) : Exception(message)

class InvalidImageException(message: String) : RemodelApiException(message)

class ModelResponseException(message: String) : RemodelApiException(message)
