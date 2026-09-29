package com.example.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseDateController {

    protected Date parseDate(String value) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(value);
    }

    protected Date addOneDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    protected DateRange resolveDateRange(String dateStartString,
                                         String dateEndString,
                                         boolean inclusiveEndDay) throws ParseException {
        Date today = new Date();

        Date dateStart;
        if (dateStartString == null || dateStartString.trim().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            dateStart = calendar.getTime();
        } else {
            dateStart = parseDate(dateStartString);
        }

        Date dateEnd;
        if (dateEndString == null || dateEndString.trim().isEmpty()) {
            dateEnd = today;
        } else {
            dateEnd = parseDate(dateEndString);
        }

        if (inclusiveEndDay) {
            dateEnd = addOneDay(dateEnd);
        }

        return new DateRange(dateStart, dateEnd);
    }

    protected static class DateRange {
        private final Date dateStart;
        private final Date dateEnd;

        public DateRange(Date dateStart, Date dateEnd) {
            this.dateStart = dateStart;
            this.dateEnd = dateEnd;
        }

        public Date getDateStart() {
            return dateStart;
        }

        public Date getDateEnd() {
            return dateEnd;
        }
    }
}