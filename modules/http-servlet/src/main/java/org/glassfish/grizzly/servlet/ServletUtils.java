/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.grizzly.servlet;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 * Grizzly Servlet utilities.
 * 
 * @author Alexey Stashok
 */
public class ServletUtils {
    /**
     * Returns internal Grizzly {@link Request} associated with the passed
     * {@link HttpServletRequest}.
     * 
     * @param servletRequest {@link HttpServletRequest}
     * 
     * @throws IllegalArgumentException if passed {@link HttpServletRequest}
     *      is not based on Grizzly {@link Request}.
     * 
     * @return internal Grizzly {@link Request} associated with the passed
     * {@link HttpServletRequest}.
     */
    public static Request getInternalRequest(HttpServletRequest servletRequest) {
        if (servletRequest instanceof Holders.RequestHolder) {
            return ((Holders.RequestHolder) servletRequest).getInternalRequest();
        }
        
        throw new IllegalArgumentException("Passed HttpServletRequest is not based on Grizzly");
    }
    
    /**
     * Returns internal Grizzly {@link Response} associated with the passed
     * {@link HttpServletResponse}.
     * 
     * @param servletResponse {@link HttpServletResponse}
     * 
     * @throws IllegalArgumentException if passed {@link HttpServletResponse}
     *      is not based on Grizzly {@link Response}.
     * 
     * @return internal Grizzly {@link Response} associated with the passed
     * {@link HttpServletResponse}.
     */
    public static Response getInternalResponse(HttpServletResponse servletResponse) {
        if (servletResponse instanceof Holders.ResponseHolder) {
            return ((Holders.ResponseHolder) servletResponse).getInternalResponse();
        }
        
        throw new IllegalArgumentException("Passed HttpServletResponse is not based on Grizzly");
    }
    
    /**
     * Returns a set of all servlet name or url pattern mappings that have
     * been defined across all registered Filters.
     *
     */
    static Set<String> getUnifiedKeyView(final Map<String[],Byte> map) {
        Set<String> names;
        if (!map.isEmpty()) {
            names = new LinkedHashSet<String>();
            for (final String[] mappings : map.keySet()) {
                for (int i = 0, len = mappings.length; i < len; i++) {
                    names.add(mappings[i]);
                }
            }
        } else {
            names = Collections.emptySet();
        }
        return names;
    }    
}
