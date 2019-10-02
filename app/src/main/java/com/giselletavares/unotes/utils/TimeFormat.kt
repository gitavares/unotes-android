package com.giselletavares.unotes.utils

import java.text.SimpleDateFormat
import java.util.*

public fun Calendar.formatOnPattern(pattern: String, date: Date = Date(), locale: Locale = Locale("en", "CA")): String {
     this.time = date
     return SimpleDateFormat(pattern, locale).format(this.time)
}

public fun Date.formatOnPattern(pattern: String, locale: Locale = Locale("en", "CA")): String {
     return SimpleDateFormat(pattern, locale).format(this)
}

object DatePattern{
     const val dateTimeForIdFormatter = "yyyyMMddHHmmss"
     const val dateTimeForNotificationIdFormatter = "MMddHHmm"
     const val dateShortFormatter = "dd/MM"
     const val dateMediumFormatter = "dd/MM/yyyy"
     const val dateDescriptiveShortFormatter = "MMM dd, yy"
     const val dateLongFormatter = "dd/MM/yyyy hh:mm a"
     const val hourFormatter = "hh:mm a"
}