/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 *
 * @author jonathan
 */
public class DateUtils {

    /**
     * Using Calendar - THE CORRECT WAY*
     */
    public static int daysBetween(Date startDate, Date endDate) {
//        long daysBetween = 0;
//        if (startDate != null && endDate != null) {
//            Calendar date = Calendar.getInstance();
//            date.setTime(startDate);
//
//            Calendar dateEnd = Calendar.getInstance();
//            date.setTime(endDate);
//
//            while (date.before(dateEnd)) {
//                date.add(Calendar.DAY_OF_MONTH, 1);
//                daysBetween++;
//            }
//        }else{
//            throw new IllegalArgumentException("Las fechas no pueden ser null.");
//        }
//        return daysBetween;
        return Days.daysBetween(new DateTime(startDate), new DateTime(endDate)).getDays();
    }
}
