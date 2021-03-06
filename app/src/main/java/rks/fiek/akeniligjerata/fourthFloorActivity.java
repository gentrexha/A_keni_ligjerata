package rks.fiek.akeniligjerata;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.joda.time.LocalTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

//Reference:
//http://stackoverflow.com/questions/1200621/how-to-declare-an-array
//http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
//http://stackoverflow.com/questions/13397709/android-hide-imageview

/**
 * Created by ${USER} on ${DATE}
 */

public class fourthFloorActivity extends AppCompatActivity {

    private DBHelper objDB;
    private static final String lecturesDBURL = "http://200.6.254.247/my-service.php";
    private static final String commentDBURL = "http://200.6.254.247/comments.php?t=0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth_floor);

        ImageView imgv4thFloor = (ImageView) findViewById(R.id.imgvPlan);
        // ImageView imgv4thFloor_Area = (ImageView) findViewById(R.id.imgvPlan_Area);
        objDB = new DBHelper(this);

        if (isNetworkAvailable())
        {
            new RetrieveSchedule().execute();
            new RetrieveComments().execute();
        }
        else {
            Toast.makeText(this,"Schedule might be not updated! Please connect to the internet to update.",Toast.LENGTH_LONG).show();
            colorAvailability("411");
            colorAvailability("401");
            colorAvailability("408");
            colorAvailability("414");
            colorAvailability("415");
        }



        imgv4thFloor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        final int x = (int) motionEvent.getX();
                        final int y = (int) motionEvent.getY();
                        int touch_color = getHotspotColor(x, y);
                        int tolerance = 25;
                        if (closeMatch(Color.BLUE, touch_color, tolerance)) {
                            Intent intFifthFloor = new Intent(getApplicationContext(), fifthFloorActivity.class);
                            startActivity(intFifthFloor);
                        }
                        if (closeMatch(Color.RED, touch_color, tolerance)) {
                            Intent int401 = new Intent(getApplicationContext(), class401Activity.class);
                            startActivity(int401);
                        }
                        if (closeMatch(Color.YELLOW, touch_color, tolerance)) {
                            Intent int414 = new Intent(getApplicationContext(), class414Activity.class);
                            startActivity(int414);
                        }
                        if (closeMatch(Color.GREEN, touch_color, tolerance)) {
                            Intent int408 = new Intent(getApplicationContext(), class408Activity.class);
                            startActivity(int408);
                        }
                        if (closeMatch(Color.MAGENTA, touch_color, tolerance)) {
                            Intent int411 = new Intent(getApplicationContext(), class411Activity.class);
                            startActivity(int411);
                        }
                        if (closeMatch(Color.CYAN, touch_color, tolerance)) {
                            Intent int415 = new Intent(getApplicationContext(), class415Activity.class);
                            startActivity(int415);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private int getHotspotColor(int x, int y) {
        ImageView img = (ImageView) findViewById(R.id.imgvPlan_Area);
        img.setDrawingCacheEnabled(true);
        Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
        img.setDrawingCacheEnabled(false);
        return hotspots.getPixel(x, y);
    }

    public boolean closeMatch(int color1, int color2, int tolerance) {
        return Math.abs(Color.blue(color1) - Color.blue(color2)) <= tolerance
                && Math.abs (Color.green (color1) - Color.green (color2)) <= tolerance
                && Math.abs (Color.red (color1) - Color.red (color2)) <= tolerance;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public class RetrieveSchedule extends AsyncTask<Void, Void, JSONArray> {

        ProgressDialog objProgressDialog;
        Exception mException;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.mException = null;
            objProgressDialog = ProgressDialog.show(fourthFloorActivity.this,
                    "Loading schedule...","Please wait while the schedule is downloading", true);
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {

            StringBuilder urlString = new StringBuilder();
            urlString.append(lecturesDBURL);

            HttpURLConnection objURLConnection = null;
            URL objURL;
            JSONArray objJSON = null;
            InputStream objInStream = null;

            try {
                objURL = new URL(urlString.toString());
                objURLConnection = (HttpURLConnection) objURL.openConnection();
                objURLConnection.setRequestMethod("GET");
                objURLConnection.setDoOutput(true);
                objURLConnection.setDoInput(true);
                objURLConnection.connect();
                objInStream = objURLConnection.getInputStream();
                BufferedReader objBReader = new BufferedReader(new InputStreamReader(objInStream));
                String line;
                String response = "";
                while ((line = objBReader.readLine()) != null) {
                    response += line;
                }
                objJSON = (JSONArray) new JSONTokener(response).nextValue();
            } catch (Exception e) {
                this.mException = e;
            } finally {
                if (objInStream != null) {
                    try {
                        objInStream.close(); // this will close the bReader as well
                    } catch (IOException ignored) {
                    }
                }
                if (objURLConnection != null) {
                    objURLConnection.disconnect();
                }
            }
            return objJSON;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            objProgressDialog.dismiss();

            if (this.mException != null) {
                Log.e("JSON Exception", this.mException.toString());
            }
            try {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject jsonObjectLecture = result.getJSONObject(i);
                    int lectureID = jsonObjectLecture.getInt("id");
                    String lectureDay = jsonObjectLecture.getString("day");
                    String lectureClassNumber = jsonObjectLecture.getString("classnumber");
                    String lectureClassName = jsonObjectLecture.getString("classname");
                    String lectureStartTime = jsonObjectLecture.getString("starttime");
                    String lectureEndTime = jsonObjectLecture.getString("endtime");
                    objDB.insertLectureOrIgnore(lectureID, lectureDay, lectureClassNumber,lectureClassName, lectureStartTime, lectureEndTime);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            colorAvailability("411");
            colorAvailability("401");
            colorAvailability("408");
            colorAvailability("414");
            colorAvailability("415");
        }
    }

    public class RetrieveComments extends AsyncTask<Void,Void,JSONArray>{

        Exception mException;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.mException = null;
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            StringBuilder urlString = new StringBuilder();
            urlString.append(commentDBURL);

            HttpURLConnection objURLConnection = null;
            URL objURL;
            JSONArray objJSON = null;
            InputStream objInStream = null;

            try {
                objURL = new URL(urlString.toString());
                objURLConnection = (HttpURLConnection) objURL.openConnection();
                objURLConnection.setRequestMethod("GET");
                objURLConnection.setDoOutput(true);
                objURLConnection.setDoInput(true);
                objURLConnection.connect();
                objInStream = objURLConnection.getInputStream();
                BufferedReader objBReader = new BufferedReader(new InputStreamReader(objInStream));
                String line;
                String response = "";
                while ((line = objBReader.readLine()) != null) {
                    response += line;
                }
                objJSON = (JSONArray) new JSONTokener(response).nextValue();
            } catch (Exception e) {
                this.mException = e;
            } finally {
                if (objInStream != null) {
                    try {
                        objInStream.close(); // this will close the bReader as well
                    } catch (IOException ignored) {
                    }
                }
                if (objURLConnection != null) {
                    objURLConnection.disconnect();
                }
            }
            return objJSON;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            if (this.mException != null) {
                Log.e("JSON Exception", this.mException.toString());
            }
            try {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject jsonObjectLecture = result.getJSONObject(i);
                    int commentID = jsonObjectLecture.getInt("id");
                    String commentClassroom = jsonObjectLecture.getString("classroom");
                    String commentContent = jsonObjectLecture.getString("commentcontent");
                    String reg_date = jsonObjectLecture.getString("reg_date");
                    objDB.insertCommentOrIgnore(commentID, commentClassroom, commentContent,reg_date);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void colorAvailability(String classnumber) {

        Cursor objCursor = objDB.getTodayLecturesTimes(classnumber);
        ArrayList<String> startTime = new ArrayList<>();
        ArrayList<String> endTime = new ArrayList<>();

        int nrRows = objCursor.getCount();

        if(nrRows >0 ) {
            for (objCursor.moveToFirst(); !objCursor.isAfterLast(); objCursor.moveToNext()) {
                // TO DO code here
                startTime.add(objCursor.getString(0));
                endTime.add(objCursor.getString(1));
            }
        }

        objCursor.close();
        ImageView imageView;
        if(nrRows < 1) {
            String nrClass = "imgvClass" + classnumber +"Green";
            int resID = getResources().getIdentifier(nrClass, "id", getPackageName());
            imageView = (ImageView) findViewById(resID);
            imageView.setVisibility(View.VISIBLE);
        }
        else
        {
            int numberOfRows = startTime.size();
            for (int i = 0; i < numberOfRows; i++)
            {
                try {
                    LocalTime now = LocalTime.now();

                    String strStarttime = startTime.get(i);
                    String strEndtime = endTime.get(i);

                    LocalTime ltStarttime = new LocalTime(strStarttime);
                    LocalTime ltEndtime = new LocalTime(strEndtime);

                    if (now.isAfter(ltStarttime) && now.isBefore(ltEndtime)) {
                        String nrClass = "imgvClass" + classnumber + "Red";
                        int resID = getResources().getIdentifier(nrClass, "id", getPackageName());
                        imageView = (ImageView) findViewById(resID);
                        imageView.setVisibility(View.VISIBLE);
                    }
                    else {
                        String nrClass = "imgvClass" + classnumber + "Green";
                        int resID = getResources().getIdentifier(nrClass, "id", getPackageName());
                        imageView = (ImageView) findViewById(resID);
                        imageView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}