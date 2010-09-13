/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.grizzly;

/**
 * Result of read operation, retuned by {@link Readable}.
 * 
 * @author Alexey Stashok
 */
public class ReadResult<K, L> implements Result, Cacheable {
    private static final ThreadCache.CachedTypeIndex<ReadResult> CACHE_IDX =
            ThreadCache.obtainIndex(ReadResult.class, 4);

    private boolean isRecycled = false;

    public static ReadResult create(Connection connection) {
        final ReadResult readResult = ThreadCache.takeFromCache(CACHE_IDX);
        if (readResult != null) {
            readResult.connection = connection;
            readResult.isRecycled = false;
            return readResult;
        }

        return new ReadResult(connection);
    }

    public static <K, L> ReadResult create(Connection connection,
            K message, L srcAddress, int readSize) {
        final ReadResult readResult = ThreadCache.takeFromCache(CACHE_IDX);
        if (readResult != null) {
            readResult.connection = connection;
            readResult.message = message;
            readResult.srcAddress = srcAddress;
            readResult.readSize = readSize;
            readResult.isRecycled = false;
            
            return readResult;
        }

        return new ReadResult(connection, message, srcAddress, readSize);

    }

    /**
     * Connection, from which data were read.
     */
    private Connection connection;

    /**
     * message data
     */
    private K message;

    /**
     *  Source address.
     */

    private L srcAddress;

    /**
     * Number of bytes read.
     */
    private int readSize;

    protected ReadResult(Connection connection) {
        this(connection, null, null, 0);
    }

    protected ReadResult(Connection connection, K message, L srcAddress,
            int readSize) {
        this.connection = connection;
        this.message = message;
        this.srcAddress = srcAddress;
        this.readSize = readSize;
    }

    /**
     * Get the {@link Connection} data were read from.
     * 
     * @return the {@link Connection} data were read from.
     */
    @Override
    public final Connection getConnection() {
        checkRecycled();
        return connection;
    }

    /**
     * Get the message, which was read.
     * 
     * @return the message, which was read.
     */
    public final K getMessage() {
        checkRecycled();
        return message;
    }

    /**
     * Set the message, which was read.
     *
     * @param message the message, which was read.
     */
    public final void setMessage(K message) {
        checkRecycled();
        this.message = message;
    }

    /**
     * Get the source address, the message was read from.
     *
     * @return the source address, the message was read from.
     */
    public final L getSrcAddress() {
        checkRecycled();
        return srcAddress;
    }

    /**
     * Set the source address, the message was read from.
     *
     * @param srcAddress the source address, the message was read from.
     */
    public final void setSrcAddress(L srcAddress) {
        checkRecycled();
        this.srcAddress = srcAddress;
    }

    /**
     * Get the number of bytes, which were read.
     *
     * @return the number of bytes, which were read.
     */
    public final int getReadSize() {
        checkRecycled();
        return readSize;
    }

    /**
     * Set the number of bytes, which were read.
     *
     * @param readSize the number of bytes, which were read.
     */
    public final void setReadSize(int readSize) {
        checkRecycled();
        this.readSize = readSize;
    }

    private void reset() {
        connection = null;
        message = null;
        srcAddress = null;
        readSize = 0;
    }

    private void checkRecycled() {
        if (Grizzly.isTrackingThreadCache() && isRecycled)
            throw new IllegalStateException("ReadResult has been recycled!");
    }
    
    @Override
    public void recycle() {
        reset();
        isRecycled = true;
        ThreadCache.putToCache(CACHE_IDX, this);
    }
}
