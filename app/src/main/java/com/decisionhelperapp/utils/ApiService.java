package com.decisionhelperapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class ApiService {
    // Check if internet is available using modern API
    public static boolean isInternetAvailable(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
    }

    // Fetch data from a given URL with proper error handling for no/slow connection
    public static String fetchDataFromUrl(@NonNull Context context, @NonNull String urlString) {
        if (!isInternetAvailable(context)) {
            return "No internet connection available";
        }
        
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "Server returned: " + responseCode;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (MalformedURLException e) {
            return "Invalid URL: " + e.getMessage();
        } catch (SocketTimeoutException e) {
            return "Connection timed out";
        } catch (IOException e) {
            return "Network error: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}