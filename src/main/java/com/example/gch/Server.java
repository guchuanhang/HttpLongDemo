package com.example.gch;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
    public static void main(String[] args){
        Server server = new Server(8080);
    }
    private ServerSocketChannel serverSocketChannel;


    public Server(int serverPort) {
        bind(serverPort);
    }

    private void bind(final int serverPort) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                //服务端要建立两个group，一个负责接收客户端的连接，一个负责处理数据传输
                //连接处理group
                EventLoopGroup boss = new NioEventLoopGroup();
                //事件处理group
                EventLoopGroup worker = new NioEventLoopGroup();
                ServerBootstrap bootstrap = new ServerBootstrap();
                // 绑定处理group
                bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                        //保持连接数
                        .option(ChannelOption.SO_BACKLOG, 128)
                        //保持连接
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        //处理新连接
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                // 增加任务处理
                                ChannelPipeline p = socketChannel.pipeline();
//                                p.addLast(new SimpleServerHandler());
                                p.addLast(new ServerHandler());
                            }
                        })
                ;

                //绑定端口，同步等待成功
                ChannelFuture future;
                try {
                    future = bootstrap.bind(serverPort).sync();
                    if (future.isSuccess()) {
                        serverSocketChannel = (ServerSocketChannel) future.channel();
                        System.out.println("服务端开启成功");
                    } else {
                        System.out.println("服务端开启失败");
                    }

                    //等待服务监听端口关闭,就是由于这里会将线程阻塞，导致无法发送信息，所以我这里开了线程
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //优雅地退出，释放线程池资源
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                }
            }
        });
        thread.start();
    }

    public void sendMessage(Object msg) {
        if (serverSocketChannel != null) {
            serverSocketChannel.writeAndFlush(msg);
        }
    }
}