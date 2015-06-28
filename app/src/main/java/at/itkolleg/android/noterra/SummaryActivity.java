package at.itkolleg.android.noterra;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SummaryActivity extends ActionBarActivity implements View.OnClickListener {

    private String outputFile = null;
    private ImageView imageView;
    private ArrayList<String> imagePath = new ArrayList<>();
    private ArrayList<String> audioPath = new ArrayList<>();
    private DBHandler forstdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbackground));
        forstdb = new DBHandler(this);
        loadImage();
    }

    public void loadImage() {
        outputFile = forstdb.getRefFromImageTable().get(forstdb.getRefFromImageTable().size()-1);

        File imgFile = new File(outputFile);

        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView = (ImageView) this.findViewById(R.id.imageview);
            imageView.setImageBitmap(myBitmap);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_finish, menu);
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

    @Override
    public void onClick(View v) {

    }

    public void loadData() {
        imagePath = forstdb.getRefFromImageTable();
        audioPath = forstdb.getRefFromAudioTable();
    }

    public ArrayList<String> getImagePath() {
        return imagePath;
    }

    public ArrayList<String> getAudioPath() {
        return audioPath;
    }

    public void back(View v) {
        Intent intent = new Intent(SummaryActivity.this, InspectionActivity.class);
        startActivity(intent);
    }

    public void send(View v) throws IOException {
        HTTPHandler httpHandler = new HTTPHandler(this);
        loadData();
        try {
            if (getImagePath() != null) {
                for(String filepath : getImagePath()){
                    FTPHandler ftp1 = new FTPHandler(filepath);
                }
            }
            if (getAudioPath() != null) {
                for(String filepath : getAudioPath()){
                    FTPHandler ftp1 = new FTPHandler(filepath);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(SummaryActivity.this, MainActivity.class);
        startActivity(intent);


        Context context = getApplicationContext();
        CharSequence text = "Erfolgreich gespeichert";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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
            View rootView = inflater.inflate(R.layout.fragment_summary, container, false);
            return rootView;
        }
    }
}
