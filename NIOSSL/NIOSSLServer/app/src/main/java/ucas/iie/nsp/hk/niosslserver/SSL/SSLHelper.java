package ucas.iie.nsp.hk.niosslserver.SSL;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 * Created by hk on 2019/3/15.
 */

public class SSLHelper {
    private static String TAG = "SSLHelper";

    private SSLEngine engine;
    private NioSSLProvider ssl;
    private SSLContext sslContext;

    public void CreateContext(){
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null
                    ,  new TrustManager[]{new EasyX509TrustManager(null)}
                    , new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
    //将key直接换成channel
    public SSLHelper(SSLContext sslContext, SocketChannel socketChannel, boolean isClient){
        // create the worker threads
        final Executor ioWorker = Executors.newSingleThreadExecutor();
        final Executor taskWorkers = Executors.newFixedThreadPool(2);

        this.sslContext = sslContext;
        CreateContext();

        // create the SSLEngine
        try {
            engine = this.sslContext.createSSLEngine();
            engine.setUseClientMode(isClient);
            engine.beginHandshake();
        } catch (SSLException e) {
            e.printStackTrace();
        }

        final int ioBufferSize = 32 * 1024;
        ssl = new NioSSLProvider(socketChannel, engine, ioBufferSize, ioWorker, taskWorkers)
        {
            @Override
            public void onFailure(Exception ex)
            {
                Log.d(TAG, "handshake failure");
                ex.printStackTrace();
            }

            @Override
            public void onSuccess()
            {
                System.out.println("handshake success");
                SSLSession session = engine.getSession();
                try
                {
                    Log.d(TAG, "local principal: " + session.getLocalPrincipal());
                    Log.d(TAG, "remote principal: " + session.getPeerPrincipal());
                    Log.d(TAG, "cipher: " + session.getCipherSuite());
                }
                catch (Exception exc)
                {
                    exc.printStackTrace();
                }

                StringBuilder str = new StringBuilder();
                str.append("hello i am client");
                ByteBuffer client = ByteBuffer.wrap(str.toString().getBytes());
                this.sendAsync(client);

            }
            @Override
            public void onInput(ByteBuffer decrypted)
            {
                // HTTP response
                byte[] dst = new byte[decrypted.remaining()];
                decrypted.get(dst);
                String response = new String(dst);
                Log.d(TAG, "onInput: " + response);
            }

            @Override
            public void onClosed()
            {
                Log.d(TAG, "ssl session closed");
            }
        };
    }

    public void HandleIncomingBuffer(ByteBuffer ComingBuffer){
        this.ssl.processInput(ComingBuffer);
    }
}
