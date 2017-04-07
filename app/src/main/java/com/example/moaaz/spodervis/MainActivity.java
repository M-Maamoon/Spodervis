package com.example.moaaz.spodervis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {


    PNConfiguration pnConfiguration;
    PubNub pubnub;
 //   com.example.moaaz.spodervis.nlp nlp  = new nlp(this);
    boolean lightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePubNub();


    }

    public void executeCommand(String response)
    {
      //  String response = nlp.value;
        if (response.equals("light on")) {
            TextView t = (TextView) findViewById(R.id.text);
            t.setText("Switching light on!");
            switchLight("light_on");
        }
        if (response.equals("light off")) {
            TextView t = (TextView) findViewById(R.id.text);
            t.setText("Switching light off!");
            switchLight("light_off");
        }
        if (response.equals("start music")) {
            TextView t = (TextView) findViewById(R.id.text);
            t.setText("Switching light off!");
            switchLight("music_on");
        }
        if (response.equals("stop music")) {
            TextView t = (TextView) findViewById(R.id.text);
            t.setText("Switching light off!");
            switchLight("music_off");
        }
        if (response.equals("null"))
        {
            TextView t = (TextView) findViewById(R.id.text);
            t.setText("I cannot catch that!");
        }


    }

    public void sendCommand(View view) throws Exception
    {
        EditText commandField = (EditText) findViewById(R.id.commandField);
        String command =  commandField.getText().toString();
        nlp n = new nlp(this);
        n.execute(command).get();
        executeCommand(n.value);
    }

    public void switchLight(String option)
    {
        pubnub.publish()
                .message(Arrays.asList(option))
                .channel("commands")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                    }
                });
    }
/*
    public void switchLight(View view)
    {
        if (!lightOn)
        {
            Button ls = (Button)  findViewById(R.id.switchLightButton);
            ls.setText("Switch Off");
            lightOn = true;
            pubnub.publish()
                    .message(Arrays.asList("on"))
                    .channel("commands")
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError to see if error happened
                        }
                    });
            return;
        }
        lightOn = false;
        Button ls = (Button)  findViewById(R.id.switchLightButton);
        ls.setText("Switch On");
        pubnub.publish()
                .message(Arrays.asList("off"))
                .channel("commands")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                    }
                });
    }
*/
    public void configurePubNub()
    {
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-6e10bdfe-1ad0-11e7-aca9-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-bcba6aa9-1ae4-4658-bfbd-ab52df9adb44");
        pubnub = new PubNub(pnConfiguration);

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getOperation() != null) {
                    switch (status.getOperation()) {
                        // let's combine unsubscribe and subscribe handling for ease of use
                        case PNSubscribeOperation:
                        case PNUnsubscribeOperation:
                            // note: subscribe statuses never have traditional
                            // errors, they just have categories to represent the
                            // different issues or successes that occur as part of subscribe
                            switch (status.getCategory()) {
                                case PNConnectedCategory:
                                    // this is expected for a subscribe, this means there is no error or issue whatsoever
                                case PNReconnectedCategory:
                                    // this usually occurs if subscribe temporarily fails but reconnects. This means
                                    // there was an error but there is no longer any issue
                                case PNDisconnectedCategory:
                                    // this is the expected category for an unsubscribe. This means there
                                    // was no error in unsubscribing from everything
                                case PNUnexpectedDisconnectCategory:
                                    // this is usually an issue with the internet connection, this is an error, handle appropriately
                                case PNAccessDeniedCategory:
                                    // this means that PAM does allow this client to subscribe to this
                                    // channel and channel group configuration. This is another explicit error
                                default:
                                    // More errors can be directly specified by creating explicit cases for other
                                    // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                            }

                        case PNHeartbeatOperation:
                            // heartbeat operations can in fact have errors, so it is important to check first for an error.
                            // For more information on how to configure heartbeat notifications through the status
                            // PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
                            if (status.isError()) {
                                // There was an error with the heartbeat operation, handle here
                            } else {
                                // heartbeat operation was successful
                            }
                        default: {
                            // Encountered unknown status type
                        }
                    }
                } else {
                    // After a reconnection see status.getCategory()
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
    }
}
