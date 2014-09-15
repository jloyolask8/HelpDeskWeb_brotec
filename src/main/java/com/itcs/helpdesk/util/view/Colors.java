/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util.view;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jonathan
 */
public class Colors {

    public static final String COLOR2 = "f83b22";
    public static final String COLOR3 = "ff7637";//rgb(255, 117, 55)
    public static final String COLOR4 = "ffac46";//rgb(255, 173, 70)";
    public static final String COLOR5 = "42d691";//rgb(66, 214, 146)";
    public static final String COLOR6 = "16a766";//rgb(22, 167, 101)";
    public static final String COLOR7 = "7ad148";//rgb(123, 209, 72)";
    public static final String COLOR8 = "b1dc6c";//rgb(179, 220, 108)";
    public static final String COLOR9 = "FBE983";
    public static final String COLOR10 = "FAD165";
    public static final String COLOR11 = "91e1c0";//rgb(146, 225, 192)";
    public static final String COLOR12 = "9FE1E7";
    public static final String COLOR13 = "9FC6E7";
    public static final String COLOR14 = "4986E7";
    public static final String COLOR15 = "9a9cff";//rgb(154, 156, 255)";
    public static final String COLOR16 = "b89aff";//rgb(185, 154, 255)";
    public static final String COLOR17 = "c2c2c2";//rgb(194, 194, 194)";
    public static final String COLOR18 = "cabdbf";//rgb(202, 189, 191)";
    public static final String COLOR19 = "cca6ab";//rgb(204, 166, 172)";
    public static final String COLOR20 = "f691b2";//rgb(246, 145, 178)";
    public static final String COLOR21 = "cd74e6";//rgb(205, 116, 230)";
    public static final String COLOR22 = "a47ae2";//rgb(164, 122, 226)";

    private static final String[] availableColors = new String[]{
        COLOR2,  COLOR5, COLOR6, COLOR3, COLOR4,
        COLOR7, COLOR8, COLOR9, COLOR10, COLOR11, COLOR12,
        COLOR13, COLOR14, COLOR15, COLOR16, COLOR17, COLOR18,
        COLOR19, COLOR20, COLOR21, COLOR22
    };

    private static List<String> colorsInUse = null;

    /**
     * circular color provider
     *
     * @return
     */
    public static String getNextColor() {
        if (colorsInUse == null) {
            colorsInUse = new LinkedList<String>();
            colorsInUse.addAll(Arrays.asList(availableColors));
        }
        if (!colorsInUse.isEmpty()) {
            String color = colorsInUse.get(0);
            colorsInUse.remove(0);
            return color;
        } else {
            colorsInUse.addAll(Arrays.asList(availableColors));
            String color = colorsInUse.get(0);
            colorsInUse.remove(0);
            return color;
        }
    }
}
