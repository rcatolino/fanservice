package com.rcatolino.fanservice;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class FanService extends TileService
{

  private class TcpHandler extends AsyncTask<Boolean, Void, String> {
    private TileService parent;

    private TcpHandler(TileService parent) {
      this.parent = parent;
    }

    protected String doInBackground(Boolean... flip) {
      // Open connection :
      Socket sock = new Socket();
      InetSocketAddress adress = new InetSocketAddress("192.168.2.100", 4875);
      try {
        sock.connect(adress, 1000);
        DataOutputStream output = new DataOutputStream(sock.getOutputStream());
        BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream(), "US-ASCII"));
        String status = input.readLine();
        if (flip.length > 0 && flip[0]) {
          if (status.equals("1")) {
            Log.d(TAG, "Stopping fan");
            output.write("0\n".getBytes("US-ASCII"));
            status = "0";
          } else if (status.equals("0")) {
            Log.d(TAG, "Starting fan");
            output.write("1\n".getBytes("US-ASCII"));
            status = "1";
          }
        }
        sock.close();
        return status;
      } catch (IOException e) {
        Log.d(TAG, e.getMessage());
      }

      return new String("");
    }

    protected void onPostExecute(String result) {
      Tile tile = parent.getQsTile();
      if (result.equals("1") && tile.getState() == Tile.STATE_INACTIVE) {
        tile.setState(Tile.STATE_ACTIVE);
        tile.setIcon(Icon.createWithResource(parent, R.drawable.fan240on));
      } else if (result.equals("0") && tile.getState() == Tile.STATE_ACTIVE) {
        tile.setState(Tile.STATE_INACTIVE);
        tile.setIcon(Icon.createWithResource(parent, R.drawable.fan240));
      }
      tile.updateTile();
    }
  }

  public static String TAG = "FanService";
  /** Called when the activity is first created. */
  @Override
  public void onClick()
  {
    super.onClick();
    Log.d(TAG, "clicked fan quick settings");
    new TcpHandler(this).execute(new Boolean(true));
  }

  @Override
  public void onStartListening() {
    new TcpHandler(this).execute(new Boolean(false));
  }
}
