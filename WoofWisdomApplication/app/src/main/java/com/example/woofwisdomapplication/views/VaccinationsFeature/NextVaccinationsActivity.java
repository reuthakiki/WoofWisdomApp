package com.example.woofwisdomapplication.views.VaccinationsFeature;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woofwisdomapplication.DTO.NextVaccination;
import com.example.woofwisdomapplication.DTO.UserObject;
import com.example.woofwisdomapplication.MainActivity;
import com.example.woofwisdomapplication.R;
import com.example.woofwisdomapplication.findNearestVetActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NextVaccinationsActivity extends AppCompatActivity {
    private static final String IP = System.getProperty("IP");//"192.168.10.57";
    private LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_vaccinations);

        linearLayout = findViewById(R.id.linearLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userJson = sharedPreferences.getString("user", null);

        int dogAgeInWeeks = 12;

        if (userJson != null) {
            // Deserialize the JSON string back into a UserObject
            UserObject user = new Gson().fromJson(userJson, UserObject.class);
            Integer dogAge = user.getDogAge();
            // Extract and set the first name and last name
            if (dogAge != null){
                dogAgeInWeeks = dogAge * 52;
            }
        }

        // Create a Retrofit instance and interface for making the API call
        String url = "http://" + IP + ":8091/" +
                "next-vaccinations?dogAgeInWeeks=" +
                String.valueOf(dogAgeInWeeks);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/json");

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<NextVaccination>>() {
                            }.getType();
                            List<NextVaccination> vaccinations = null;
                            try {
                                vaccinations = gson.fromJson(response.body().string(), listType);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            for (NextVaccination vac : vaccinations) {
                                View vaccinationItemView = LayoutInflater.from(NextVaccinationsActivity.this).inflate(R.layout.recommended_vaccination_item, null);
                                TextView txtVaccinationName = vaccinationItemView.findViewById(R.id.txtVaccinationName);
                                TextView txtVaccinationDetails = vaccinationItemView.findViewById(R.id.txtVaccinationDetails);
                                Button btnSetReminder = vaccinationItemView.findViewById(R.id.btnSetReminder);

                                txtVaccinationName.setText(vac.getName());

                                String details = "";
                                if (vac.getInWeeks() > 0) {
                                    details += "In " + vac.getInWeeks() + " weeks";
                                }
                                if (vac.getInGeneral() != 0) {
                                    if (!details.isEmpty()) {
                                        details += ", ";
                                    }
                                    details += "In general " + vac.getInGeneral();
                                }
                                txtVaccinationDetails.setText(details);

                                btnSetReminder.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent secondActivityIntent = new Intent(
                                                getApplicationContext(), ImmunizationsRecordActivity.class
                                        );
                                        secondActivityIntent.putExtra("vaccinationName", vac.getName());
                                        secondActivityIntent.putExtra("inWeeks", vac.getInWeeks());
                                        secondActivityIntent.putExtra("inGeneral", vac.getInGeneral());
                                        startActivity(secondActivityIntent);
                                    }
                                });

                                linearLayout.addView(vaccinationItemView);
                            }
                        }
                    });
                } else {
                    // Handle the error
                    //Toast.makeText(AddVaccinationActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            // Handle "Home" click here, maybe go to the main activity or dashboard
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_return) {
            // Handle "Return" click, maybe just close the current activity
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}