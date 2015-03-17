package at.itkolleg.android.noterra;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.IOException;


public class NotesActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1888;
    private int recButtonCount = 1;
    private Chronometer myChrono = null;
    private ImageView imageView;
    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;
    ImageButton cameraButton;
    ImageButton recordButton;
    ImageButton playButton;
    ImageButton stopButton;
    ImageButton deleteAudioButton;
    ImageButton deleteButton;
    Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        loadImage();
        createRecorder();
        addButton();

    }

//-------------Button Hinzufügen und funktion hinzufügen zugriff auf kamera, ufnahme von audio und notiz aufnahme----
    public void addButton() {
        cameraButton = (ImageButton) findViewById(R.id.camerabutton);
        recordButton = (ImageButton) findViewById(R.id.recordbutton);
        playButton = (ImageButton) findViewById(R.id.playbutton);
        stopButton = (ImageButton) findViewById(R.id.stopbutton);
        deleteAudioButton = (ImageButton) findViewById(R.id.deleteaudiobutton);
        deleteButton = (ImageButton) findViewById(R.id.deletebutton);
        saveButton = (Button) findViewById(R.id.savebutton);
        myChrono = (Chronometer) findViewById(R.id.chronometer);


        cameraButton.setOnClickListener(this);
        recordButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        deleteAudioButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camerabutton:
                cameraButtonClick();
                break;
            case R.id.recordbutton:
                recordButtonClick();
                break;
            case R.id.playbutton:
                playButtonClick();
                break;
            case R.id.stopbutton:
                stopButtonClick();
                break;
            case R.id.deleteaudiobutton:
                deleteAudioButtonClick();
                break;
            case R.id.savebutton:
                saveButtonClick();
                break;
            case R.id.deletebutton:
                imageDeleteClick();
                break;
        }
    }

    private void cameraButtonClick() {


        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        Uri uriSavedImage = Uri.fromFile(new File("/storage/emulated/0/NOTErra/Media/Images/begehung_001.jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void recordButtonClick() {

        if(recButtonCount == 1){
            try {
                myRecorder.prepare();
                myRecorder.start();
                myChrono.start();

            } catch (IllegalStateException e) {
                // start:it is called before prepare()
                // prepare: it is called after start() or before setOutputFormat()
                e.printStackTrace();
            } catch (IOException e) {
                // prepare() fails
                e.printStackTrace();
            }

            recButtonCount ++;

            recordButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.stoprec));

            Context context = getApplicationContext();
            CharSequence text = "Aufnahme wird gestartet!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }else if (recButtonCount == 2){
            try {
                myRecorder.stop();
                myRecorder.release();
                myChrono.stop();
                myChrono.setBase(SystemClock.elapsedRealtime());
                myRecorder = null;

                recordButton.setEnabled(true);
                playButton.setEnabled(true);

            } catch (IllegalStateException e) {
                //  it is called before start()
                e.printStackTrace();
            } catch (RuntimeException e) {
                // no valid audio/video data has been received
                e.printStackTrace();
            }

            recButtonCount = 1;
            recordButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.record));

            Context context = getApplicationContext();
            CharSequence text = "Aufnahme wird gestoppt!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

    private void playButtonClick() {

        try{
            myPlayer = new MediaPlayer();
            myPlayer.setDataSource(outputFile);
            myPlayer.prepare();
            myPlayer.start();

            playButton.setEnabled(false);
            stopButton.setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Context context = getApplicationContext();
        CharSequence text = "Aufnahme wird abgespielt!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void stopButtonClick() {
        try {
            if (myPlayer != null) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
                playButton.setEnabled(true);
                stopButton.setEnabled(false);

                Toast.makeText(getApplicationContext(), "Audio wird gestoppt!",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAudioButtonClick(){
        new AlertDialog.Builder(this)
                .setTitle("Aufnahme löschen")
                .setMessage("Wollen Sie die Aufnahme löschen?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        File fdelete = new File("/storage/emulated/0/NOTErra/media/Audio/begehungAudio001.3gpp");
                        if (fdelete.exists()) {
                            fdelete.delete();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void saveButtonClick() {
        startActivity(new Intent(this, InspectionActivity.class));
    }

    private void imageDeleteClick() {
        new AlertDialog.Builder(this)
                .setTitle("Bild löschen")
                .setMessage("Wollen Sie das Bild löschen?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        File fdelete = new File("/storage/emulated/0/NOTErra/media/Images/begehung_001.jpg");
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                imageView.setImageDrawable(null);
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
//---------------------------------------------------------------------
//---------------Ladet das bild in den Imageview wenn eines vorhanden ist in der Ordner struktur------------------
    public void loadImage() {
        File imgFile = new File("/storage/emulated/0/NOTErra/Media/Images/begehung_001.jpg");

        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView = (ImageView) this.findViewById(R.id.imageview);
            imageView.setImageBitmap(myBitmap);
        }
    }
//---------------------------------------------------------------------
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadImage();
    }

    private void createRecorder(){

        outputFile = "/storage/emulated/0/NOTErra/Media/Audio/begehungAudio001.3gpp";
        if(!outputFile.isEmpty()){
            int i = 1;
            outputFile = "/storage/emulated/0/NOTErra/Media/Audio/begehungAudio" + i + ".3gpp";
        }
        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myRecorder.setOutputFile(outputFile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
            return rootView;
        }
    }
}
