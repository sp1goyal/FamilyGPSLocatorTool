package com.mytracker.familygpstracker.Activities;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mytracker.familygpstracker.Models.CircleJoin;
import com.mytracker.familygpstracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Thread.sleep;

import org.json.JSONException;
import org.json.JSONObject;

public class SendAlertActivity extends AppCompatActivity {

    @BindView(R.id.textView9) TextView t1_CounterTxt;
    @BindView(R.id.toolbar) Toolbar toolbar;


    int countValue = 5;
    Thread myThread;
    DatabaseReference circlereference,usersReference;
    FirebaseAuth auth;
    FirebaseUser user;
    String memberUserId;
    ArrayList<String> userIDsList;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAYOCtKVM:APA91bFddLkeN5sjCxzbRtn-MaoEnqxt6zrKEKlsx0NSa5VWC1TIdatcV7uDqAMTpbnAa5gitwOPuHgl8zW2dd14HEVbMUh19b_V9kQJHWKnbc2yFNoSoH7UCdgbzKBaaPvZXw287LJH";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_alert);

        ButterKnife.bind(this);
        auth = FirebaseAuth.getInstance();

        toolbar.setTitle("Send Help Alert");
        setSupportActionBar(toolbar);


        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }


        userIDsList = new ArrayList<>();
        user = auth.getCurrentUser();

        circlereference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("CircleMembers");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        myThread = new Thread(new ServerThread());
        myThread.start();

    }



    private class ServerThread implements Runnable
    {
        @Override
        public void run() {
            try {
                //do some heavy task here on main separate thread like: Saving files in directory, any server operation or any heavy task

                ///Once this task done and if you want to update UI the you can update UI operation on runOnUiThread method like this:

                while(countValue!=0) {

                    sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            t1_CounterTxt.setText(String.valueOf(countValue));
                            countValue = countValue - 1;

                        }
                    });




                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        circlereference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userIDsList.clear();
                                for (DataSnapshot dss: dataSnapshot.getChildren())
                                {
                                    memberUserId = dss.child("circlememberid").getValue(String.class);
                                    userIDsList.add(memberUserId);
                                }


                                if(userIDsList.isEmpty())
                                {
                                    Toast.makeText(getApplicationContext(),"No circle members. Please add some one to your circle.",Toast.LENGTH_SHORT).show();

                                }
                                else
                                {
                                    CircleJoin circleJoin = new CircleJoin(user.getUid());
                                    for(int i =0;i<userIDsList.size();i++)
                                    {

                                        usersReference.child(userIDsList.get(i)).child("HelpAlerts").child(user.getUid()).setValue(circleJoin)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            Toast.makeText(SendAlertActivity.this,"Alerts sent successfully.",Toast.LENGTH_SHORT).show();
                                                            JSONObject notification = new JSONObject();
                                                            JSONObject notifcationBody = new JSONObject();
                                                            try {
                                                                notifcationBody.put("title", "NOTIFICATION_TITLE");
                                                                notifcationBody.put("message", "NOTIFICATION_MESSAGE");

                                                                notification.put("to", "/topics/userABC");
                                                                notification.put("data", notifcationBody);
                                                            } catch (JSONException e) {
                                                                Log.e(TAG, "onCreate: " + e.getMessage() );
                                                            }
                                                            sendNotification(notification);
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(SendAlertActivity.this,"Could not send alerts. Please try again later.",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }

                                    finish();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(SendAlertActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });


            }
            catch (Exception e) {
                e.printStackTrace();

            }
        }
    }


    public void setCancel(View v)
    {

        Toast.makeText(this,"Alert cancelled.",Toast.LENGTH_SHORT).show();
        myThread.interrupt();
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SendAlertActivity.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    public static class MySingleton {
        private  static MySingleton instance;
        private RequestQueue requestQueue;
        private Context ctx;

        private MySingleton(Context context) {
            ctx = context;
            requestQueue = getRequestQueue();
        }

        public static synchronized MySingleton getInstance(Context context) {
            if (instance == null) {
                instance = new MySingleton(context);
            }
            return instance;
        }

        public RequestQueue getRequestQueue() {
            if (requestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking the
                // Activity or BroadcastReceiver if someone passes one in.
                requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
            }
            return requestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }
    }

}
