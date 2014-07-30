/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author jorge
 */
public class ClassUtils {

    /**
     * Private helper method.
     *
     * @param connection the connection to the jar
     * @param pckgname the package name to search for
     * @param classes the current ArrayList of all classes. This method will
     * simply add new classes.
     * @throws ClassNotFoundException if a file isn't loaded but still is in the
     * jar file
     * @throws IOException if it can't correctly read from the jar file.
     */
    private static void checkJarFile(JarURLConnection connection,
            String pckgname, ArrayList<Class<?>> classes)
            throws ClassNotFoundException, IOException {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry = null; entries.hasMoreElements()
                && ((jarEntry = entries.nextElement()) != null);) {
            name = jarEntry.getName();

            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');

                if (name.contains(pckgname)) {
                    classes.add(Class.forName(name));
                }
            }
        }
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     *
     * @param pckgname the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException if something went wrong
     */
    public static List<Class<?>> getClassesForPackage(String pckgname)
            throws ClassNotFoundException {
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

        try {
            final ClassLoader cld = Thread.currentThread()
                    .getContextClassLoader();

            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }

            final Enumeration<URL> resources = cld.getResources(pckgname
                    .replace('.', '/'));
            URLConnection connection;

            for (URL url = null; resources.hasMoreElements()
                    && ((url = resources.nextElement()) != null);) {
                try {
                    connection = url.openConnection();

                    if (connection instanceof JarURLConnection) {
                        checkJarFile((JarURLConnection) connection, pckgname,
                                classes);
                    } else {
                        throw new ClassNotFoundException(pckgname + " ("
                                + url.getPath()
                                + ") does not appear to be a valid package");
                    }
                } catch (final IOException ioex) {
                    throw new ClassNotFoundException(
                            "IOException was thrown when trying to get all resources for "
                            + pckgname, ioex);
                }
            }
        } catch (final NullPointerException ex) {
            throw new ClassNotFoundException(
                    pckgname
                    + " does not appear to be a valid package (Null pointer exception)",
                    ex);
        } catch (final IOException ioex) {
            throw new ClassNotFoundException(
                    "IOException was thrown when trying to get all resources for "
                    + pckgname, ioex);
        }

        return classes;
    }
}
