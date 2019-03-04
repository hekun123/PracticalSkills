package ucas.iie.nsp.hk.sslclient;

import android.content.Context;
import android.content.Intent;
import android.security.KeyChain;

import java.io.IOException;
import java.io.InputStream;

import javax.security.cert.CertificateEncodingException;

/**
 * Created by hk on 2019/3/2.
 */

public class InstallCert {
    private final static String CLIENTCERT = "client.cer";
    /**
     * 安裝证书
     */
    public static void installCert(Context context) {
        InputStream assetsIn = null;
        Intent intent = KeyChain.createInstallIntent();
        try {
            //获取证书流，注意参数为assets目录文件全名
            assetsIn = context.getAssets().open(CLIENTCERT);
            byte[] cert = new byte[10240];
            assetsIn.read(cert);
            javax.security.cert.X509Certificate x509 = null;
            x509 = javax.security.cert.X509Certificate.getInstance(cert);
            //将证书传给系统
            intent.putExtra(KeyChain.EXTRA_CERTIFICATE, x509.getEncoded());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (javax.security.cert.CertificateException e) {
            e.printStackTrace();
        }
        //此处为给证书设置默认别名，第二个参数可自定义，设置后无需用户输入
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("name", "别名（可设置默认）");
        context.startActivity(intent);
    }

}
