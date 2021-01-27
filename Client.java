/**
 * @author Antoun Obied
 *
 * This class contains the functionality of the client side. It initiates a two-way handshake, reads an input data file,
 * and sends it to the server.
 */


import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Client implements Runnable{

    private static int packetDataSize = 20000; // The size of each packet

    private static String ip; // IP address to send data to
    private static byte[] fileData; // Data that will be sent
    private static int seqNum; // Packet sequence number
    private static String destPodAddr; // Final destination address

    public Client(String ip, byte[] fileData, String destPodAddr) {
        Client.ip = ip;
        Client.fileData = fileData;
        Client.destPodAddr = destPodAddr;
    }


    /**
     * This method initiates a two-way handshake using UDP. It creates a packet that contains a SYN flag set to 1,
     * and a sequence number set to 0 by default. Then, it waits for an ACK from the server side confirming the
     * use of that sequence number
     * @throws IOException
     */
    private void initHandshake() throws IOException {
        DatagramSocket socketSend = new DatagramSocket();
        DatagramSocket socketReceive = new DatagramSocket(12345);
        byte[] receive = new byte[14];
        DatagramPacket reply = new DatagramPacket(receive, receive.length);

        byte[] handshake = new byte[14];
        handshake[0] = 1; // Mark that this is a handshake
        handshake[1] = 0; // Initial sequence number is 0

        // Get localhost IP address and put it into handshake packet
        String[] thisIP = InetAddress.getLocalHost().toString().split("/");
        String localHost = thisIP[1];
        String[] localIP = localHost.split("\\.");
        handshake[2] = Operations.stringToByte(localIP[0]);
        handshake[3] = Operations.stringToByte(localIP[1]);
        handshake[4] = Operations.stringToByte(localIP[2]);
        handshake[5] = Operations.stringToByte(localIP[3]);

        // Put the final destination address into handshake packet
        String[] destIP = destPodAddr.split("\\.");
        handshake[6] = Operations.stringToByte(destIP[0]);
        handshake[7] = Operations.stringToByte(destIP[1]);
        handshake[8] = Operations.stringToByte(destIP[2]);
        handshake[9] = Operations.stringToByte(destIP[3]);

        // Put the length of the file being sent into the packet
        byte[] dataLength = Operations.intToByteArray(fileData.length);
        handshake[10] = Operations.intToByte(dataLength[0]);
        handshake[11] = Operations.intToByte(dataLength[1]);
        handshake[12] = Operations.intToByte(dataLength[2]);
        handshake[13] = Operations.intToByte(dataLength[3]);


        // Send the handshake packet
        DatagramPacket handshakePacket = new DatagramPacket(handshake, handshake.length, InetAddress.getByName(ip), 12345);
        socketSend.send(handshakePacket);
        System.out.println("Handshake initiated!");

        // Wait for a response from the server
        socketReceive.receive(reply);
        System.out.println("Reply received!");
        if (receive[0] == 1){
            seqNum = 0;
            System.out.println("Connection established!");
        }

        socketReceive.close();
        socketSend.close();
    }

    /**
     * Reads input file, then sends the data to the server
     * Byte 0: FIN flag
     * Bytes 1-4: Packet sequence number
     * Bytes: 5-end: Data
     * @throws IOException
     */
    private void readAndSendData() throws IOException {
        DatagramSocket socketSend = new DatagramSocket();
        DatagramSocket socketReceive = new DatagramSocket(54321);
        socketReceive.setSoTimeout(1000);

        // The total number of packets to be sent to the server
        int numberOfPackets = fileData.length / packetDataSize;

        System.out.println("Number of packets to send: " + numberOfPackets);

        // While loop sends all packets from starting sequence number, to the second last packet.
        // Last packet will generally be of a different length
        while (seqNum < numberOfPackets){
            byte[] toSend = new byte[packetDataSize + 5];
            toSend[0] = 0;
            byte[] sequence = Operations.intToByteArray(seqNum);
            System.arraycopy(sequence, 0, toSend, 1, 4);
            System.arraycopy(fileData, seqNum * packetDataSize, toSend, 5, packetDataSize);

            DatagramPacket dataPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getByName(ip), 54321);
            socketSend.send(dataPacket);

            System.out.println("Sent packet: " + seqNum);

            // Wait for an ACK from the server
            byte[] ACK = new byte[4];
            DatagramPacket ACKFromServer = new DatagramPacket(ACK, ACK.length);

            try {
                socketReceive.receive(ACKFromServer);
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout");
                continue;
            }

            // New sequence number is the one contained in the ACK packet that the server sends
            seqNum = Operations.byteArrayToInt(ACK);

            socketReceive.setSoTimeout(300);

        }

        // This block takes care of sending the last packet, as the size would be smaller
        byte[] toSend = new byte[5 + fileData.length - (seqNum * packetDataSize)];

        // Set the FIN flag to 1, indicating this is the last packet, and the connection should end
        toSend[0] = 1;

        System.out.println("Sent last packet!");
        byte[] sequence = Operations.intToByteArray(seqNum);
        System.arraycopy(sequence, 0, toSend, 1, 4);
        System.arraycopy(fileData, seqNum * packetDataSize, toSend, 5, (fileData.length - (seqNum * packetDataSize)));

        DatagramPacket dataPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getByName(ip), 54321);
        socketSend.send(dataPacket);


        socketReceive.close();
        socketSend.close();

    }

    public void run() {
        Client client = new Client(ip, fileData, destPodAddr);
        try {
            client.initHandshake();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            client.readAndSendData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
