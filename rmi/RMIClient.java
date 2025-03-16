package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

/**
 * RMI Client that randomly picks one of the 5 known ports,
 * connects to a KeyValueStore, then does 5 PUTs, 5 GETs, and 5 DELETEs.
 */
public class RMIClient {
    public static void main(String[] args) {
        try {
            int[] ports = {1099, 1100, 1101, 1102, 1103};
            int port = ports[new Random().nextInt(ports.length)];

            Registry registry = LocateRegistry.getRegistry("localhost", port);
            KeyValueStoreInterface store = (KeyValueStoreInterface) registry.lookup("KeyValueStore");

            System.out.println("Connected to RMI Server on port " + port);

            String[] keys = {"key1", "key2", "key3", "key4", "key5"};
            String[] values = {"value1", "value2", "value3", "value4", "value5"};

            // 5 PUTs
            for (int i = 0; i < keys.length; i++) {
                System.out.println("PUT " + keys[i] + ": " + store.put(keys[i], values[i]));
            }

            // 5 GETs
            for (String key : keys) {
                System.out.println("GET " + key + ": " + store.get(key));
            }

            // 5 DELETEs
            for (String key : keys) {
                System.out.println("DELETE " + key + ": " + store.delete(key));
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
