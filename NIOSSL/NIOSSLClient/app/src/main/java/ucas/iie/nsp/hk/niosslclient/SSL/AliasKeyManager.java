package ucas.iie.nsp.hk.niosslclient.SSL;

import java.io.FileNotFoundException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

/**
 * Created by hk on 2019/3/4.
 */

public class AliasKeyManager implements X509KeyManager {

    private KeyStore _ks;
    private String _alias;
    private String _password;

    public AliasKeyManager(KeyStore ks, String alias, String password) {
        _ks = ks;
        _alias = alias;
        _password = password;
    }


    public String chooseServerAlias(String str, Principal[] principal, Socket socket) {
        return _alias;
    }

    @Override
    public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
        return null;
    }

    public X509Certificate[] getCertificateChain(String alias) {
        try {
            java.security.cert.Certificate[] certificates = this._ks.getCertificateChain(alias);
            if(certificates == null) {
                throw new FileNotFoundException("no certificate found for alias:" + alias);
            }
            X509Certificate[] x509Certificates = new X509Certificate[certificates.length];
            System.arraycopy(certificates, 0, x509Certificates, 0, certificates.length);
            return x509Certificates;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getClientAliases(String str, Principal[] principal) {
        return new String[]{_alias};
    }

    public PrivateKey getPrivateKey(String alias) {
        try {
            return (PrivateKey) _ks.getKey(alias, _password == null ? null : _password.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getServerAliases(String str, Principal[] principal) {
        return new String[]{_alias};
    }
}