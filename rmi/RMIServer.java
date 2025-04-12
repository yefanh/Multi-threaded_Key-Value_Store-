package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * RMIServer starts an RMI registry, instantiates a Paxos-based Key-Value Store,
 * and attempts to connect to other replica servers.
 *
 * Usage: java rmi.RMIServer <port> <commaSeparatedReplicaPorts>
 */
public class RMIServer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java rmi.RMIServer <port> <commaSeparatedReplicaPorts>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            String[] replicaPorts = args[1].split(",");

            // Create a local RMI registry on the specified port.
            Registry localRegistry = LocateRegistry.createRegistry(port);

            // Instantiate the KeyValueStore implementation with an empty replica list initially.
            KeyValueStoreImpl store = new KeyValueStoreImpl(new ArrayList<>());

            // Bind the store instance to the registry.
            localRegistry.rebind("KeyValueStore", store);

            // Connect to other replicas.
            List<KeyValueStoreInterface> replicas = new ArrayList<>();
            for (String rp : replicaPorts) {
                try {
                    int rPort = Integer.parseInt(rp.trim());
                    Registry registry = LocateRegistry.getRegistry("localhost", rPort);
                    KeyValueStoreInterface stub = (KeyValueStoreInterface) registry.lookup("KeyValueStore");
                    replicas.add(stub);
                } catch (Exception e) {
                    System.err.println("Error connecting to replica on port " + rp + ": " + e.getMessage());
                }
            }
            // Update the store’s replica list.
            store.setReplicas(replicas);

            System.out.println("RMI Server started on port " + port +
                               ". Connected to " + replicas.size() + " replicas.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
