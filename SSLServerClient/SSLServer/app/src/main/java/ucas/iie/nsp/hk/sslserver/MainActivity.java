package ucas.iie.nsp.hk.sslserver;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

public class MainActivity extends AppCompatActivity {
    private Button btnStartServer = null;
    private Button btnEndServer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doSomeThing();
//        handle();
    }

    private void handle(){
        final TextView txt = findViewById(R.id.txtShowDialog);

        final Handler handle = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                txt.append(msg.obj.toString());
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



        String[] protocols = sslContext.getSupportedSSLParameters().getProtocols();
        for (String protocol : protocols) {
            Message msg = new Message();
            msg.obj = "Context supported protocol: " + protocol + "\n";
            handle.sendMessage(msg);
        }

        SSLEngine engine = sslContext.createSSLEngine();
        String[] supportedProtocols = engine.getSupportedProtocols();
        for (String protocol : supportedProtocols) {
            Message msg = new Message();
            msg.obj = "Engine supported protocol: " + protocol + "\n";
            handle.sendMessage(msg);
        }
    }

    private void doSomeThing(){
        InstallCert.installCert(getApplicationContext());

        btnStartServer = findViewById(R.id.btnStartService);
        btnEndServer = findViewById(R.id.btnEndService);
        btnStartServer.setEnabled(true);
        btnEndServer.setEnabled(false);

        final NioSslServer[] server = new NioSslServer[1];

        btnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            server[0] = new NioSslServer(getApplicationContext(),"SSL", "127.0.0.1", 12345);
                            server[0].start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                btnEndServer.setEnabled(true);
                btnStartServer.setEnabled(false);
            }
        });

        btnEndServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    server[0].finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                btnStartServer.setEnabled(true);
                btnEndServer.setEnabled(false);
            }
        });
    }
}
