package com.example.followbot;
/*
Simple class to record position data, implemented through the open-source Wifips
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PosData implements Serializable {
    public static final int MAX_DISTANCE=99999999;
    private String name;
    public static final int MINIMUM_COMMON_ROUTERS=1;
    public HashMap<String, Integer> values = new HashMap<>();
    public HashMap<String,String> routers = new HashMap<>();

    public PosData(String name) {

        this.name=name;

    }
    public void addValue(Router router,int strength){

        values.put(router.getBSSID(), strength);
        routers.put(router.getBSSID(),router.getSSID());

    }
    public String getName() {
        return name;
    }
    public String toString() {
        String result="";
        result+=name+"\n";
        for(Map.Entry<String, Integer> e: this.values.entrySet())
            result+=routers.get(e.getKey())+" : "+e.getValue().toString()+"\n";

        return result;

    }
    public HashMap<String, Integer> getValues() {
        return values;
    }


    private boolean isFriendlyWifi(ArrayList<Router> wifis,String bssid){
        for(int i=0;i<wifis.size();i++){
            if(wifis.get(i).getBSSID().equals(bssid))
                return true;

        }
        return false;

    }
}
