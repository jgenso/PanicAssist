package edu.cpp.preston.PanicAssist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StaticMethods {

    String message = "";

    public void sendNotification(String userObjectId, int type){

        if (type == 0){
            message = App.username + " has sent an alert!";
        } else if (type == 1){
            message = "Contact request from " + App.username;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User"); //For sending a push notification
        query.getInBackground(userObjectId, new GetCallback<ParseObject>() {
            public void done(ParseObject user, ParseException e) {
                if (e == null) {
                    String pushId = user.getString("pushId");

                    // WRONG WAY TO SEND PUSH - INSECURE!
                    ParseQuery pushQuery = ParseInstallation.getQuery();
                    pushQuery.whereEqualTo("installationId", pushId);

                    ParsePush push = new ParsePush();
                    push.setQuery(pushQuery); // Set our Installation query
                    push.setMessage(message);
                    push.sendInBackground();
                }
            }
        });
    }

    public static void showTimerDialog(final Activity activity, final int seconds){

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setPositiveButton(R.string.send_default, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendDefaultAlert(activity);
            }
        });

        builder.setNeutralButton(R.string.send_custom, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /*
                Intent intent = new Intent(getActivity(), CustomizeAlert.class);
                startActivity(intent);
                */
                //TODO open activity like before ^
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.setMessage("Send default alert now?"); // Do not remove this line of code

        CountDownTimer timer = new CountDownTimer(seconds * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                dialog.setMessage("Send default alert now?\nSeconds until sent: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                if (dialog.isShowing()) { // Dialog box is still open after allotted time, therefor send alert
                    dialog.cancel();
                    sendDefaultAlert(activity);
                }
            }
        };

        dialog.show();
        timer.start();

    }


    public static void sendDefaultAlert(final Activity activity) {
        GPSTracker gps = new GPSTracker(activity);
        //TODO WAIT UNTIL GET GOOD LPCATION TO SEND!!!!!!!!!!!!!!!!!
        if (gps == null){
            gps = new GPSTracker(activity);
        }

        SharedPreferences sharedPrefContacts = activity.getSharedPreferences(activity.getString(R.string.preference_file_contacts_key), Context.MODE_PRIVATE);
        SharedPreferences sharedPrefQuickTexts = activity.getSharedPreferences(activity.getString(R.string.preference_file_quick_text_key), Context.MODE_PRIVATE);

        if (gps.canGetLocation()){
            String lat = gps.getLatitude() + "";
            String lon = gps.getLongitude() + "";
            String geoUri = "http://maps.google.com/maps?q=loc:" + lat + "," + lon + "(" + App.username + ")";
            String userMessage = sharedPrefQuickTexts.getString("quicktext0", ""); //TODO get user-set default message
            String myLocation = "My location is: " + geoUri;
            DateFormat df = new SimpleDateFormat("h:mm a");
            String time = df.format(Calendar.getInstance().getTime());
            df = new SimpleDateFormat("MM/dd/yy");
            String date = df.format(Calendar.getInstance().getTime());

            for (int j = 0; j < 50; j++) { //removes request from preferences
                if (sharedPrefContacts.contains("displayname" + j)) {
                    if (sharedPrefContacts.getString("isConfirmed" + j, "*").equalsIgnoreCase("true")) {
                        if (sharedPrefContacts.getString("isNumber" + j, "*").equalsIgnoreCase("true")) { //send text here
                            CompatibilitySmsManager smsManager = CompatibilitySmsManager.getDefault();
                            smsManager.sendTextMessage("+" + sharedPrefContacts.getString("usernameOrNumber" + j, "ERROR"), null, userMessage, null, null);
                            smsManager.sendTextMessage("+" + sharedPrefContacts.getString("usernameOrNumber" + j, "ERROR"), null, myLocation, null, null);
                        } else {
                            ParseObject notification = new ParseObject("Notification"); //make new notification
                            notification.put("sender", App.username);
                            notification.put("type", "alert");
                            notification.put("receiverId", sharedPrefContacts.getString("userObjectId" + j, "ERROR"));
                            notification.put("message", userMessage);
                            notification.put("lat", lat);
                            notification.put("lon", lon);
                            notification.put("time", time);
                            notification.put("date", date);
                            notification.saveEventually(); //save notification on server

                            new StaticMethods().sendNotification(sharedPrefContacts.getString("userObjectId" + j, "ERROR"), 0);
                        }
                    }
                }
            }

            Toast.makeText(activity, "Alerts sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Unable to get location!", Toast.LENGTH_SHORT).show();
        }
    }
}
