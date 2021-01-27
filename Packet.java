/**
 * @author Antoun Obied
 *
 * This class contains the method to create and parse RIPv2 packets
 */

import java.util.ArrayList;

public class Packet {

    public static final int command = 2;
    public static final int version = 2;
    public static final int zero = 0;
    private String sender;

    public Packet(String sender){
        this.sender = sender;
    }

    // Creates RIPv2 packet from the current routing table
    public static byte[] createRIPPacket(ArrayList<Pod> routingTable){
        byte[] array = new byte[(routingTable.size() * 20) + 4];
        int currentIndex = 0;
        array[currentIndex] = (byte) command;
        array[++currentIndex] = (byte) version;
        array[++currentIndex] = (byte) zero;
        array[++currentIndex] = (byte) zero;
        for (Pod current : routingTable){

            array[++currentIndex] = (byte) (current.getAddressFamily() >> 8);
            array[++currentIndex] = (byte) current.getAddressFamily();

            // Ignoring route tag
            array[++currentIndex] = (byte) 0;
            array[++currentIndex] = (byte) 0;

            String[] splitIPAddress = current.getAddress().split("\\.");
            for (String s : splitIPAddress){
                array[++currentIndex] = (byte) Integer.parseInt(s);
            }

            ++currentIndex;
            ++currentIndex;
            ++currentIndex;
            ++currentIndex;

            String[] splitNextHop = current.getNextHop().split("\\.");
            for (String s : splitNextHop){
                array[++currentIndex] = (byte) Integer.parseInt(s);
            }

            array[++currentIndex] = 0;
            array[++currentIndex] = 0;
            array[++currentIndex] = 0;
            array[++currentIndex] = (byte) current.getCost();

        }
        return array;
    }

    // Parses data from a received RIPv2 packet for the routing table to be updated
    public static ArrayList<Pod> parseRIPPacket(byte[] b){

        ArrayList<Pod> tempRoutingTable = new ArrayList<>();
        int currentIndex = 3;

        for (int i = 4; i < ((b.length - 4) / 20); i++) {
            int addressFamily = Operations.byteToInt(b[++currentIndex]) + Operations.byteToInt(b[++currentIndex]);

            // Ignoring route tag
            ++currentIndex;
            ++currentIndex;

            String IPAddress = "";
            for (int j = 0; j < 3; j++) {
                IPAddress += Operations.byteToString(b[++currentIndex]) + ".";
            }
            IPAddress += Operations.byteToString(b[++currentIndex]);

            // Ignoring subnet mask
            ++currentIndex;
            ++currentIndex;
            ++currentIndex;
            ++currentIndex;

            String nextHop = "";
            for (int j = 0; j < 3; j++) {
                nextHop += Operations.byteToString(b[++currentIndex]) + ".";
            }
            nextHop += Operations.byteToString(b[++currentIndex]);

            ++currentIndex;
            ++currentIndex;
            ++currentIndex;
            int cost = Operations.byteToInt(b[++currentIndex]);

            String[] splitIP = IPAddress.split("\\.");
            int podID = Integer.parseInt(splitIP[2]);

            Pod current = new Pod(podID);
            current.setAddressFamily(addressFamily);
            current.setCost(cost);
            current.setNextHop(nextHop);
            current.setAddress(IPAddress);
            if (current.getID() > 0) {
                tempRoutingTable.add(current);
            }
        }
        return tempRoutingTable;
    }
}
