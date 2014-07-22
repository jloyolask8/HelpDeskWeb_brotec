package com.itcs.helpdesk.util;

import java.util.Date;

/**
 * Class for human-readable, pretty date formatting
 *
 * @author Lea Verou
 */
public class PrettyDate {

    public static String format(Date date) {
        if (date == null) {
            return "";
        }
        long current = (new Date()).getTime(),
                timestamp = date.getTime(),
                diff = (current - timestamp) / 1000;
        int amount = 0;
        String what = "";

        /**
         * Second counts 3600: hour 86400: day 604800: week 2592000: month
         * 31536000: year
         */
        if (diff > 31536000) {
            amount = (int) (diff / 31536000);
            what = "año";
        } else if (diff > 2592000) {
            amount = (int) (diff / 2592000);
            what = "mes";
        } else if (diff > 604800) {
            amount = (int) (diff / 604800);
            what = "semana";
        } else if (diff > 86400) {
            amount = (int) (diff / 86400);
            what = "día";
        } else if (diff > 3600) {
            amount = (int) (diff / 3600);
            what = "hora";
        } else if (diff > 60) {
            amount = (int) (diff / 60);
            what = "minuto";
        } else if (diff > 0) {
            amount = (int) diff;
            what = "segundo";
            if (amount < 6) {
                return "justo ahora";
            }
        } else {
            //Futuro
            if (diff < -31536000) {
                amount = (int) (diff / 31536000);
                what = "año";
            } else if (diff < -2592000) {
                amount = (int) (diff / 2592000);
                what = "mes";
            } else if (diff < -604800) {
                amount = (int) (diff / 604800);
                what = "semana";
            } else if (diff < -86400) {
                amount = (int) (diff / 86400);
                what = "día";
            } else if (diff < -3600) {
                amount = (int) (diff / 3600);
                what = "hora";
            } else if (diff < -60) {
                amount = (int) (diff / 60);
                what = "minuto";
            } else {
                amount = (int) diff;
                what = "segundo";
                if (amount > -6) {
                    return "justo ahora";
                }
            }

            if (amount == 1) {
                if (what.equals("día")) {
                    return "Ayer";
                } else if (what.equals("semana")) {
                    return "en 1 semana";
                } else if (what.equals("mes")) {
                    return "en 1 mes";
                } else if (what.equals("año")) {
                    return "en 1 año";
                }
            } else {
                what += "s";
            }

            return "en " + (amount*(-1)) + " " + what;
        }

        if (amount == 1) {
            if (what.equals("día")) {
                return "Ayer";
            } else if (what.equals("semana")) {
                return "hace 1 semana";
            } else if (what.equals("mes")) {
                return "hace 1 mes";
            } else if (what.equals("año")) {
                return "hace 1 año";
            }
        } else {
            what += "s";
        }

        return "hace " + amount + " " + what;
    }
}
