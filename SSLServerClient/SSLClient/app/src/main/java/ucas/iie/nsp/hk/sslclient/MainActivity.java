package ucas.iie.nsp.hk.sslclient;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doSomeThing();
        handle();
    }

    private void handle(){
        final TextView txt = findViewById(R.id.txtShowDialog);
        txt.setMovementMethod(ScrollingMovementMethod.getInstance());

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
        String[] ciphers = engine.getEnabledCipherSuites();
        for(String cipher : ciphers){
            Message msg = new Message();
            msg.obj = "Engine supported cipher cuite: " + cipher + "\n";
            handle.sendMessage(msg);
        }
        String[] supportedProtocols = engine.getEnabledCipherSuites();
        for (String protocol : supportedProtocols) {
            Message msg = new Message();
            msg.obj = "Engine supported protocol: " + protocol + "\n";
            handle.sendMessage(msg);
        }
    }

    private void doSomeThing(){
        //InstallCert.installCert(getApplicationContext());
        final Button btnStartServer = findViewById(R.id.btnStartService);
        final Button btnEndServer = findViewById(R.id.btnEndService);
        btnStartServer.setEnabled(true);
        btnEndServer.setEnabled(false);

        btnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SSLClient sslSocket = null;
                        try {
                            sslSocket = new SSLClient(getApplicationContext(), "127.0.0.1", 12345);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        BufferedReader ioReader = new BufferedReader(new InputStreamReader(System.in));
                        String sayMsg = "";
                        String svrRespMsg = "";
                        try {
                            sayMsg = "hello this is test";
                            svrRespMsg = sslSocket.sayToSvr(sayMsg);
                            if (svrRespMsg != null && !svrRespMsg.trim().equals("")) {
                                Log.d(TAG, "服务器通过SSL协议响应:" + svrRespMsg);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                btnStartServer.setEnabled(false);
                btnEndServer.setEnabled(true);
            }
        });
    }
}
