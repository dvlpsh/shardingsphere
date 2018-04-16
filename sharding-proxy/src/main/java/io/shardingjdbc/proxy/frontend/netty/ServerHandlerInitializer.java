/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.frontend.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.proxy.frontend.common.FrontendHandlerFactory;
import io.shardingjdbc.proxy.transport.common.codec.PacketCodecFactory;
import lombok.AllArgsConstructor;

/**
 * init  Channel.
 * @author xiaoyu
 */
@AllArgsConstructor
public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private EventLoopGroup userGroup;

    @Override
    protected void initChannel(final SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // TODO load database type from yaml or startup arguments
        pipeline.addLast(PacketCodecFactory.createPacketCodecInstance(DatabaseType.MySQL));
        pipeline.addLast(FrontendHandlerFactory.createFrontendHandlerInstance(DatabaseType.MySQL, userGroup));

    }
}
