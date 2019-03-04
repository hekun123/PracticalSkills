package ucas.iie.nsp.hk.sslserver;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.SecureRandom;
import java.util.Iterator;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * An SSL/TLS server, that will listen to a specific address and port and serve SSL/TLS connections
 * compatible with the protocol it applies.
 * <p/>
 * After initialization {@link NioSslServer#start()} should be called so the server starts to listen to
 * new connection requests. At this point, start is blocking, so, in order to be able to gracefully stop
 * the server, a {@link Runnable} containing a server object should be created. This runnable should 
 * start the server in its run method and also provide a stop method, which will call {@link NioSslServer#stop()}.
 * </p>
 * NioSslServer makes use of Java NIO, and specifically listens to new connection requests with a {@link ServerSocketChannel}, which will
 * create new {@link SocketChannel}s and a {@link Selector} which serves all the connections in one thread.
 *
 * @author <a href="mailto:alex.a.karnezis@gmail.com">Alex Karnezis</a>
 */
public class NioSslServer extends NioSslPeer {
    private static String TAG = "NioSslServer";

    private static String SERVER_KEY_ALIAS = "server";
    private static String SERVER_KEY_STORE = "kserver.p12";
    private static String SERVER_KEY_STORE_PWD = "123456";
    private static String SERVER_TRUST_STORE = "tserver.bks";
    private static String SERVER_TRUST_STORE_PWD = "123456";
	/**
	 * Declares if the server is active to serve and create new connections.
	 */
	private boolean active;
	
    /**
     * The context will be initialized with a specific SSL/TLS protocol and will then be used
     * to create {@link SSLEngine} classes for each new connection that arrives to the server.
     */
    private SSLContext context;

    /**
     * A part of Java NIO that will be used to serve all connections to the server in one thread.
     */
    private Selector selector;


    /**
     * Server is designed to apply an SSL/TLS protocol and listen to an IP address and port.
     *
     * @param appContext - Application context
     * @param protocol - the SSL/TLS protocol that this server will be configured to apply.
     * @param hostAddress - the IP address this server will listen to.
     * @param port - the port this server will listen to.
     * @throws Exception
     */
    public NioSslServer(Context appContext, String protocol, String hostAddress, int port) throws Exception {

        context = SSLContext.getInstance(protocol);
        Log.d(TAG, SERVER_KEY_STORE);
        context.init(createKeyManagers(appContext, SERVER_KEY_STORE, SERVER_KEY_STORE_PWD, SERVER_KEY_ALIAS)
                , null//createTrustManagers(appContext, SERVER_TRUST_STORE, SERVER_TRUST_STORE_PWD)
                , new SecureRandom());

        SSLSession dummySession = context.createSSLEngine().getSession();
        myAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        myNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
        peerAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        peerNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
        dummySession.invalidate();

        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(hostAddress, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        active = true;
        
    }

    /**
     * Should be called in order the server to start listening to new connections.
     * This method will run in a loop as long as the server is active. In order to stop the server
     * you should use {@link NioSslServer#stop()} which will set it to inactive state
     * and also wake up the listener, which may be in blocking select() state.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        Log.d(TAG, "Initialized and waiting for new connections...");
        while (isActive()) {
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
                    read((SocketChannel) key.channel(), (SSLEngine) key.attachment());
                }
            }
        }

        Log.d(TAG, "Goodbye!");
        
    }
    
    /**
     * Sets the server to an inactive state, in order to exit the reading loop in {@link NioSslServer#start()}
     * and also wakes up the selector, which may be in select() blocking state.
     */
    public void stop() {
        Log.d(TAG, "Will now close server...");
    	active = false;
    	executor.shutdown();
    	selector.wakeup();
    }

    /**
     * Will be called after a new connection request arrives to the server. Creates the {@link SocketChannel} that will
     * be used as the network layer link, and the {@link SSLEngine} that will encrypt and decrypt all the data
     * that will be exchanged during the session with this specific client.
     *
     * @param key - the key dedicated to the {@link ServerSocketChannel} used by the server to listen to new connection requests.
     * @throws Exception
     */
    private void accept(SelectionKey key) throws Exception {

        Log.d(TAG, "New connection request!");

        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);
        Log.d(TAG, "this is what i get : " + socketChannel.socket().getLocalPort());

        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);
        //engine.setNeedClientAuth(false);
        engine.beginHandshake();
        String[] ciphers = engine.getEnabledCipherSuites();
        for(String cipher : ciphers){
            Log.d("enable ciphers : " , cipher);
        }

        if (doHandshake(socketChannel, engine)) {
            socketChannel.register(selector, SelectionKey.OP_READ, engine);
        } else {
            socketChannel.close();
            Log.d(TAG, "Connection closed due to handshake failure.");
        }
    }

    /**
     * Will be called by the selector when the specific socket channel has data to be read.
     * As soon as the server reads these data, it will call {@link NioSslServer#write(SocketChannel, SSLEngine, String)}
     * to send back a trivial response.
     *
     * @param socketChannel - the transport link used between the two peers.
     * @param engine - the engine used for encryption/decryption of the data exchanged between the two peers.
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    @Override
    protected void read(SocketChannel socketChannel, SSLEngine engine) throws IOException {

        Log.d(TAG, "About to read from a client...");

        peerNetData.clear();
        int bytesRead = socketChannel.read(peerNetData);
        if (bytesRead > 0) {
            peerNetData.flip();
            while (peerNetData.hasRemaining()) {
                peerAppData.clear();
                SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
                switch (result.getStatus()) {
                case OK:
                    peerAppData.flip();
                    Log.d(TAG, "Incoming message: " + new String(peerAppData.array()));
                    break;
                case BUFFER_OVERFLOW:
                    peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                    break;
                case BUFFER_UNDERFLOW:
                    peerNetData = handleBufferUnderflow(engine, peerNetData);
                    break;
                case CLOSED:
                    Log.d(TAG, "Client wants to close connection...");
                    closeConnection(socketChannel, engine);
                    Log.d(TAG, "Goodbye client!");
                    return;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }

            write(socketChannel, engine, "Hello! I am your server!\n");

        } else if (bytesRead < 0) {
            Log.e(TAG, "Received end of stream. Will try to close connection with client...");
            handleEndOfStream(socketChannel, engine);
            Log.d(TAG, "Goodbye client!");
        }
    }

    /**
     * Will send a message back to a client.
     *
     * @param socketChannel - the key dedicated to the socket channel that will be used to write to the client.
     * @param message - the message to be sent.
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    @Override
    protected void write(SocketChannel socketChannel, SSLEngine engine, String message) throws IOException {

        Log.d(TAG, "About to write to a client...");

        myAppData.clear();
        myAppData.put(message.getBytes());
        myAppData.flip();
        while (myAppData.hasRemaining()) {
            // The loop has a meaning for (outgoing) messages larger than 16KB.
            // Every wrap call will remove 16KB from the original message and send it to the remote peer.
            myNetData.clear();
            SSLEngineResult result = engine.wrap(myAppData, myNetData);
            switch (result.getStatus()) {
            case OK:
                myNetData.flip();
                while (myNetData.hasRemaining()) {
                    socketChannel.write(myNetData);
                }
                Log.d(TAG, "Message sent to the client: " + message);
                break;
            case BUFFER_OVERFLOW:
                myNetData = enlargePacketBuffer(engine, myNetData);
                break;
            case BUFFER_UNDERFLOW:
                throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
            case CLOSED:
                closeConnection(socketChannel, engine);
                return;
            default:
                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }
    }

    /**
     * Determines if the the server is active or not.
     *
     * @return if the server is active or not.
     */
    private boolean isActive() {
        return active;
    }

    @Override
    protected void finalize() throws Throwable {

        super.finalize();
    }
}
