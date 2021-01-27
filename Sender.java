/**
 * @author Antoun Obied
 *
 * This class contains the methods needed to broadcast each Pod's routing table
 */

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Sender implements Runnable{

    int port;
    String multicastAddress;
    int nodeNum;
    private ArrayList<Pod> routingTable;

    public Sender(int port, String multicastAddress, int nodeNum, ArrayList<Pod> routingTable){
        this.port = port;
        this.multicastAddress = multicastAddress;
        this.nodeNum = nodeNum;
        this.routingTable = routingTable;
    }

    // Broadcasts the current routing table in the format of a RIPv2 packet via UDP
    public void sendMessage(ArrayList<Pod> table) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress multicastGroup = InetAddress.getByName(multicastAddress);
        byte[] b = Packet.createRIPPacket(table);
        DatagramPacket packet = new DatagramPacket(b, b.length, multicastGroup, port);

        socket.send(packet);
        socket.close();
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 90000) {
            try {
                sendMessage(routingTable);
                Thread.sleep(5000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done transmitting routing table!");
    }
}
