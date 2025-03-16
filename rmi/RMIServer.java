package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * RMI Server that starts a local registry, binds its KeyValueStore,
 * and attempts to connect to other replicas based on the ports given.
 */
public class RMIServer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java rmi.RMIServer <port> <commaSeparatedReplicaPorts>");
            System.exit(1);
        }

        try {
            // retrieve the port and replica ports from command line arguments
            int port = Integer.parseInt(args[0]);
            String[] replicaPorts = args[1].split(",");

            // create a local registry on the specified port
            Registry localRegistry = LocateRegistry.createRegistry(port);

            // create a new KeyValueStore instance
            KeyValueStoreImpl store = new KeyValueStoreImpl(new ArrayList<>());

            // bind the KeyValueStore instance to the local registry
            localRegistry.rebind("KeyValueStore", store);

            // attempt to connect to other replicas
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
            // set the replicas in the KeyValueStore instance
            store.setReplicas(replicas);

            System.out.println("RMI Server started on port " + port + ", known replicas: " + replicas.size());
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
