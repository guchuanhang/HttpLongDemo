package com.example.gch;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Iterator;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 客户端与服务端创建连接的时候调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端与服务端连接开始...");
        NettyConfig.group.add(ctx.channel());
    }

    /**
     * 客户端与服务端断开连接时调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端与服务端连接关闭...");
        NettyConfig.group.remove(ctx.channel());
    }

    /**
     * 服务端接收客户端发送过来的数据结束之后调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        System.out.println("信息接收完毕...");
    }

    /**
     * 工程出现异常的时候调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 服务端处理客户端websocket请求的核心方法，这里接收了客户端发来的信息
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object info) throws Exception {
        ByteBuf result = (ByteBuf) info;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        System.out.println("我是服务端，我接受到了：" + new String(result1));
        //服务端使用这个就能向 每个连接上来的客户端群发消息
//        NettyConfig.group.writeAndFlush(info);
        channelHandlerContext.writeAndFlush(info);

        Iterator<Channel> iterator = NettyConfig.group.iterator();
        while (iterator.hasNext()) {
            //打印出所有客户端的远程地址
            System.out.println((iterator.next()).remoteAddress());
        }
        try {
            Thread.sleep(3000);
            // 向客户端发送消息
            String response = "hello client";
            // 在当前场景下，发送的数据必须转换成ByteBuf数组
            ByteBuf encoded = channelHandlerContext.alloc().buffer(4 * response.length());
            encoded.writeBytes(response.getBytes());
            //单聊
//            channelHandlerContext.write(encoded);
//            channelHandlerContext.flush();
            //群聊
            NettyConfig.group.writeAndFlush(encoded);
        } catch (Exception e) {
            e.printStackTrace();
        }


//    	//单独回复客户端信息
//    	channelHandlerContext.writeAndFlush(info);
//
//        ByteBuf result = (ByteBuf) info;
//
//
//        //服务端使用这个就能向 每个连接上来的客户端群发消息
//        NettyConfig.group.writeAndFlush(info);
//
//        System.out.println("我是服务端，我接受到了：" + ((MsgBean) info).getInfo());
//        byte[] result1 = new byte[result.readableBytes()];
//        result.readBytes(result1);
//        System.out.println("Server said:" + new String(result1));
//        //TODO write ?
//        try {
//            Thread.sleep(3000);
//            // 向客户端发送消息
//            String response = "hello every client!";
//            // 在当前场景下，发送的数据必须转换成ByteBuf数组
//            ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
//            encoded.writeBytes(response.getBytes());
//            ctx.write(encoded);
//            ctx.flush();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//

    }
}