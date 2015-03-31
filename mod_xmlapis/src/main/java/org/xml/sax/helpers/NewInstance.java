// NewInstance.java - create a new instance of a class by name.
// http://www.saxproject.org
// Written by Edwin Goei, edwingo@apache.org
// and by David Brownell, dbrownell@users.sourceforge.net
// NO WARRANTY!  This class is in the Public Domain.
// $Id: NewInstance.java,v 1.2 2002/08/26 23:55:45 neilg Exp $

package org.xml.sax.helpers;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Create a new instance of a class by name.
 *
 * <blockquote>
 * <em>This module, both source code and documentation, is in the
 * Public Domain, and comes with <strong>NO WARRANTY</strong>.</em>
 * See <a href='http://www.saxproject.org'>http://www.saxproject.org</a>
 * for further information.
 * </blockquote>
 *
 * <p>This class contains a static method for creating an instance of a
 * class from an explicit class name.  It tries to use the thread's context
 * ClassLoader if possible and falls back to using
 * Class.forName(String).  It also takes into account JDK 1.2+'s
 * AccessController mechanism for performing its actions.  </p>
 *
 * <p>This code is designed to run on JDK version 1.1 and later and compile
 * on versions of Java 2 and later.</p>
 *
 * @author Edwin Goei, David Brownell, Neil Graham
 * @version $Id: NewInstance.java,v 1.2 2002/08/26 23:55:45 neilg Exp $
 */
class NewInstance {

    // constants

    // governs whether, if we fail in finding a class even
    // when given a classloader, we'll make a last-ditch attempt
    // to use the current classloader.  
    private static final boolean DO_FALLBACK = true;

    /**
     * Creates a new instance of the specified class name
     *
     * Package private so this code is not exposed at the API level.
     */
    static Object newInstance (ClassLoader cl, String className)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException
    {

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
                if (DO_FALLBACK) {
                    // Fall back to current classloader
                    cl = NewInstance.class.getClassLoader();
                    providerClass = cl.loadClass(className);
                } else {
                    throw x;
                }
            }
        }
        Object instance = providerClass.newInstance();
        return instance;
    }

    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use
     * the context ClassLoader.
     */           
    static ClassLoader getClassLoader ()
    {

        SecuritySupport ss = SecuritySupport.getInstance();

        // Figure out which ClassLoader to use for loading the provider
        // class.  If there is a Context ClassLoader then use it.
        ClassLoader cl = ss.getContextClassLoader();
        if (cl == null) {
            // Assert: we are on JDK 1.1 or we have no Context ClassLoader
            // so use the current ClassLoader
            cl = NewInstance.class.getClassLoader();
        }
        return cl;

    }
}
