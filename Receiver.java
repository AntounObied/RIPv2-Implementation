/**
 * @author Antoun Obied
 *
 * This class is responsible for receiving RIPv2 packets, parsing the data of the routing table inside, and
 * update the current Pod's routing table if needed.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Receiver implements Runnable{

    private int port;
    private String multicastAddress;
    private ArrayList<Pod> routingTable;
    private ArrayList<Pod> tempRoutingTable;
    private Map<String, Long> lastTransmissionTimes = new HashMap<>(); // List of last time a transmission was received from each node
    private static String originIP;


    public Receiver(int port, String multicastAddress, ArrayList<Pod> routingTable){
        this.port = port;
        this.multicastAddress = multicastAddress;
        this.routingTable = routingTable;
    }

    // This method receives a multicast from Pods at the next hop
    private void receiveMessage() throws IOException {
        byte[] buffer = new byte[504];

        MulticastSocket mSocket = new MulticastSocket(port);
        InetAddress multicastGroup = InetAddress.getByName(multicastAddress);
        mSocket.joinGroup(multicastGroup);

        // Receives packets, ignores packets if it receives it from itself
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        mSocket.receive(packet);
        if (!(packet.getAddress().toString()).equals((InetAddress.getLocalHost().getHostAddress()).trim().replace("/", ""))) {
            tempRoutingTable = Packet.parseRIPPacket(packet.getData());

            originIP = packet.getAddress().toString().replace("/", "");

            lastTransmissionTimes.put(packet.getAddress().toString().replace("/", ""), System.currentTimeMillis());
        }
    }


    // Adds entries to the routing table, and updates cost if there exists a shorter path
    public void updateRoutingTable(ArrayList<Pod> useToUpdate){
        for (Pod p : useToUpdate){
            boolean addp = true;
            int i;

            // Do not add the pod if it already exists in the table
            for (i = 0; i < routingTable.size(); i++) {
                if (routingTable.get(i).getID() == p.getID()) {
                    addp = false;
                    break; //keep the index i when the match happened
                }
            }
            // Add the pod if it does not exist
            if (addp){ //add p to the routing table
                int cost = p.getCost() + 1;
                p.setCost(cost);
                p.setNextHop(originIP);
                routingTable.add(p);


            } else if (routingTable.get(i).getCost() > p.getCost() + 1) { //update cost if needed
                    routingTable.get(i).setCost(p.getCost() + 1);
                    routingTable.get(i).setNextHop(originIP);
            }

            }
        }

    // Prints the current pod's routing table
    public void printRoutingTable(ArrayList<Pod> routingTable){
        for (Pod p : routingTable){
            System.out.println("" + p.getAddress() + "\t" + p.getNextHop() + "\t" + p.getCost());
        }
    }

    public ArrayList<Pod> getRoutingTable(){
        return routingTable;
    }

    public void run() {
        try {
            while (true) {
                receiveMessage();
                updateRoutingTable(tempRoutingTable);
                System.out.println("\nAddress\t\tNextHop\t\tCost");
                System.out.println("====================================");
                printRoutingTable(routingTable);

                // If more than 10 seconds pass without receiving a transmission of a nod, update cost to unreachable
                for (String address : lastTransmissionTimes.keySet()){
                    if ((System.currentTimeMillis() - lastTransmissionTimes.get(address)) > 10000){
                        for (Pod p : routingTable){
                            if (p.getNextHop().equals(address)){
                                p.setCost(16);
                                p.setNextHop(originIP);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
