package com.app.famcare.model

data class BookingDaily(
    val bookID: String,
    val nannyName: String,
    val bookDate: String,
    val bookHours: String,
    var endHours: String,
    val type: BookingType
)

data class BookingMonthly(
    val bookID: String,
    val nannyName: String,
    val startDate: String,
    val endDate: String,
    val type: BookingType
)

data class BookingDailyHistory(
    val bookID: String = "",
    val nannyName: String = "",
    val bookDate: String = "",
    val bookHours: String = "",
    var bookDuration: String = "",
    var startTime: String = "",
    var endHours: String = "",
    var salary: String = "",
    val type: BookingType = BookingType.DAILY,
    val nannyID: String = "",
    val totalCost: String = "",
    val totalPricing: Long = 0,
)

data class BookingMonthlyHistory(
    val bookID: String = "",
    val nannyName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val type: BookingType = BookingType.MONTHLY,
    val nannyID: String = "",
    val totalCost: String = "",
    val totalPricing: Long = 0,
)

enum class BookingType {
    DAILY, MONTHLY
}