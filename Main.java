/**
* @author Antoun Obied
*
* This main class instantiates a Pod, and starts a sender and receiver thread to broadcast and receive data.
*/


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

class Main {

    private static byte[] data;

    public static ArrayList<Pod> routingTable = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length>0) {

            // Create a Pod, add itself to a routing table with cost 0
            int nodeNum = Integer.parseInt(args[0]);
            Pod self = new Pod(nodeNum);
            InetAddress localhost = InetAddress.getLocalHost();
            String address = (localhost.getHostAddress()).trim().replace("/", "");
            self.setCost(0);
            self.setNextHop(address);
            self.setAddress(Operations.getSenderAddress(nodeNum));
            self.setAddressFamily(2);
            routingTable.add(self);


            // Start receiver thread
            System.out.println("Starting receiver thread");
            Thread receiver = new Thread(new Receiver(8080,"230.230.230.230", routingTable));
            receiver.start();


            // Start sender thread
            System.out.println("Starting sender thread");
            Thread sender=new Thread(new Sender(8080,"230.230.230.230", nodeNum, routingTable));
            sender.start();

        }
        else {
            System.out.println("No input args! Must specify Node Number!");
        }


        Thread.sleep(100000); // Waits for 100 seconds while the nodes figure out the network layout

        // If not sending, start a server thread
        if (args.length < 3) {
            Thread server = new Thread(new Server(routingTable));
            server.start();
        }



        if (args.length > 1){
            Thread.sleep(5000);

            // Read file into a byte array
            data = Files.readAllBytes(Paths.get(args[2]));

            String ip = args[1];
            String nextHop = null;

            // Get next hop for the final destination
            for (Pod p : routingTable){
                if (p.getAddress().equals(ip)){
                    nextHop = p.getNextHop();
                }
            }


            System.out.println("\nStarting client thread");
            Thread client = new Thread(new Client(nextHop, data, ip));
            client.start();
            System.out.println("\nClient thread started");
        }
    }
}