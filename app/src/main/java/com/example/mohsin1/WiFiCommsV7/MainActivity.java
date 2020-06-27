package com.example.mohsin1.WiFiCommsV7;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    String myCoordinates, peerCoordinates;
    String dRadius="100";
    int initialRun=0;
    LatLng emergencyCoordinates;
    double lat, lon, emergencyLat,emergencyLon;
    String eMessage="Safety risk found in current location, Please leave the area";
    String sMessage="In safe Zone";
    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    Button button;
    TextView textView;
    LocationManager locationManager;
    LocationListener locationListener;
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    Marker marker = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
        mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    // Location Handling *************************************************************************************************************************************
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat=location.getLatitude();
                lon=location.getLongitude();
                LatLng latLng=new LatLng(lat,lon);
                textView.setText("Coordinates"+"\n"+lat+" "+lon);
                if (initialRun<1){
                    emergencyCoordinates=latLng;
                    emergencyLat=lat;
                    emergencyLon=lon;
                    drawCircle(emergencyCoordinates);
                    initialRun=initialRun+1;
                }

                Geocoder geocoder= new Geocoder(getApplicationContext());
                try {

                    List<Address> addressList=geocoder.getFromLocation(lat,lon,1);
                    String str=addressList.get(0).getLocality()+", ";
                    str+=addressList.get(0).getCountryName();
                    if (marker!=null) {
                        marker.remove();
                    }
                    marker=mMap.addMarker(new MarkerOptions().position(latLng).title(str));



                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
                    float [] results=new float [10];
                    Location.distanceBetween(lat,lon,emergencyLat,emergencyLon,results);
                    if(results[0]<100){
                        read_msg_box.setText(eMessage);
                        Toast.makeText(getApplicationContext(),"EMERGENCY ALERT", Toast.LENGTH_SHORT).show();

                    }else{
                        read_msg_box.setText(sMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                myCoordinates=lat+"\n"+lon;
                sendReceive.write(myCoordinates.getBytes());
//                myCoordinates="51.2"+"\n"+"-0.23";
//                if(peerCoordinates==null){
//                    peerCoordinates=myCoordinates;
//                }
//                String fileName = "myfile.txt";
//                String fileContents = myCoordinates+"\n"+dRadius+"\n"+peerCoordinates;
//                if (isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
//                    File textFile=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),fileName);
//                    try{
//
//                        FileOutputStream fileOutputStream=new FileOutputStream(textFile);
//                        fileOutputStream.write(fileContents.getBytes());
//                        fileOutputStream.close();
//                        Toast.makeText(getApplicationContext(),"File Saved", Toast.LENGTH_SHORT).show();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }else{
//                    Toast.makeText(getApplicationContext(),"File Not Saved", Toast.LENGTH_SHORT).show();
//                }




//                File file = new File(context.getFilesDir(), filename);
//                FileOutputStream outputStream;
//
//                try {
//                    outputStream = openFileOutput(filename, MODE_PRIVATE);
//                    outputStream.write(fileContents.getBytes());
//                    outputStream.close();
//                    Toast.makeText(getApplicationContext(),"Text Saved", Toast.LENGTH_SHORT).show();
//
//
//                FileInputStream fileInputStream=openFileInput(filename);
//                InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
//
//                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
//
//                StringBuffer stringBuffer=new StringBuffer();
//                String lines;
//                while ((lines=bufferedReader.readLine())!=null) {
//                    stringBuffer.append(lines + "\n");
//                }
//                    read_msg_box.setText(stringBuffer.toString());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

// Location Handling *************************************************************************************************************************************





            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET
            },10);
            return;
        }else{
            configureButton();
        }

    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
        LatLng hendon = new LatLng(51.5883319,-0.2300675);
//        mMap.addMarker(new MarkerOptions().position(hendon).title("Marker in Hendon"));
//        drawCircle(hendon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hendon,15));
    }

    private void drawCircle(LatLng point){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(100);

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }

    private void configureButton() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
                }else if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 5000, 0, locationListener);
                };

            }
        });

    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                byte[] readBuff=(byte[]) msg.obj;
                String tempMsg=new String(readBuff,0,msg.arg1);
                read_msg_box.setText(tempMsg);
                peerCoordinates=tempMsg;
                break;
            }
            return true;
        }
    });



//    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%CREATING FOLDER IN GOOGLE DRIVE%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

//  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%






    private void initialWork() {
        btnOnOff=(Button) findViewById(R.id.onOff);
        btnDiscover=(Button) findViewById(R.id.discover);
        btnSend=(Button) findViewById(R.id.sendButton);
        listView=(ListView) findViewById(R.id.peerListView);
        read_msg_box=(TextView) findViewById(R.id.readMsg);
        connectionStatus=(TextView) findViewById(R.id.connectionStatus);
        writeMsg=(EditText) findViewById(R.id.writeMsg);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        wifiManager= (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel=mManager.initialize(this, getMainLooper(),null);
        mReceiver=new WiFiDirectBroadcastReceiver(mManager,mChannel,this);
        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }

    WifiP2pManager.PeerListListener peerListListener=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers))
            {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray=new String[peerList.getDeviceList().size()];
                deviceArray=new WifiP2pDevice[peerList.getDeviceList().size()];
                int index=0;

                for(WifiP2pDevice device : peerList.getDeviceList())
                {
                    deviceNameArray[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;
                }

                ArrayAdapter<String> adapter= new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }

            if(peers.size()==0)
            {
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();

                return;
            }

        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress=wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                connectionStatus.setText("Connection Established");
                serverClass=new ServerClass();
                serverClass.start();

//                criticalAreaInfo=myCoordinates+"\n"+dRadius;
//                String message=criticalAreaInfo+eMessage;
//                sendReceive.write(message.getBytes());


            }else if(wifiP2pInfo.groupFormed)
            {
                connectionStatus.setText("Connection Established");
                clientClass=new ClientClass(groupOwnerAddress);
                clientClass.start();

//                criticalAreaInfo=myCoordinates+"\n"+dRadius;
//                String message=criticalAreaInfo+eMessage;
//                sendReceive.write(message.getBytes());
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
                sendReceive=new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while (socket!=null)
            {
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress){
            hostAdd=hostAddress.getHostAddress();
 //           connectionStatus.setText(hostAdd);
            socket=new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive=new SendReceive(socket);
                sendReceive.start();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //    Created a method to see if the external storage is writable and permission given ************************************************************
    private boolean isExternalStorageWritable() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State","Yes it is Writable");
            return true;
        }
        else{
            return false;
        }
    }


    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check==PackageManager.PERMISSION_GRANTED);
    }
    //    Created a method to see if the external storage is writable and permission given************************************************************


    private void exqListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("Turn On");
                }else{
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("Turn Off");

                }




            }


        });
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText("Discovery Failed");

                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device=deviceArray[i];
                WifiP2pConfig config=new WifiP2pConfig();
                config.deviceAddress=device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener(){//mManager.createGroup(mChannel, new WifiP2pManager.ActionListener()
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(),"Not Connected", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        //Sending the Text message when the btnSend is pressed  *******************************************************************************************************

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=writeMsg.getText().toString();
                sendReceive.write(msg.getBytes());
                String msg2= "astalavista";
//                if(myCoordinates==null)
//                {
//                    myCoordinates="51.587679 -0.230849";
//                }
//                else{
//                    myCoordinates=msg+" "+myCoordinates;
//
//                }
//                sendReceive.write(myCoordinates.getBytes());

            }
        });

        //**************************************************************************************************************************************************************

    }






}
