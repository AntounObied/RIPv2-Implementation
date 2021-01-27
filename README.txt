README

To run the program:

1. Build the program using docker
2. Start a new network if needed
3. Start each individual node with a unique node number as the only command line argument, except if it is the main node
4. DO NOT START THE MAIN NODE FIRST. Any node that sends should be started at the end
5. Nodes that will be sending take 3 command line arguments: node number, destination pod address (not IP address of the container they are in), and the name of the file to be sent
6. Each pod will run for 100 seconds, transmitting their routing table to figure out the layout of the network, then will start a server thread to wait for handshake. If it a sending pod, it will start a client thread to send to the next hop.

