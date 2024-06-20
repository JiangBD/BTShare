
package btshare;
import javax.bluetooth.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class BluetoothServiceChannelFinder {
    private static LocalDevice localDevice;
    private static DiscoveryAgent agent;

    public BluetoothServiceChannelFinder() throws BluetoothStateException {
        localDevice = LocalDevice.getLocalDevice();
        agent = localDevice.getDiscoveryAgent();
    }

    public int findServiceChannel(UUID uuid, String remoteAddress) throws InterruptedException, BluetoothStateException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger channelNumber = new AtomicInteger(-1);

        // Convert java.util.UUID to javax.bluetooth.UUID
        javax.bluetooth.UUID bluetoothUUID = new javax.bluetooth.UUID(uuid.toString().replace("-",""), false);

        RemoteDevice[] remoteDevices = agent.retrieveDevices(DiscoveryAgent.CACHED);
        RemoteDevice remoteDevice = null;
        if (remoteDevices != null) {
            for (RemoteDevice device : remoteDevices) {
                if (device.getBluetoothAddress().equals(remoteAddress.replace(":", ""))) {
                    remoteDevice = device;
                    break;
                }  
            }
        }

        if (remoteDevice == null) {
            System.err.println("Device with address " + remoteAddress + " not found.");
            return -1;
        }

        javax.bluetooth.UUID[] searchUuidSet = new javax.bluetooth.UUID[]{bluetoothUUID};
        int[] attrIDs = new int[]{0x0100}; // Service name

        agent.searchServices(attrIDs, searchUuidSet, remoteDevice, new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                for (ServiceRecord record : servRecord) {
                    String url = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url != null) {
                        int channel = extractChannelFromUrl(url);
                        channelNumber.set(channel);
                    }
                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                latch.countDown();
            }

            @Override
            public void inquiryCompleted(int discType) {
            }
        });

        latch.await(); // Wait for the service search to complete
        return channelNumber.get();
    }

    public String generateServiceURL(UUID uuid, String remoteAddress, int channel) {
        String baseServiceURL = "btspp://" + remoteAddress + ":" + channel;
        return baseServiceURL + ";authenticate=false;encrypt=false;master=false";
    }

    private int extractChannelFromUrl(String url) {
        // Example URL: btspp://001122334455:1;authenticate=false;encrypt=false;master=false
        int colonIndex = url.lastIndexOf(':');
        int semicolonIndex = url.indexOf(';', colonIndex);
        if (semicolonIndex == -1) semicolonIndex = url.length();
        return Integer.parseInt(url.substring(colonIndex + 1, semicolonIndex));
    }
}
