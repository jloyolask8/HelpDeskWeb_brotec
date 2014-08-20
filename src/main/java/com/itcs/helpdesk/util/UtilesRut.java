package com.itcs.helpdesk.util;

/**
 * Clase encargada de formatear el rut con puntos y guiones, ademas de validarlo
 * con su digito verificador.
 */
public class UtilesRut {

    public static synchronized String formatear(String rut) {
        String formattedVal = "";
        if (rut != null && !rut.trim().isEmpty()) {

            int cont = 0;
            String format;
            rut = rut.replace(" ", "");
            rut = rut.replace(".", "");
            rut = rut.replace("-", "");
            rut = rut.replace("k", "K");
            format = "-" + rut.substring(rut.length() - 1);
            for (int i = rut.length() - 2; i >= 0; i--) {
                format = rut.substring(i, i + 1) + format;
                cont++;
                if (cont == 3 && i != 0) {
                    format = "." + format;
                    cont = 0;
                }
            }
            formattedVal = format.toUpperCase();
        }
        return formattedVal;
    }

    public static int getAsNumber(String rut) {
        try {
            String rutInt = rut.replace(".", "");
            rutInt = rutInt.replace("-", "");
            rutInt = rutInt.replace("k", "1");
            rutInt = rutInt.replace("K", "1");

            return Integer.valueOf(rutInt);

        } catch (Exception ex) {
            return 0;
        }
    }

//    public static void main(String[] args) {
//        System.out.println("Valid:" + UtilesRut.validar("96.792.430-k"));
//    }

    public static boolean validar(String rut) {
        try {
            int dvR, dvT, suma = 0;
            int[] serie = {
                2, 3, 4, 5, 6, 7
            };
            rut = rut.replace(".", "");
            rut = rut.replace("-", "");
            if (rut.substring(rut.length() - 1).equalsIgnoreCase("K")) {
                dvR = 10;
            } else {
                dvR = Integer.valueOf(rut.substring(rut.length() - 1));
            }
            for (int i = rut.length() - 2; i >= 0; i--) {
                suma += Integer.valueOf(rut.substring(i, i + 1)) * serie[(rut.length() - 2 - i) % 6];
            }
            dvT = 11 - suma % 11;
            if (dvT == 11) {
                dvT = 0;
            }
//            System.out.println("dvt: " + dvT + " dvr:" + dvR);
            if (dvT == dvR) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }
}
