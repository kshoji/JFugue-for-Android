/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "Apache Software Foundation" must not be used to endorse or
 *    promote products derived from this software without prior written
 *    permission. For written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999-2001, Sun Microsystems,
 * Inc., http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */

package jp.kshoji.javax.xml.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * This class is duplicated for each JAXP subpackage so keep it in sync.
 * It is package private and therefore is not exposed as part of the JAXP
 * API.
 *
 * This code is designed to implement the JAXP 1.1 spec pluggability
 * feature.  The code runs both as part of an unbundled jar file and also
 * when bundled as part of the JDK.  Ideally the code should both compile
 * and run on JDK version 1.1 and later.  However, due to the complexities
 * of invoking Java 2 security methods via reflection, this code will only
 * compile on Java 2 although it will run under JDK 1.1 VMs.  As of 1may02
 * this file is on a "java2-branch".
 *
 * @author Edwin Goei
 */
class FactoryFinder {
    /** Controls debugging output to stderr */
    private static boolean debug;

    // Define system property "jaxp.debug" to get output
    static {
        try {
            String val =
                SecuritySupport.getInstance().getSystemProperty("jaxp.debug");
            // Allow simply setting the prop to turn on debug
            debug = val != null && (! "false".equals(val));
        } catch (SecurityException se) {
            debug = false;
        }
    }

    /**
     * Main entry point.  Finds and creates a new instance of a concrete
     * factory implementation in the specified order as stated in the JAXP
     * spec.  This code attempts to find a factory implementation in
     * serveral locations.  If one fails, the next one is tried.  To be
     * more robust, this occurs even if a SecurityException is thrown, but
     * perhaps it may be better to propogate the SecurityException instead,
     * so SecurityException-s are not masked.
     *
     * @return A new instance of the concrete factory class, never null
     *
     * @param factoryId
     *        Name of the factory to find, same as a property name
     *
     * @param fallbackClassName
     *        Implementation class name, if nothing else is found.  Use
     *        null to mean not to use a fallback.
     *
     * @throws jp.kshoji.javax.xml.parsers.FactoryFinder.ConfigurationError
     *         If a factory instance cannot be returned
     *
     * Package private so this code can be shared.
     */
    static Object find(String factoryId, String fallbackClassName)
        throws ConfigurationError
    {
        SecuritySupport ss = SecuritySupport.getInstance();

        // Figure out which ClassLoader to use for loading the provider
        // class.  If there is a Context ClassLoader then use it.
        ClassLoader cl = ss.getContextClassLoader();
        if (cl == null) {
            // Assert: we are on JDK 1.1 or we have no Context ClassLoader
            // so use the current ClassLoader
            cl = FactoryFinder.class.getClassLoader();
        }

        dPrint("find factoryId=" + factoryId);

        // Use the system property first
        try {
            String systemProp = ss.getSystemProperty(factoryId);
            if (systemProp != null) {
                dPrint("found system property, value=" + systemProp);
                return newInstance(systemProp, cl, true);
            }
        } catch (SecurityException se) {
            // Ignore and continue w/ next location
        }

        // Try to read from $java.home/lib/jaxp.properties
        try {
            String javah = ss.getSystemProperty("java.home");
            String configFile = javah + File.separator +
                "lib" + File.separator + "jaxp.properties";
            FileInputStream fis = ss.getFileInputStream(new File(configFile));
            Properties props = new Properties();
            props.load(fis);
            String factoryClassName = props.getProperty(factoryId);
            if (factoryClassName != null) {
                dPrint("found in jaxp.properties, value=" + factoryClassName);
                return newInstance(factoryClassName, cl, true);
            }
        } catch (Exception x) {
            // assert(x instanceof FileNotFoundException
            //        || x instanceof SecurityException)
            // In both cases, ignore and continue w/ next location
        }

        // Try Jar Service Provider Mechanism
        Object provider = findJarServiceProvider(factoryId);
        if (provider != null) {
            return provider;
        }

        if (fallbackClassName == null) {
            throw new ConfigurationError(
                "Provider for " + factoryId + " cannot be found", null);
        }

        dPrint("using fallback, value=" + fallbackClassName);
        return newInstance(fallbackClassName, cl, true);
    }

    private static void dPrint(String msg) {
        if (debug) {
            System.err.println("JAXP: " + msg);
        }
    }

    /**
     * Create an instance of a class using the specified ClassLoader and
     * optionally fall back to the current ClassLoader if not found.
     *
     * @param className Name of the concrete class corresponding to the
     * service provider
     *
     * @param cl ClassLoader to use to load the class, null means to use
     * the bootstrap ClassLoader
     *
     * @param doFallback true if the current ClassLoader should be tried as
     * a fallback if the class is not found using cl
     */
    private static Object newInstance(String className, ClassLoader cl,
                                      boolean doFallback)
        throws ConfigurationError
    {
        // assert(className != null);

        try {
            Class providerClass;
            if (cl == null) {
                // XXX Use the bootstrap ClassLoader.  There is no way to
                // load a class using the bootstrap ClassLoader that works
                // in both JDK 1.1 and Java 2.  However, this should still
                // work b/c the following should be true:
                //
                // (cl == null) iff current ClassLoader == null
                //
                // Thus Class.forName(String) will use the current
                // ClassLoader which will be the bootstrap ClassLoader.
                providerClass = Class.forName(className);
            } else {
                try {
                    providerClass = cl.loadClass(className);
                } catch (ClassNotFoundException x) {
                    if (doFallback) {
                        // Fall back to current classloader
                        cl = FactoryFinder.class.getClassLoader();
                        providerClass = cl.loadClass(className);
                    } else {
                        throw x;
                    }
                }
            }
            Object instance = providerClass.newInstance();
            dPrint("created new instance of " + providerClass +
                   " using ClassLoader: " + cl);
            return instance;
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError(
                "Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider " + className + " could not be instantiated: " + x,
                x);
        }
    }

    /*
     * Try to find provider using Jar Service Provider Mechanism
     *
     * @return instance of provider class if found or null
     */
    private static Object findJarServiceProvider(String factoryId)
        throws ConfigurationError
    {
        SecuritySupport ss = SecuritySupport.getInstance();
        String serviceId = "META-INF/services/" + factoryId;
        InputStream is = null;

        // First try the Context ClassLoader
        ClassLoader cl = ss.getContextClassLoader();
        if (cl != null) {
            is = ss.getResourceAsStream(cl, serviceId);

            // If no provider found then try the current ClassLoader
            if (is == null) {
                cl = FactoryFinder.class.getClassLoader();
                is = ss.getResourceAsStream(cl, serviceId);
            }
        } else {
            // No Context ClassLoader or JDK 1.1 so try the current
            // ClassLoader
            cl = FactoryFinder.class.getClassLoader();
            is = ss.getResourceAsStream(cl, serviceId);
        }

        if (is == null) {
            // No provider found
            return null;
        }

        dPrint("found jar resource=" + serviceId +
               " using ClassLoader: " + cl);

        // Read the service provider name in UTF-8 as specified in
        // the jar spec.  Unfortunately this fails in Microsoft
        // VJ++, which does not implement the UTF-8
        // encoding. Theoretically, we should simply let it fail in
        // that case, since the JVM is obviously broken if it
        // doesn't support such a basic standard.  But since there
        // are still some users attempting to use VJ++ for
        // development, we have dropped in a fallback which makes a
        // second attempt using the platform's default encoding. In
        // VJ++ this is apparently ASCII, which is a subset of
        // UTF-8... and since the strings we'll be reading here are
        // also primarily limited to the 7-bit ASCII range (at
        // least, in English versions), this should work well
        // enough to keep us on the air until we're ready to
        // officially decommit from VJ++. [Edited comment from
        // jkesselm]
        BufferedReader rd;
        try {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(is));
        }
        
        String factoryClassName = null;
        try {
            // XXX Does not handle all possible input as specified by the
            // Jar Service Provider specification
            factoryClassName = rd.readLine();
            rd.close();
        } catch (IOException x) {
            // No provider found
            return null;
        }

        if (factoryClassName != null &&
            ! "".equals(factoryClassName)) {
            dPrint("found in resource, value="
                   + factoryClassName);

            // Note: here we do not want to fall back to the current
            // ClassLoader because we want to avoid the case where the
            // resource file was found using one ClassLoader and the
            // provider class was instantiated using a different one.
            return newInstance(factoryClassName, cl, false);
        }

        // No provider found
        return null;
    }

    static class ConfigurationError extends Error {
        private Exception exception;

        /**
         * Construct a new instance with the specified detail string and
         * exception.
         */
        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        }

        Exception getException() {
            return exception;
        }
    }
}
