package ssaurel.com.testmp2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class ChooseDeviceActivity extends AppCompatActivity {
    private ListView listView;
    public static BluetoothDevice device;
    private ArrayList<BluetoothDevice> devices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.paired_list);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<String> deviceNames = new ArrayList<>();
        devices = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices){
                devices.add(device);
                deviceNames.add(device.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChooseDeviceActivity.this,android.R.layout.simple_list_item_1,deviceNames);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    device = devices.get(position);
                    Intent intent = new Intent(ChooseDeviceActivity.this,MainActivity.class);
                    startActivity(intent);
                    //finish();
                }
            });

        }


    }

}
