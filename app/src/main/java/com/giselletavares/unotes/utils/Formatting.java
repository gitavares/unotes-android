package com.giselletavares.unotes.utils;

import android.annotation.TargetApi;
import android.icu.util.LocaleData;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Formatting {

    Locale locale = new Locale("en", "CA");
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
    SimpleDateFormat dateTimeForIdFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat dateTimeForNotificationIdFormatter = new SimpleDateFormat("MMddHHmm");
    SimpleDateFormat dateShortFormatter = new SimpleDateFormat("dd/MM");
    SimpleDateFormat dateMediumFormatter = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dateDescriptiveShortFormatter = new SimpleDateFormat("MMM dd, yy");
    SimpleDateFormat dateLongFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
    SimpleDateFormat hourFormatter = new SimpleDateFormat("hh:mm a");

    public Formatting() { }

    public String getCurrencyFormatter(Double value) {
        return currencyFormatter.format(value);
    }

    public String getDateTimeForIdFormatter(Date value) {
        return dateTimeForIdFormatter.format(value);
    }

    public String getDateTimeForNotificationIdFormatter(Date value) {
        return dateTimeForNotificationIdFormatter.format(value);
    }

    public String getDateShortFormatter(Date value) {
        return dateShortFormatter.format(value);
    }

    public String getDateMediumFormatter(Date value) {
        return dateMediumFormatter.format(value);
    }

    public String getDateDescriptiveShortFormatter(Date value) {
        return dateDescriptiveShortFormatter.format(value);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public String getDateLongFormatter(Date value) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        Date yesterdayDate = cal.getTime();

        if(getDateMediumFormatter(value).compareTo(getDateMediumFormatter(new Date())) == 0){
            return "Today at " + hourFormatter.format(value);
        } else if(getDateMediumFormatter(value).compareTo(getDateMediumFormatter(yesterdayDate)) == 0){
            return "Yesterday at " + hourFormatter.format(value);
        }
        return dateLongFormatter.format(value);
    }

}
