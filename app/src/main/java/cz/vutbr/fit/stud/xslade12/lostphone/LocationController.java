package cz.vutbr.fit.stud.xslade12.lostphone;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Třída pro získání polohy zařízení z GPS nebo ze síťě
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class LocationController {
    Timer timer;
    LocationManager locationManager;
    LocationResult locationResult;

    boolean gpsEnabled = false;
    boolean networkEnabled = false;

    /**
     * Získej polohu zařízení
     * @param context Appliacetion context
     * @param result Třída zpracující výsledek
     * @return
     */
    public boolean getLocation(Context context, LocationResult result)
    {

        locationResult = result;
        // Systémová služba pro práci s lokací
        if(locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Zjištění jestli je dostupný provider GPS a Síť
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch( Exception ex ) {}
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch( Exception ex ) {}

        // Zadnej provider neni aktivovany
        if(!gpsEnabled && !networkEnabled)
            return false;

        // zaregistruju listener na zjisteni polohy z GPS a zahajim hledani
        if(gpsEnabled)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        // zaregistruju listener na zjisteni polohy ze síťě a zahajim hledani
        if(networkEnabled)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

        // pro sychr po 20 sekundách pošlu poslední známou polohu
        timer = new Timer();
        timer.schedule(new GetLastLocation(), 20000);

        return true;
    }

    /**
     * Listener k hledani polohy přes GPS
     */
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel(); // poloha nalezena => ukončím sychr variantu

            // Vratim nalezenou pozici
            locationResult.gotLocation(location);

            // Ukoncim hledani gps i sítě
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    /**
     * Listener k hledani polohy přes Síť
     */
    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel(); // poloha nalezena => ukončím sychr variantu

            // Vratim nalezenou pozici
            locationResult.gotLocation(location);

            // Ukoncim hledani sítě i GPS
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };


    /**
     * Pokud se nepodařilo nalezt aktualni souřadnice, použijeme ty naposledy známé
     */
    class GetLastLocation extends TimerTask {
        @Override
        public void run() {

            // Ukoncim hledani sítě i GPS
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);

            Location locGps = null;
            Location locNet = null;

            // Zjiskam poslední znamou polohu z GPS i ze sítě
            if(gpsEnabled)
                locGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(networkEnabled)
                locNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            // Pokud mám obě, najdu nejaktuálnější
            if(locGps != null && locNet != null){
                if(locGps.getTime() > locNet.getTime())
                    locationResult.gotLocation(locGps);
                else
                    locationResult.gotLocation(locNet);
                return;
            } else if(locGps!=null){
                locationResult.gotLocation(locGps);
                return;
            }
            else if(locNet!=null){
                locationResult.gotLocation(locNet);
                return;
            }

            locationResult.gotLocation(null);
        }
    }

    /**
     * Abstraktni třída pro zpracovnání výsledku hledání
     */
    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}