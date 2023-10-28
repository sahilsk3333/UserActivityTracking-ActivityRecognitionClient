package me.iamsahil.googlemapsusertracking.utils


typealias SimpleResource = Resource<Unit>
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val throwable: Throwable? = null
) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String?, data: T? = null) : Resource<T>(data, message)
}