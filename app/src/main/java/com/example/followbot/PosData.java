package com.example.followbot;

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

    public int uDistance(PosData arg,ArrayList<Router> friendlyWifis){
        int sum=0;
        int count=0;
        for(Map.Entry<String, Integer> e: this.values.entrySet()){
            int v;
            //Log.v("Key : ",arg.values.get(e.getKey()).toString());
            if(isFriendlyWifi(friendlyWifis,e.getKey()) && arg.values.containsKey(e.getKey()))
            {
                v=arg.values.get(e.getKey());
                sum+=Math.pow((v-e.getValue()),2);
                count++;
            }
        }
        if(count<MINIMUM_COMMON_ROUTERS){
            sum=MAX_DISTANCE;
        }

        return sum;
    }
    private boolean isFriendlyWifi(ArrayList<Router> wifis,String bssid){
        for(int i=0;i<wifis.size();i++){
            if(wifis.get(i).getBSSID().equals(bssid))
                return true;

        }
        return false;

    }
}
