package com.inari.team.core.utils.extensions

fun Any?.toString(): String {
    return this?.toString() ?: ""
}

fun Any?.toInt(): Int {
    return this?.let {
        when (it) {
            is String -> it.toIntOrNull() ?: 0
            is Int -> it
            is Double -> it.toInt()
            is Float -> it.toInt()
            is Boolean -> if (it) 1 else 0
            else -> 0
        }
    } ?: 0
}

fun Any?.toFloat(): Float {
    return this?.let {
        when (it) {
            is String -> it.toFloatOrNull() ?: 0f
            is Int -> it.toFloat()
            is Double -> it.toFloat()
            is Float -> it
            is Boolean -> if (it) 1f else 0f
            else -> 0f
        }
    } ?: 0f
}

fun Any?.toDouble(): Double {
    return this?.let {
        when (it) {
            is String -> it.toDoubleOrNull() ?: 0.0
            is Int -> it.toDouble()
            is Double -> it
            is Float -> it.toDouble()
            is Boolean -> if (it) 1.0 else 0.0
            else -> 0.0
        }
    } ?: 0.0
}

fun Any?.toBoolean(): Boolean {
    return this?.let {
        when (it) {
            is String -> it.toLowerCase().equals("true")
            is Int -> it.equals(1)
            is Double -> it.equals(1.0)
            is Float -> it.equals(1f)
            is Boolean -> it
            else -> false
        }
    } ?: false
}

fun <K> List<K>?.toList(): List<K> {
    return this?.let {
        if (it.isNotEmpty()) it
        else arrayListOf()
    } ?: arrayListOf<K>()
}