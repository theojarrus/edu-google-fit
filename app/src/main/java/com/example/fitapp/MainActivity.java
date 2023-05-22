package com.example.fitapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitapp.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int FIT_CODE = 100;

    private FitnessOptions fitnessOptions;
    private GoogleSignInAccount account;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.access.setOnClickListener(v -> {
            init();
            auth();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIT_CODE) {
            if (resultCode == RESULT_OK) {
                accessGoogleFit();
            } else {
                Toast.makeText(this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void init() {
        fitnessOptions = getFitnessOptions();
        account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
    }

    private void auth() {
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, FIT_CODE, account, fitnessOptions);
        } else {
            accessGoogleFit();
        }
    }

    private void accessGoogleFit() {
        getHeartBpmFitnessData(); // for example
    }

    private void getHeartBpmFitnessData() {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime yearBefore = current.minusYears(1);
        DataType dataType = DataType.TYPE_HEART_RATE_BPM;

        Fitness.getHistoryClient(this, account)
                .readData(getFitnessRequest(yearBefore, current, dataType))
                .addOnSuccessListener(dataReadResponse -> {
                    // TODO
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                        stringBuilder.append("Activity: ");
                        stringBuilder.append("\n");
                        stringBuilder.append(bucket.getActivity());
                        stringBuilder.append("\n");
                        stringBuilder.append("Datasets: ");
                        for (DataSet dataSet : bucket.getDataSets()) {
                            stringBuilder.append(dataSet.getDataPoints());
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append("\n");
                    }
                    binding.data.setText(stringBuilder.toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private DataReadRequest getFitnessRequest(LocalDateTime start, LocalDateTime end, DataType dataType) {
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        return new DataReadRequest.Builder()
                .aggregate(dataType)
                .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();
    }

    private FitnessOptions getFitnessOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM)
                // add more data types
                .build();
    }
}
