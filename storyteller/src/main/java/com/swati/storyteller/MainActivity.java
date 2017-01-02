package com.swati.storyteller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonElement;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private Button listenButton;
    private TextView resultTextView;
    private AIService aiService;
    private JSONArray jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listenButton = (Button) findViewById(R.id.listenButton);
        checkPermission();

        resultTextView = (TextView) findViewById(R.id.resultTextView);
        final AIConfiguration config = new AIConfiguration("1f6d9832a21e42c38a9e9eb60138cd44",
                AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        TextToSpeechConverter.init(getApplicationContext());


    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
                return;
            }
        }
    }


    public void listenButtonOnClick(final View view) {
        aiService.startListening();
    }

    @Override
    public void onResult(final AIResponse response) {
        Result result = response.getResult();
        final JSONObject[] queryResult = {null};
        //Get Parameters
        //resultTextView.setText(innerResult[0]);
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                //parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                parameterString += entry.getValue();
                TextToSpeechConverter.speak(parameterString);
            }

            final String str = parameterString;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {


                        // looking for
                        String strNoSpaces = str.replace(" ", "+");

                        // Your API key
                        String key = "AIzaSyDNjatEoR9qfT_weQJw-YyFzWUO4wRnvfc";

                        // Your Search Engine ID
                        String cx = "015927602026764487960:mlztqmrtc44";

                        String url2 = "https://www.googleapis.com/customsearch/v1?q=" + strNoSpaces + "&key=" + key + "&cx=" + cx + "&alt=json";
                        Log.d("search", "Url = " + url2);
                        queryResult[0] = httpGet(url2);
                        if (queryResult[0] != null) {
                            JSONObject jsonObject = queryResult[0].getJSONArray("items").getJSONObject(0);
                            String str = jsonObject.get("formattedUrl").toString();
                            queryResult[0] = httpGet("https://"+str);
                            System.out.println(queryResult[0]);
                        }

                    } catch (Exception e) {
                        System.out.println("Error1 " + e.getMessage());
                    }

                }


                private JSONObject httpGet(String urlStr) throws IOException {

                    URL url = new URL(urlStr);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    if (conn.getResponseCode() != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }

                    Log.d("search", "Connection status = " + conn.getResponseMessage());
                    InputStream inputStream = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(inputStream, baos);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(baos.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    conn.disconnect();
                    return jsonObject;
                }
            });

            thread.start();

        }
                resultTextView.setText(parameterString);
                TextToSpeechConverter.speak(parameterString);


        // Show results in TextView.
        /*resultTextView.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString);*/
    }


    @Override
    public void onError(final AIError error) {
        resultTextView.setText(error.toString());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
    }

    @Override
    public void onListeningCanceled() {
    }

    @Override
    public void onListeningFinished() {
    }
}
