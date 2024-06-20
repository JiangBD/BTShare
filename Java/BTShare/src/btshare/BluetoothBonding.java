/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package btshare;

import com.intel.bluetooth.RemoteDeviceHelper;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;

public class BluetoothBonding {

    private static final Object lock = new Object();
    private static RemoteDevice discoveredDevice;

    public static void main(String[] args) {
        try {
            String remoteAddress = "001122334455"; // Replace with actual address
            boolean success = BluetoothBonding.discoverAndBondNewDevice(remoteAddress);
            System.out.println(success ? "Pairing successful" : "Pairing failed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean discoverAndBondNewDevice(String remoteAddress) throws BluetoothStateException, IOException, InterruptedException {
        // Get the local Bluetooth device
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Local Device Address: " + localDevice.getBluetoothAddress());
        System.out.println("Local Device Name: " + localDevice.getFriendlyName());

        // Discover the specific device
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        agent.startInquiry(DiscoveryAgent.GIAC, new DeviceDiscoveryListener(remoteAddress));

        // Wait for device discovery to complete
        synchronized (lock) {
            lock.wait();
        }

        if (discoveredDevice != null) {
            // Initiate pairing
            boolean paired = RemoteDeviceHelper.authenticate(discoveredDevice,"016723");
            if (paired) {
                // Discover services on the paired device
                String serviceUrl = discoverService(agent, discoveredDevice);
                if (serviceUrl != null) {
                    // Connect to the service to trigger pairing
                    StreamConnection connection = (StreamConnection) Connector.open(serviceUrl);
                    System.out.println("Connected to the service at: " + serviceUrl);

                    // Perform some I/O to complete the connection and trigger pairing
                    connection.openDataOutputStream().write("Hello".getBytes());
                    connection.close();
                    return true;
                } else {
                    System.out.println("Service not found.");
                }
            } else {
                System.out.println("Pairing failed.");
            }
        } else {
            System.out.println("Device with address " + remoteAddress + " not found.");
        }
        return false;
    }

    private static String discoverService(DiscoveryAgent agent, RemoteDevice device) throws BluetoothStateException, InterruptedException {
        UUID[] searchUuidSet = new UUID[]{new UUID(0x1101)}; // Serial Port Profile (SPP)
        int[] attrIDs = new int[]{0x0100}; // Service name

        ServiceDiscoveryListener listener = new ServiceDiscoveryListener();
        agent.searchServices(attrIDs, searchUuidSet, device, listener);

        // Wait for service discovery to complete
        synchronized (listener) {
            listener.wait();
        }

        return listener.getServiceUrl();
    }

    // Inner class to handle device discovery
    static class DeviceDiscoveryListener implements DiscoveryListener {
        private String targetAddress;

        DeviceDiscoveryListener(String remoteAddress) {
            this.targetAddress = remoteAddress;
        }

        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            try {
                if (btDevice.getBluetoothAddress().equals(targetAddress)) {
                    System.out.println("Target device discovered: " + btDevice.getBluetoothAddress());
                    discoveredDevice = btDevice;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void inquiryCompleted(int discType) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {}
    }

    // Inner class to handle service discovery
    static class ServiceDiscoveryListener implements DiscoveryListener {
        private String serviceUrl = null;

        public String getServiceUrl() {
            return serviceUrl;
        }

        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {}

        @Override
        public void inquiryCompleted(int discType) {}

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            for (ServiceRecord record : servRecord) {
                serviceUrl = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                if (serviceUrl != null) {
                    break;
                }
            }
        }
    }
}



