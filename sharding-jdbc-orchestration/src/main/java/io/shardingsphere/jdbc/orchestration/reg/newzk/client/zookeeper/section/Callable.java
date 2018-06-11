/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section;

import lombok.Setter;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * sync retry call
 *
 * @author lidongbo
 */
public abstract class Callable<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Callable.class);

    private final io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayPolicyExecutor delayPolicyExecutor;
    
    private final io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider provider;
    
    @Setter
    private T result;
    
    public Callable(final io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider provider, final io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayRetryPolicy delayRetryPolicy) {
        this.delayPolicyExecutor = new io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayPolicyExecutor(delayRetryPolicy);
        this.provider = provider;
    }
    
    /**
     * call.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public abstract void call() throws KeeperException, InterruptedException;
    
    /**
     * get result.
     *
     * @return result
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public T getResult() throws KeeperException, InterruptedException {
        if (result == null) {
            exec();
        }
        return result;
    }
    
    /**
     * call without result.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public void exec() throws KeeperException, InterruptedException {
        try {
            call();
        } catch (KeeperException e) {
            LOGGER.warn("exec KeeperException:{}", e.getMessage());
            delayPolicyExecutor.next();
            if (io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Connection.needReset(e)) {
                provider.resetConnection();
            } else {
                throw e;
            }
            execDelay();
        } catch (InterruptedException e) {
            throw e;
        }
    }
    
    private void execDelay() throws KeeperException, InterruptedException {
        for (;;) {
            long delay = delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
            if (delay > 0) {
                try {
                    LOGGER.debug("exec delay:{}", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw e;
                }
            } else {
                if (delayPolicyExecutor.hasNext()) {
                    LOGGER.debug("exec hasNext");
                    exec();
                }
                break;
            }
        }
    }
}
