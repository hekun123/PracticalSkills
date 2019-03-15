package ucas.iie.nsp.hk.niosslclient.Server;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import ucas.iie.nsp.hk.niosslclient.SSL.AliasKeyManager;

/**
 * Created by hk on 2019/3/15.
 */

public class NIOSSLClient {
    private static String SERVER_KEY_ALIAS = "server";
    private static String SERVER_KEY_STORE = "kserver.p12";
    private static String SERVER_KEY_STORE_PWD = "123456";
    private static String SERVER_TRUST_STORE = "tserver.bks";
    private static String SERVER_TRUST_STORE_PWD = "123456";

    private ByteBuffer NetBuffer;
    public static int MAX_MUTE = 2560;

    private Selector selector;
    private SSLContext sslContext;

    private boolean isActive;

    protected KeyManager[] createKeyManagers(Context appContext, String filepath, String keystorePassword, String alias) throws Exception {
        final String KEY_STORE_TYPE_P12 = "PKCS12";
        InputStream inputStream = appContext.getResources().getAssets().open(filepath);
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);
        keyStore.load(inputStream, keystorePassword.toCharArray());

        KeyManager[] managers;
        if(alias != null) {
            managers = new KeyManager[]{new AliasKeyManager(keyStore, alias, keystorePassword)};
        } else {
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword == null ? null : keystorePassword.toCharArray());
            managers = keyManagerFactory.getKeyManagers();
        }
        return managers;
    }

    public NIOSSLClient(Context appContext, String hostAddress, int port){
        isActive = false;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(createKeyManagers(appContext, SERVER_KEY_STORE, SERVER_KEY_STORE_PWD, SERVER_KEY_ALIAS)
                    , null
                    , new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetBuffer = ByteBuffer.allocate(MAX_MUTE);

        try {
            selector = SelectorProvider.provider().openSelector();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.socket().bind(new InetSocketAddress(hostAddress, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isActive = true;
    }

    public void StartWork() throws Exception {

        while (isActive) {
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read((SSLTunnel) key.attachment());
                }
            }
        }
    }
    private void accept(SelectionKey key) throws Exception {

        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        //engine.setNeedClientAuth(false);
        engine.beginHandshake();
        String[] ciphers = engine.getEnabledCipherSuites();
        for(String cipher : ciphers){
            Log.d("enable ciphers : " , cipher);
        }
        SSLTunnel localTunnel = new SSLTunnel(sslContext, socketChannel, engine);
        socketChannel.register(selector, SelectionKey.OP_READ, localTunnel);
    }

    private void read(SSLTunnel sslTunnel){
        NetBuffer.clear();
        try {
            sslTunnel.socketChannel.read(NetBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sslTunnel.read(NetBuffer);
    }
}
