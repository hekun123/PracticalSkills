package ucas.iie.nsp.hk.niosslserver.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.net.ssl.SSLContext;

import ucas.iie.nsp.hk.niosslserver.SSL.SSLHelper;

/**
 * Created by hk on 2019/3/15.
 */

public class NIOSSLClient {
    private ByteBuffer NetBuffer;
    public NIOSSLClient(){
        NetBuffer = ByteBuffer.allocate(1024*32);
    }

    public void Connect(){
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 12345);
        Selector selector = null;
        try {
            selector = Selector.open();
            SocketChannel channel = SocketChannel.open();
            channel.connect(address);
            channel.configureBlocking(false);
            int ops = SelectionKey.OP_CONNECT | SelectionKey.OP_READ;

            SelectionKey key =  channel.register(selector, ops);

            SSLHelper ssl = new SSLHelper(SSLContext.getDefault(), channel, true);

            while (true)
            {
                key.selector().select();
                Iterator<SelectionKey> keys = key.selector().selectedKeys().iterator();
                while (keys.hasNext())
                {
                    SelectionKey Event = keys.next();
                    keys.remove();
                    if(!Event.isValid()) {
                        continue;
                    }
                    if(Event.isConnectable()){
                        continue;
                    }else if(Event.isReadable()){
                        NetBuffer.clear();
                        ((ReadableByteChannel) key.channel()).read(NetBuffer);
                        ssl.HandleIncomingBuffer(NetBuffer);
                    }

                }
            }
        } catch (IOException e) {
        e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
