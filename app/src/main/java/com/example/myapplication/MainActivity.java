package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private File audioFile;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private int playbackSpeed = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MediaPlayer and MediaRecorder objects
        mediaPlayer = new MediaPlayer();
        mediaRecorder = new MediaRecorder();

        // Set up MediaPlayer listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);

        // Set up MediaRecorder
        setUpMediaRecorder();

        // Set up SeekBar
        SeekBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause(); // Pause playback when seeking starts
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start(); // Resume playback when seeking ends
                }
            }
        });

        // Set up OnClickListeners for buttons
        Button recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecordButtonClick(v);
            }
        });

        Button playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayButtonClick();
            }
        });

        Button pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseButtonClick(v);
            }
        });

        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopButtonClick(v);
            }
        });

        Button speedButton = findViewById(R.id.speed_button);
        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress;
                onSpeedButtonClick(v);
            }
        });
    }

    private void setUpMediaRecorder() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the error, log it, or display a message to the user
        }
    }

    private void onRecordButtonClick(View v) {
        if (!isRecording) {
            // Start recording
            startRecording();
        } else {
            // Stop recording
            stopRecording();
        }
    }

    private void startRecording() {
        try {
            // Reset MediaRecorder
            mediaRecorder.reset();

            // Create a temporary file to store the recorded audio
            audioFile = File.createTempFile("audio", ".3gp", getExternalCacheDir());

            // Configure MediaRecorder
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

            // Prepare MediaRecorder
            mediaRecorder.prepare();

            // Start recording
            mediaRecorder.start();

            // Update recording state
            isRecording = true;
            // Disable other buttons while recording
            disableButtons();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error, log it, or display a message to the user
            Toast.makeText(this, "Failed to start recording: IO Exception", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            // Handle the error, log it, or display a message to the user
            Toast.makeText(this, "Failed to start recording: Illegal State Exception", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopRecording() {
        try {
            // Stop recording
            mediaRecorder.stop();
            mediaRecorder.reset();

            // Update recording state
            isRecording = false;
            // Re-enable buttons after recording
            enableButtons();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the error, log it, or display a message to the user
        }
    }

    private void onPlayButtonClick() {
        if (audioFile != null && audioFile.exists()) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                isPlaying = true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to play recording", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No recording available to play", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPauseButtonClick(View v) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void onStopButtonClick(View v) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            isPlaying = false;
        }
    }

    private void onSeekButtonClick(View v) {
        // Get the SeekBar from the layout
        SeekBar seekBar = findViewById(R.id.progress_bar); // Replace R.id.progress_bar with the ID of your SeekBar

        // Get the current progress of the SeekBar
        int progress = seekBar.getProgress();

        // Seek to the selected position in the MediaPlayer
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(progress);
        } else {
            // If the MediaPlayer is not playing, display a message or handle the situation accordingly
            Toast.makeText(this, "Media is not playing", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        // Handle media playback completion event if needed
    }


    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // Handle seek completion event if needed
    }
    private void onSpeedButtonClick(View v) {
        try {
            PlaybackParams params = mediaPlayer.getPlaybackParams();
            if (params == null) {
                params = new PlaybackParams();
            }

            // Adjust playback speed based on the current speed
            switch (playbackSpeed) {
                case 1:
                    // Increase to 1.2x speed
                    params.setSpeed(1.0f);
                    playbackSpeed = 3; // Reset to 1x
                    Toast.makeText(this, "Media is playing in 1x", Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    // Decrease to 1x speed
                    params.setSpeed(1.5f);
                    playbackSpeed = 2;
                    Toast.makeText(this, "Media is playing in 1.5x", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    // Increase to 2x speed
                    params.setSpeed(2.0f);
                    playbackSpeed = 1;
                    Toast.makeText(this, "Media is playing in 2x", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            mediaPlayer.setPlaybackParams(params);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            // Handle the IllegalStateException (possibly due to incorrect state)
        }
    }

    private void disableButtons() {
        // Disable buttons while recording
    }

    private void enableButtons() {
        // Re-enable buttons after recording
    }
}
