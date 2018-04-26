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

package io.shardingjdbc.proxy.backend.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.shardingjdbc.proxy.backend.common.BackendHandler;
import io.shardingjdbc.proxy.backend.common.CommandResponsePacketsHandler;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandResponsePackets;

/**
 * Backend handler.
 *
 * @author wangkai
 */
public class MySQLBackendHandler extends CommandResponsePacketsHandler {
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        //TODO 判断报文是握手还是
        if (true) {
            auth(context,(ByteBuf) message);
        } else {
        
        }
    }
    
    @Override
    protected void auth(ChannelHandlerContext context, ByteBuf message) {
    
    }
    
    @Override
    protected void executeCommandResponsePackets(ChannelHandlerContext context, ByteBuf message) {
    
    }

}
