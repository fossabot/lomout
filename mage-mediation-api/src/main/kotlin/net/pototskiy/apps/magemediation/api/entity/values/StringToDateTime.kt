package net.pototskiy.apps.magemediation.api.entity.values

import net.pototskiy.apps.magemediation.api.source.workbook.SourceException
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

fun String.stringToDate(locale: Locale): DateTime {
    val format = DateTimeFormat.shortDate().withLocale(locale)
    return try {
        format.parseDateTime(this.trim())
    } catch (e: IllegalArgumentException) {
        throw SourceException("String can not be converted to date with locale<${locale.displayLanguage}_${locale.displayCountry}>.")
    } catch (e: UnsupportedOperationException) {
        throw SourceException("String can not be converted to date with locale<${locale.displayLanguage}_${locale.displayCountry}>.")
    }
}


fun String.stringToDateTime(pattern: String): DateTime {
    val format = DateTimeFormat.forPattern(pattern)
    return try {
        format.parseDateTime(this.trim())
    } catch (e: IllegalArgumentException) {
        throw SourceException("String can not be converted to date with pattern<$pattern>.")
    } catch (e: UnsupportedOperationException) {
        throw SourceException("String can not be converted to date with pattern<$pattern>.")
    }
}

fun String.stringToDateTime(locale: Locale): DateTime {
    val format = DateTimeFormat.shortDateTime().withLocale(locale)
    return try {
        format.parseDateTime(this.trim())
    } catch (e: IllegalArgumentException) {
        throw SourceException("String can not be converted to date-time with locale ${locale.displayLanguage}_${locale.displayCountry}.")
    } catch (e: UnsupportedOperationException) {
        throw SourceException("String can not be converted to date-time with locale ${locale.displayLanguage}_${locale.displayCountry}.")
    }
}

fun DateTime.dateToString(locale: Locale): String =
    this.toString(DateTimeFormat.shortDate().withLocale(locale))

fun DateTime.datetimeToString(locale: Locale): String =
    this.toString(DateTimeFormat.shortDateTime().withLocale(locale))

fun DateTime.datetimeToString(pattern: String): String = this.toString(pattern)