package com.eldhopj.mlkitsample.Utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
/**Helper class to check the internet connection*/
public class InternetCheck extends AsyncTask<Void,Void,Boolean> {
    private static final String TAG = "InternetCheck";

   public interface Consumer{
        void accept(boolean internet);
    }

      private Consumer consumer;

    public InternetCheck(Consumer consumer) {
        this.consumer = consumer;
        execute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com",80),1500);
            socket.close();
            return true;
        } catch (IOException e) {
            Log.d(TAG, "Internet connection exception: " +e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        consumer.accept(aBoolean);
    }
}
