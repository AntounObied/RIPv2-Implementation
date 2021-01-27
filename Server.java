/**
 * @author Antoun Obied
 *
 * This class contains the functionality of the server side. It receives data from the client, and outputs it to
 * a user-specified file path.
 */


import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server implements Runnable{

    private static int fileDataLength;
    private static String receivedFrom; // IP address of the client
    private static String destPodAddr;
    private static final int packetDataSize = 20000; // Should be the same packet size as the ones the client is sending, or greater
    private static byte[] fullFileData; // Total file cannot be more than 2 GB
    private static int seqNum; //  Packet sequence number
    private static ArrayList<Pod> routingTable;


    public Server(ArrayList<Pod> routingTable){
        Server.routingTable = routingTable;
    }

    /**
     * This method waits for the initial handshake message from the client, then responds with an ACK
     * @throws IOException
     */
    private void waitForHandshake() throws IOException {
        DatagramSocket socketSend = new DatagramSocket();
        DatagramSocket socketReceive = new DatagramSocket(12345);
        byte[] ACK = new byte[1];
        ACK[0] = 1;


        byte[] receive = new byte[14];
        DatagramPacket reply = new DatagramPacket(receive, receive.length);
        System.out.println("Waiting for handshake...");
        socketReceive.receive(reply);
        System.out.println("Received handshake packet!");

        // Get the destination pod address, the ip of the client, and file length
        if (receive[0] == 1){
            seqNum = 0;
            destPodAddr = Operations.byteToString(receive[6]) + "." + Operations.byteToString(receive[7]) + "." +
                    Operations.byteToString(receive[8]) + "." + Operations.byteToString(receive[9]);
            receivedFrom = Operations.byteToString(receive[2]) + "." + Operations.byteToString(receive[3]) + "." +
                    Operations.byteToString(receive[4]) + "." + Operations.byteToString(receive[5]);
            byte[] dataLength = {receive[10], receive[11], receive[12], receive[13]};
            fileDataLength = Operations.byteArrayToInt(dataLength);
            DatagramPacket ack = new DatagramPacket(ACK, ACK.length, InetAddress.getByName(receivedFrom), 12345);
            socketSend.send(ack);
            System.out.println("ACKed back!");
        }

        socketReceive.close();
        socketSend.close();
    }

    /**
     * This method receives data packets from the client, and outputs them to a user-specified file path
     * @throws IOException
     */
    private void receiveAndACK() throws IOException {
        fullFileData = new byte[fileDataLength + packetDataSize];
        // FIN flag indicating end of connection is zero at the start
        int FIN = 0;
        DatagramSocket socketSend = new DatagramSocket();
        DatagramSocket socketReceive = new DatagramSocket(54321);

        // Socket has a set timeout in case a packet never arrives
        socketReceive.setSoTimeout(10000);

        // While loop runs until the client sends a packet with a FIN flag set to 1
        while (FIN == 0){
            byte[] dataReceived = new byte[packetDataSize + 5];
            DatagramPacket data = new DatagramPacket(dataReceived, dataReceived.length);

            // If a packet is dropped, the socket will wait for 200 ms, then send an ACK to the server indicating
            // which packet it has not received
            try {
                socketReceive.receive(data);
            } catch (SocketTimeoutException e){
                System.out.println("Timeout");
                byte[] ACK = Operations.intToByteArray(seqNum + 1);
                DatagramPacket ACKPacket = new DatagramPacket(ACK, ACK.length, InetAddress.getByName(receivedFrom), 54321);
                socketSend.send(ACKPacket);
                System.out.println("Sent ACK");
                continue;
            }

            FIN = dataReceived[0];

            // Read the sequence number of the packet that arrived
            byte[] sequenceNumber = new byte[4];
            System.arraycopy(dataReceived, 1, sequenceNumber, 0, 4);
            seqNum = Operations.byteArrayToInt(sequenceNumber);

            System.out.println("Received packet: " + seqNum);

            // Copy the data from the received packet to the byte array in the appropriate location
            System.arraycopy(dataReceived, 5, fullFileData, seqNum * packetDataSize, packetDataSize);

            // Send an ACK requesting the next sequence numbered packet
            byte[] ACK = Operations.intToByteArray(seqNum + 1);
            DatagramPacket ACKPacket = new DatagramPacket(ACK, ACK.length, InetAddress.getByName(receivedFrom), 54321);
            socketSend.send(ACKPacket);
            socketReceive.setSoTimeout(300);
        }
        socketReceive.close();
        socketSend.close();
    }

    public byte[] getData(){
        return fullFileData;
    }

    public void run(){
        Server server = new Server(routingTable);
        try {
            server.waitForHandshake();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            server.receiveAndACK();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String nextHop = null;

        for (Pod p : routingTable){
            if (p.getAddress().equals(destPodAddr)){
                nextHop = p.getNextHop();
            }
        }

        // Check if the destination address is the address of the current pod. If it is, write out file
        if (routingTable.get(0).getAddress().equals(destPodAddr)) {
            try {
                FileOutputStream fos = new FileOutputStream("output.jpg");
                fos.write(server.getData());
                fos.close();
                System.out.println("\nWrote out image!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // If not, start a client thread to send to the nexthop
        else{
            Thread client = new Thread(new Client(nextHop, server.getData(), destPodAddr));
            client.start();
        }
    }
}
