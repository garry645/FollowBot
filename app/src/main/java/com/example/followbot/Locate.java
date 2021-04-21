package com.example.followbot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.followbot.PosData.MAX_DISTANCE;
import static com.example.followbot.PosData.MINIMUM_COMMON_ROUTERS;

public class Locate extends AppCompatActivity {
    String building;
    Button locate;
    TextView result;
    SimpleDatabase db;
    WifiManager wifi;
    PosData positionData;


    public class ResultData {
        private Router router;

        public Router getRouter() {
            return this.router;
        }

        public List<Integer> values;

        public ResultData(Router router) {

            this.router = router;
            values = new ArrayList<Integer>();
        }
    }
    public int uDistance(PosData arg, ArrayList<Router> friendlyWifis){
        int sum=0;
        int count=0;
        for(Map.Entry<String, Integer> e: this.values.entrySet()) {
            int v;

            v = arg.values.get(e.getKey());
            sum += Math.pow((v - e.getValue()), 2);
            count++;
        }
        if(count<0){
            sum=9999;
        }

        return sum;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        locate = (Button) findViewById(R.id.locate);
        result = (TextView) findViewById(R.id.result);
        locate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = getIntent();
                wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                wifi.startScan();
                List<ScanResult> results = wifi.getScanResults();

                ArrayList<ResultData> resultsData = new ArrayList<>();

                for (int i = 0; i < results.size(); i++) {
                    String ssid = results.get(i).SSID;
                    String bssid = results.get(i).BSSID;
                    int rssi0 = results.get(i).level;
                    ResultData data = new ResultData(new Router(ssid, bssid));
                    data.values.add(rssi0);
                    resultsData.add(data);
                }

                for (int length = 0; length < resultsData.size(); length++) {

                    int sum = 0;
                    for (int l = 0; l < resultsData.get(length).values.size(); l++) {
                        sum += resultsData.get(length).values.get(l);

                    }
                    int average = sum / resultsData.get(length).values.size();

                    positionData.addValue(resultsData.get(length).getRouter(), average);
                }
                String closestPosition = null;
                ArrayList<Router> wifis = db.getFriendlyWifis(building);


                //below from open source WiFiIPS
                int min_distance = uDistance(positionData.get(0), wifis);
                int j = 0;
                closestPosition = positionData.get(0).getName();
                String res = "";
                res += closestPosition + "\n" + min_distance;
                res += "\n" + positionData.get(0).toString();
                for (int i = 1; i < positionData.size(); i++) {
                    int distance = uDistance(positionData.get(i), wifis);
                    res += "\n" + positionData.get(i).getName() + "\n" + distance;
                    res += "\n" + positionData.get(i).toString();
                    if (distance < min_distance) {
                        min_distance = distance;
                        j = i;
                        closestPosition = positionData.get(i).getName();

                    }

                }
                res += "\nCurrent:\n" + positionData.toString();
                result.setText(res);


            }
        });
    }

}