package ucas.iie.nsp.hk.niosslclient.Server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import ucas.iie.nsp.hk.niosslclient.SSL.SSLHelper;

/**
 * Created by hk on 2019/3/15.
 */

public class SSLTunnel {
    public SocketChannel socketChannel;
    private SSLEngine engine;
    private SSLHelper helper;

    public SSLTunnel(SSLContext sslContext, SocketChannel channel, SSLEngine engine){
        this.socketChannel = channel;
        this.engine = engine;
        helper = new SSLHelper(sslContext, channel, false);
    }

    public void read(ByteBuffer NetBuffer){
        helper.HandleIncomingBuffer(NetBuffer);
    }

}
