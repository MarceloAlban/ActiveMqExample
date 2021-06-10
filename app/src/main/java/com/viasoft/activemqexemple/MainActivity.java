package com.viasoft.activemqexemple;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    MqttAndroidClient client;
    TextView textView;
    private String serverURI = "tcp://192.168.2.104:1883";
    private String clientId = "client_android_tests_german";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        connect();
    }

    private String getSecureId() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void connect() {
        clientId = "_client_android_" + getSecureId();

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setCleanSession(false);//mantera a conexao e as mensagens no server, assim que o client reconectar será entregue
        connectOptions.setKeepAliveInterval(10);//Este é o intervalo de tempo em que o cliente precisa efetuar ping no broker para manter a conexão ativa.

        client = new MqttAndroidClient(this, serverURI, clientId);
        try {
            client.connect(connectOptions);

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    subscribe();
                }

                @Override
                public void connectionLost(Throwable cause) {
                    textView.setText(cause != null ? cause.getMessage() : "Null");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    textView.setText(message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        String subscribeTopic = "topico_testes";
        try {
            /*
             * QOS
             * Valor 0: não confiável, a mensagem é entregue no máximo uma única vez, se o cliente estiver indisponível no momento, ele perderá a mensagem.
             * Valor 1: a mensagem deve ser entregue pelo menos uma vez.
             * Valor 2: a mensagem deve ser entregue exatamente uma vez.
             * */
            client.subscribe(subscribeTopic, 2, new IMqttMessageListener() {
                @Override
                public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(message.toString());
                        }
                    });
                }
            });

            textView.setText("Conectado");
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /*private void connect() {
        clientId = "_client_android_" + getSecureId();

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        //connectOptions.setCleanSession(true);

        client = new MqttAndroidClient(this, serverURI, clientId);
        try {
            client.connect(connectOptions, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        String subscribeTopic = "topico_testes";
        try {

            client.subscribe(subscribeTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, message.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Toast.makeText(MainActivity.this, message.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                }
            });

            client.getPendingDeliveryTokens();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
*/
    @Override
    protected void onDestroy() {
        if (client.isConnected()) {
            try {
                client.unregisterResources();
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    public void publish(View view) {
        String publishTopic = "topico_testes";
        String message = ((EditText) findViewById(R.id.txtEditor)).getText().toString();
        try {
            client.publish(publishTopic, message.getBytes(), 2, ((CheckBox) findViewById(R.id.reter)).isChecked());
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
