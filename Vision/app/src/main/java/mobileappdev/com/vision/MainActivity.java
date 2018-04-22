package mobileappdev.com.vision;

import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mobileappdev.com.vision.output.Greeting;

public class MainActivity extends AppCompatActivity {

    private ScheduledThreadPoolExecutor exec;
    TextToSpeech assistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        (imageView).setBackgroundResource(R.drawable.progress_animation);
        AnimationDrawable progressAnimation = (AnimationDrawable) imageView.getBackground();
        progressAnimation.start();

    }

    public void startWalking(View view) {
        exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                // code to execute repeatedly
                new HttpRequestTask().execute();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stopWalking(View view) {
        exec.shutdown();
    }

    private class HttpRequestTask extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                //http://172.21.66.77:8999
                final String url = "http://rest-service.guides.spring.io/greeting";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Greeting greeting = restTemplate.getForObject(url, Greeting.class);
                Toast.makeText(MainActivity.this, "Sending response", Toast.LENGTH_SHORT).show();
                return greeting;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Greeting greeting) {
            TextView greetingIdText = (TextView) findViewById(R.id.id_value);
            TextView greetingContentText = (TextView) findViewById(R.id.content_value);
            greetingIdText.setText(greeting.getId());
            greetingContentText.setText(greeting.getContent());
            Toast.makeText(MainActivity.this, "Response Received", Toast.LENGTH_SHORT).show();
            assistant = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {

                if(Integer.parseInt(greeting.getId()) <= 10) {
                    assistant.speak("Your obstacle is " + greeting.getId() + " meters away!", TextToSpeech.QUEUE_FLUSH, null);
                }
                }
            });
        }

    }
}
