/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.grizzly.memory;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Grizzly;

import static org.glassfish.grizzly.memory.DefaultMemoryManagerFactory.DMMF_PROP_NAME;

class MemoryManagerInitializer {

    private static final String DMM_PROP_NAME =
            "org.glassfish.grizzly.DEFAULT_MEMORY_MANAGER";

    private static final Logger LOGGER =
            Grizzly.logger(MemoryManagerInitializer.class);


    // ------------------------------------------------- Package-Private Methods


    static MemoryManager initManager() {

        final MemoryManager mm = initMemoryManagerViaFactory();
        return (mm != null) ? mm : initMemoryManagerFallback();

    }


    // --------------------------------------------------------- Private Methods


    @SuppressWarnings("unchecked")
    private static MemoryManager initMemoryManagerViaFactory() {
        String dmmfClassName = System.getProperty(DMMF_PROP_NAME);
        if (dmmfClassName != null) {
            final DefaultMemoryManagerFactory mmf = newInstance(dmmfClassName);
            if (mmf != null) {
                return mmf.createMemoryManager();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static MemoryManager initMemoryManagerFallback() {
        final String className = System.getProperty(DMM_PROP_NAME);
        final MemoryManager mm = newInstance(className);
        return (mm != null) ? mm : new HeapMemoryManager();
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(final String className) {
        if (className == null) {
            return null;
        }
        try {
            Class clazz =
                    Class.forName(className,
                                  true,
                                  MemoryManager.class.getClassLoader());
            return (T) clazz.newInstance();
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE,
                           "Unable to load or create a new instance of Class {0}.  Cause: {1}",
                           new Object[]{className, e.getMessage()});
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, e.toString(), e);
            }
            return null;
        }
    }

}
