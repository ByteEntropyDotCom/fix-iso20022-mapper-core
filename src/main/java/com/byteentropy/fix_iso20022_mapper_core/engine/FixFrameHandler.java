package com.byteentropy.fix_iso20022_mapper_core.engine;

import com.byteentropy.fix_iso20022_mapper_core.core.PipelineDispatcher;
import com.byteentropy.fix_iso20022_mapper_core.model.RawFixMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class FixFrameHandler extends SimpleChannelInboundHandler<String> {
    private final PipelineDispatcher dispatcher;

    public FixFrameHandler(PipelineDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // Wrap and hand off to Virtual Threads immediately
        dispatcher.dispatch(new RawFixMessage(msg, System.nanoTime()));
    }
}