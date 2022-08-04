package com.stepup.codefetcher

import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Url

interface CodeService {

    @GET("/")
    fun getPath(): Single<NextPath>

    @GET
    fun getResponseCode(@Url url: String): Single<Response>
}

fun CodeService.getResponseCode(): Single<Response> =
    getPath().flatMap { getResponseCode(it.next_path) }

@Serializable
data class NextPath(val next_path: String)

@Serializable
data class Response(
    val path: String,
    val response_code: String
)

@Serializable
data class Error(val error: String)

val Throwable.errorMessage
    get() = (this as? HttpException)?.response()?.errorBody()?.string()?.let {
        runCatching { Json.decodeFromString<Error>(it).error }.getOrNull()
    }