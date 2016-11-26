package kazakov.com.termnwox;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class PlayerActivity extends AppCompatActivity implements SensorEventListener{

     AudioTrack audioTrack;
    double lastX;
    double lastY;

    private long lastTime=0;
    long curTime;

    double volumeLevel=50;


    Button btnPlay;

    AudioManager audioManager;



    SensorManager sensorManager;

    final float[] mValuesMagnet = new float[3];
    final float[] mValuesAccel = new float[3];
    final float[] mValuesOrientation = new float[3];
    final float[] mRotationMatrix = new float[9];

    private final int duration = 300; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private  double freqOfTone = 1000; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];
    TextView tvRate, tvVolume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);


        btnPlay =  (Button) findViewById(R.id.btn_play);
        tvVolume = (TextView) findViewById(R.id.tv_volume);
        tvRate = (TextView)findViewById(R.id.tv_rate);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                playSound();

            }
        });


        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);


    }





    /**
     * increase volume level
     */
    private void increaseVolume(){
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, 0);
    }

    private void decreaseVolume(){
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER, 0);
    }


    @Override
    protected void onStop() {
        super.onStop();
        audioTrack.stop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mValuesAccel, 0, 3);

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
                break;
        }
        SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet);
        SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);

        lastX = Math.toDegrees((mValuesOrientation[0]));
        lastY = Math.toDegrees((mValuesOrientation[1]));

        curTime = System.currentTimeMillis();

//playSound();

        if ((curTime - lastTime) > 1000) {
            lastTime = curTime;


            if (audioTrack!=null){

                if (lastX>0){
                    freqOfTone = Math.abs(lastX) * 100;
                }else if (lastX<0){
                    freqOfTone = Math.abs(lastX) * 50;
                }


                audioTrack.setPlaybackRate((int)freqOfTone);
            }



            if (lastY>0){
                volumeLevel  = volumeLevel+5;
                if (volumeLevel>100){
                    volumeLevel = 100;
                }

            }else if (lastY<0){
                volumeLevel  = volumeLevel-5;
                if (volumeLevel<0){
                    volumeLevel = 0;
                }
            }
            if (audioTrack!=null) {
                audioTrack.setVolume((float) volumeLevel);
            }
            tvRate.setText(freqOfTone+"");
            tvVolume.setText(volumeLevel+"");
        }




    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    void genTone(){

        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        int idx = 0;
        for (final double dVal : sample) {

            final short val = (short) ((dVal * 32767));

            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
    }



    void playSound(){
        genTone();
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }

}
