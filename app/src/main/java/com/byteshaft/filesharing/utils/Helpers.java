package com.byteshaft.filesharing.utils;

import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Helpers {
    public static String generateSSID(String identifier, String port) {
        try {
            return String.format(
                    "SH-%s-%s",
                    Base64.encodeToString(identifier.getBytes("UTF-8"), Base64.DEFAULT).trim(),
                    Base64.encodeToString(port.getBytes("UTF-8"), Base64.DEFAULT).trim()
            );
        } catch (UnsupportedEncodingException e) {
            return "UNABLE";
        }
    }

    public static boolean locationEnabled() {
        LocationManager lm = (LocationManager) Application.getContext()
                .getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled || network_enabled;
    }

    @WorkerThread
    public static void sendFileOverNetwork(String hostIP, String port, String filePath) {
        try {
            Socket sock = new Socket(hostIP, Integer.valueOf(port));
            File myFile = new File(filePath);
            byte[] fileBytesArray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(fileBytesArray, 0, fileBytesArray.length);
            OutputStream os = sock.getOutputStream();

            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(fileBytesArray.length);
            dos.write(fileBytesArray, 0, fileBytesArray.length);
            dos.flush();

            //Closing socket
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ensureDataDirectoryCreated(Context context) {
        String appName = "FileSharing";
        File directory = new File(Environment.getExternalStorageDirectory() + "/" + appName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static String getImgPath(Context context, Uri uri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        return cursor.getString(columnIndex);
    }

    public static String decodeString(String base64String) {
        byte[] data = Base64.decode(base64String, Base64.DEFAULT);
        String text = null;
        try {
            text = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }
}
