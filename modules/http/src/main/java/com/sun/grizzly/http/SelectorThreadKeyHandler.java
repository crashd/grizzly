/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
 *
 */
package com.sun.grizzly.http;

import com.sun.grizzly.DefaultSelectionKeyHandler;
import com.sun.grizzly.SelectionKeyHandler;
import com.sun.grizzly.util.Copyable;
import com.sun.grizzly.util.SelectionKeyAttachment;
import java.nio.channels.SelectionKey;
import java.util.Iterator;

/**
 * Default HTTP {@link SelectionKeyHandler} implementation
 *
 * @author Jean-Francois Arcand
 * @author Alexey Stashok
 */
public class SelectorThreadKeyHandler extends DefaultSelectionKeyHandler {

    private SelectorThread selectorThread;

    public SelectorThreadKeyHandler() {
    }

    public SelectorThreadKeyHandler(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    @Override
    public void copyTo(Copyable copy) {
        super.copyTo(copy);
        SelectorThreadKeyHandler copyHandler = (SelectorThreadKeyHandler) copy;
        copyHandler.selectorThread = selectorThread;
    }

    @Override
    public void cancel(SelectionKey key) {
        if (key != null) {
            if (selectorThread.getThreadPool() instanceof StatsThreadPool) {
                if (selectorThread.isMonitoringEnabled() &&
                        ((StatsThreadPool) selectorThread.getThreadPool()).getStatistic().decrementOpenConnectionsCount(key.channel())) {
                    selectorThread.getRequestGroupInfo().decreaseCountOpenConnections();
                }
            }
            super.cancel(key);
        }
    }

    @Override
    public void doRegisterKey(SelectionKey key, int ops, long currentTime) {
        if (!key.isValid()){
            selectorHandler.addPendingKeyCancel(key);
        }else{
            key.interestOps(key.interestOps() | ops);
            addExpirationStamp(key,currentTime);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void expire(Iterator<SelectionKey> iterator) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime < nextKeysExpiration) {
            return;
        }
        nextKeysExpiration = currentTime + 1000;
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            if (!key.isValid()) {
                continue;
            }

            Object attachment = key.attachment();
            if (attachment != null) {
                long expire = getExpirationStamp(attachment);

                if (expire != SelectionKeyAttachment.UNLIMITED_TIMEOUT) {
                    long idleLimit = getIdleLimit(attachment);

                    if (idleLimit != -1 && currentTime - expire >= idleLimit &&
                        (!(attachment instanceof SelectionKeyAttachment) ||
                        ((SelectionKeyAttachment)attachment).timedOut(key))){
                            selectorHandler.addPendingKeyCancel(key);
                        }
                }
            }
        }
    }

    /**
     * returns idle limit
     * @param attachment
     * @return
     */
    private long getIdleLimit(Object attachment){
        if (attachment instanceof SelectionKeyAttachment){  
            long idleLimit = ((SelectionKeyAttachment) attachment).getIdleTimeoutDelay();
            if (idleLimit != SelectionKeyAttachment.UNLIMITED_TIMEOUT) {
                return idleLimit;
            }
        }
        return timeout;
    }
}
