package ucas.iie.nsp.hk.sslclient;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLClient {

    private static final String SSL_TYPE = "SSL";
    private static String CLIENT_ALIAS = "client";
    private static String CLIENT_KEY_STORE = "kclient.p12";
    private static String CLIENT_KEY_PSW = "123456";
    private static String CLIENT_TRUST_STORE = "tclient.bks";
    private static String CLIENT_TRUST_PSW = "123456";

    private SSLSocket sslSocket;

    public SSLClient(Context appContext, String targetHost, int port) throws Exception {
        SSLContext sslContext = createSSLContext(appContext);
        SSLSocketFactory sslcntFactory = (SSLSocketFactory) sslContext.getSocketFactory();
        sslSocket = (SSLSocket) sslcntFactory.createSocket(targetHost, port);
        String[] supported = sslSocket.getSupportedCipherSuites();
        sslSocket.setEnabledCipherSuites(supported);
    }

    private SSLContext createSSLContext(Context appContext) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(SSL_TYPE);
        sslContext.init(null//createKeyManagers(appContext, CLIENT_KEY_STORE, CLIENT_KEY_PSW, CLIENT_ALIAS)
                        , new TrustManager[]{new EasyX509TrustManager(null)}//null//createTrustManagers(appContext, CLIENT_TRUST_STORE, CLIENT_KEY_PSW)
                        , new SecureRandom());
        return sslContext;
    }

    public String sayToSvr(String sayMsg) throws IOException {

        BufferedReader ioReader = new BufferedReader(new InputStreamReader(
                sslSocket.getInputStream()));
        PrintWriter ioWriter = new PrintWriter(sslSocket.getOutputStream());
        ioWriter.println(sayMsg);
        ioWriter.flush();
        return ioReader.readLine();
    }

    /**
     * Creates the key managers required to initiate the {@link SSLContext}, using a JKS keystore as an input.
     *
     * @param appContext - Application context
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @param alias - the key's alias
     * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
     * @throws Exception
     */
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

    /**
     * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore as an input.
     *
     * @param appContext - the app context
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
     * @throws Exception
     */
    protected TrustManager[] createTrustManagers(Context appContext, String filepath, String keystorePassword) throws Exception {
        final String KEY_STORE_TYPE_BKS = "bks";
        InputStream inputStream = appContext.getResources().getAssets().open(filepath);
        KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
        trustStore.load(inputStream, keystorePassword.toCharArray());

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();

    }
}
