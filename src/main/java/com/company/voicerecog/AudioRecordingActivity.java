package com.company.voicerecog;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AudioRecordingActivity extends Activity {

	private static String TAG = "voicerecog";
	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
	private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private MediaRecorder recorder = null;
	private MediaPlayer player = null;
	private int currentFormat = 0;
	private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,
			MediaRecorder.OutputFormat.THREE_GPP };
	private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4,
			AUDIO_RECORDER_FILE_EXT_3GP };
	
	private String currentFileName = null;

	private ProgressBar progressBar = null;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.main);
		setButtonHandlers();
		enableButtons(false);
		enablePlayButtons(false);
		setFormatButtonCaption();
	}

	private void setButtonHandlers() {
		((Button) findViewById(R.id.recordButton)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnFormat)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.stopButton)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.playButton)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.stopPlayButton)).setOnClickListener(btnClick);
	}

	private void enableButton(int id, boolean isEnable) {
		((Button) findViewById(id)).setEnabled(isEnable);
	}

	private void enableButtons(boolean isRecording) {
		enableButton(R.id.recordButton, !isRecording);
		enableButton(R.id.btnFormat, !isRecording);
		enableButton(R.id.stopButton, isRecording);
	}
	
	private void enablePlayButtons(boolean isPlaying){
		enableButton(R.id.playButton, !isPlaying);
		enableButton(R.id.stopPlayButton, isPlaying);
	}

	private void setFormatButtonCaption() {
		((Button) findViewById(R.id.btnFormat))
				.setText(getString(R.string.audio_format) + " ("
						+ file_exts[currentFormat] + ")");
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
	}

	private void startRecording() {
		recorder = new MediaRecorder();

		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(output_formats[currentFormat]);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		currentFileName = getFilename(); 
		recorder.setOutputFile(currentFileName);

		recorder.setOnErrorListener(errorListener);
		recorder.setOnInfoListener(infoListener);

		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void stopRecording() {
		if (null != recorder) {
			recorder.stop();
			recorder.reset();
			recorder.release();

			recorder = null;
		}
	}

	private void displayFormatDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String formats[] = { "MPEG 4", "3GPP" };

		builder.setTitle(getString(R.string.choose_format_title))
				.setSingleChoiceItems(formats, currentFormat,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								currentFormat = which;
								setFormatButtonCaption();

								dialog.dismiss();
							}
						}).show();
	}

	private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			Toast.makeText(AudioRecordingActivity.this,
					"Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
		}
	};

	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			Toast.makeText(AudioRecordingActivity.this,
					"Warning: " + what + ", " + extra, Toast.LENGTH_SHORT)
					.show();
		}
	};
	
	private MediaPlayer.OnCompletionListener playFinished = new MediaPlayer.OnCompletionListener() {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			player.stop();
			player.release();
			player = null;
		}
	};

	private View.OnClickListener btnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.recordButton: {
				Toast.makeText(AudioRecordingActivity.this,
						"Started Recording Now!", Toast.LENGTH_SHORT).show();
				enableButtons(true);
				startRecording();
				showProgressBar(v, View.VISIBLE);
				break;
			}
			case R.id.stopButton: {
				Toast.makeText(AudioRecordingActivity.this,
						"Stopped Recording Now!", Toast.LENGTH_SHORT).show();
				enableButtons(false);
				stopRecording();
				showProgressBar(v, View.GONE);
				break;
			}
			case R.id.btnFormat: {
				displayFormatDialog();

				break;
			}
			case R.id.playButton: {
				enablePlayButtons(true);
				playRecordedFile();
				break;
			}
			case R.id.stopPlayButton: {
				enablePlayButtons(false);
				stopPlaying();
				break;
			}
			}
		}
	};

	public void showProgressBar(View source, int visiblity) {

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setVisibility(visiblity);
	}
	
	private void playRecordedFile() {
		if(player==null){
			player = new MediaPlayer();
		}
		player.reset();
		
		try {
			player.setDataSource(currentFileName);
			if(player.isPlaying()){
				if(player!=null){
					player.pause();
	            }
			} else {
				player.prepare();
				player.start();
				player.setVolume(1f, 1f);
				player.setOnCompletionListener(playFinished);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void stopPlaying() {
		if(player!=null){
			if(player.isPlaying())
				player.stop();
				player.release();
		}
	}
}
