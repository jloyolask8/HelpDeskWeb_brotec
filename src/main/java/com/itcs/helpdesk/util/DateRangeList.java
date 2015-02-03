/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 *
 * @author jonathan
 */
public class DateRangeList implements Iterable<Interval> {

    private final DateTime start;
    private final DateTime end;
    private final int plusPeriod;

    public DateRangeList(Date start,
            Date end, int plusPeriod) {
        this.start = new DateTime(start/*, DateTimeZone.UTC*/);
        this.end = new DateTime(end/*, DateTimeZone.UTC*/);
        this.plusPeriod = plusPeriod;
    }

    @Override
    public Iterator<Interval> iterator() {
        return new LocalDateRangeIterator(start, end, plusPeriod);
    }

    private static class LocalDateRangeIterator implements Iterator<Interval> {

        private Interval interval;
        private DateTime start;
        private final DateTime end;
        private final int plusPeriod;

        private LocalDateRangeIterator(DateTime start,
                DateTime end, int plusPeriod) {
            this.start = start;
            this.end = end;
            this.plusPeriod = plusPeriod;
        }

        @Override
        public boolean hasNext() {
            return start != null;
        }

        @Override
        public Interval next() {
            if (start == null) {
                throw new NoSuchElementException();
            }

            switch (plusPeriod) {
                case Calendar.MONTH:

                    final DateTime monthStart = start.withDayOfMonth(1);
//                    Calendar monthEnd = Calendar.getInstance();
//                    monthEnd.setTime(start.toDate());
                    final int lastDayOfMonth = monthStart.dayOfMonth().getMaximumValue();//monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
//                    monthEnd.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                    final DateTime monthEnd = monthStart.dayOfMonth().setCopy(lastDayOfMonth);

                    //System.out.println("Start:" + monthStart + "\n lastDayOfMonth:" + lastDayOfMonth + "\n End: " + monthEnd);

                    interval = new Interval(monthStart, monthEnd);

                    start = monthStart.plusMonths(1);
                    break;
                case Calendar.DATE:

                    Date dayEnd = new Date(start.withTimeAtStartOfDay().toDate().getTime() + (24 * 60 * 60 * 1000)); //here i add the days of the period, i can increase by days, weeks, months or years.
                    interval = new Interval(new DateTime(start.withTimeAtStartOfDay()), new DateTime(dayEnd.getTime()));
                    start = start.plusDays(1);
                    break;
                case Calendar.WEEK_OF_MONTH:

                    Date weekEnd = new Date(start.withTimeAtStartOfDay().toDate().getTime() + ((7 * 24 * 60 * 60 * 1000) - 1));
                    interval = new Interval(new DateTime(start.withTimeAtStartOfDay()), new DateTime(weekEnd.getTime()));
                    start = start.plusWeeks(1);
                    break;
                case Calendar.YEAR:
                    start = start.withDayOfYear(1);
                    Calendar yearEnd = Calendar.getInstance();
                    yearEnd.setTime(start.toDate());
                    int lastDayOfYear = yearEnd.getActualMaximum(Calendar.DAY_OF_YEAR);
                    yearEnd.set(Calendar.DAY_OF_YEAR, lastDayOfYear);
                    interval = new Interval(new DateTime(start.withTimeAtStartOfDay()), new DateTime(yearEnd.getTime()));

                    start = start.plusYears(1);
                    break;
                default:
                    throw new UnsupportedOperationException();

            }

            if (start.compareTo(end) > 0) {
                start = null;
            }
            return interval;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
