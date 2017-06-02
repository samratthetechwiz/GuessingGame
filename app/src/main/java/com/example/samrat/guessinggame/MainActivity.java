package com.example.samrat.guessinggame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();
    int randomCeleb = 0;
    ImageView celebImage;
    int locationOfCorrectAnswer;
    int incorrectAnswer;
    String[] options = new String[4];
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    Button playAgain;
    Button startButton;
    int score = 0;
    int numberOfQuestions = 0;
    TextView scoreTextView;
    TextView timerTextView;
    TextView resultTextView;
    GridLayout gridLayout;

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{


        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(inputStream);

                return image;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  null;
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while( data != -1){
                    char current = (char) data;
                    result = result + current;
                    data = inputStreamReader.read();
                }

                return  result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void playAgain(View view) throws ExecutionException, InterruptedException {
        playAgain.setVisibility(View.INVISIBLE);
        generateQuestions();
        resultTextView.setVisibility(View.INVISIBLE);
        button0.setVisibility(View.VISIBLE);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        button3.setVisibility(View.VISIBLE);

        new CountDownTimer(30100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.valueOf(millisUntilFinished/1000)+'s');
            }

            @Override
            public void onFinish() {
                resultTextView.setVisibility(View.VISIBLE);
                resultTextView.setText("Your Score is " + Integer.toString(score)+"/"+Integer.toString(numberOfQuestions));
                timerTextView.setText("30s");
                scoreTextView.setText("0/0");
                playAgain.setVisibility(View.VISIBLE);
                button0.setVisibility(View.INVISIBLE);
                button1.setVisibility(View.INVISIBLE);
                button2.setVisibility(View.INVISIBLE);
                button3.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    public void chosenAnswer(View view) throws ExecutionException, InterruptedException {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            score++;
            Toast.makeText(this,"Correct!!!",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"Incorrect!!!",Toast.LENGTH_SHORT).show();
        }
        numberOfQuestions++;
        scoreTextView.setText(Integer.toString(score)+"/"+Integer.toString(numberOfQuestions));
        generateQuestions();
    }

    public void generateQuestions() throws ExecutionException, InterruptedException {
        Random rand = new Random();
        randomCeleb = rand.nextInt(celebURLs.size());

        ImageDownloader imageDownloader = new ImageDownloader();
        Bitmap bitmap;

        bitmap = imageDownloader.execute(celebURLs.get(randomCeleb)).get();
        celebImage.setImageBitmap(bitmap);

        locationOfCorrectAnswer = rand.nextInt(4);

        for(int i=0;i<4;i++){
            if(i==locationOfCorrectAnswer) {
                options[i] = celebNames.get(randomCeleb);
            }else {
                incorrectAnswer = rand.nextInt(celebNames.size());
                while(incorrectAnswer == randomCeleb){
                    incorrectAnswer = rand.nextInt(celebNames.size());
                }
                options[i] = celebNames.get(incorrectAnswer);
            }
        }

        button0.setText(options[0]);
        button1.setText(options[1]);
        button2.setText(options[2]);
        button3.setText(options[3]);

    }

    public void start(View view) throws ExecutionException, InterruptedException {
        startButton.setVisibility(View.INVISIBLE);
        gridLayout.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.VISIBLE);
        playAgain.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.VISIBLE);
        playAgain(findViewById(R.id.playAgain));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebImage = (ImageView)findViewById(R.id.celebImage);
        button0 = (Button)findViewById(R.id.button0);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        playAgain = (Button)findViewById(R.id.playAgain);
        startButton = (Button)findViewById(R.id.startButton);
        scoreTextView = (TextView)findViewById(R.id.scoreTextView);
        timerTextView = (TextView)findViewById(R.id.timerTextView);
        resultTextView = (TextView)findViewById(R.id.resultTextView);
        gridLayout = (GridLayout)findViewById(R.id.gridLayout);

        DownloadTask task = new DownloadTask();
        String result = "";

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            String splitResult[] = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("img src=\"(.*?)\" alt");
            Matcher m = p.matcher(splitResult[0]);

            while(m.find()){
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("\" alt=\"(.*?)\"/>");
            m = p.matcher(splitResult[0]);

            while(m.find()){
                celebNames.add(m.group(1));
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
