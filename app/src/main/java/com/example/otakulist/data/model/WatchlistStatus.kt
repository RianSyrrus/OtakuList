package com.example.otakulist.data.model

enum class WatchlistStatus(val value: String, val displayName: String) {
    BELUM("BELUM", "Belum"),
    SUDAH("SUDAH", "Sudah");

    val isFinished: Boolean get() = this == SUDAH

    companion object {
        val WATCHING = BELUM
        val PLAN_TO_WATCH = BELUM
        val COMPLETED = SUDAH
        val DROPPED = BELUM

        fun fromValue(value: String): WatchlistStatus {
            return if (value.equals("SUDAH", ignoreCase = true) || value.equals("COMPLETED", ignoreCase = true)) {
                SUDAH
            } else {
                BELUM
            }
        }
    }
}
