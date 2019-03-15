package ucas.iie.nsp.hk.niosslserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ThemedSpinnerAdapter;

import ucas.iie.nsp.hk.niosslserver.Server.NIOSSLClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnStart = findViewById(R.id.btnStartService);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 0 ; i < 10 ; i ++){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NIOSSLClient client = new NIOSSLClient();
                            client.Connect();
                        }
                    }).start();
                }
            }
        });
    }
}
