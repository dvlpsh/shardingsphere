package com.saaavsaaa.client.untils;

import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa on 18-4-23.
 */
public interface Listener {
    void process(WatchedEvent event);
}
