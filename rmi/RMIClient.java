package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

/**
 * RMIClient for the Paxos-based fault-tolerant Key-Value Store.
 * It randomly selects one of the available server ports and performs:
 * - 5 PUT operations
 * - 5 GET operations
 * - 5 DELETE operations
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

            // Perform 5 PUT operations using Paxos consensus.
            for (int i = 0; i < keys.length; i++) {
                String result = store.put(keys[i], values[i]);
                System.out.println("PUT " + keys[i] + ": " + result);
            }

            // Perform 5 GET operations (read-only, no consensus needed).
            for (String key : keys) {
                String value = store.get(key);
                System.out.println("GET " + key + ": " + value);
            }

            // Perform 5 DELETE operations using Paxos consensus.
            for (String key : keys) {
                String result = store.delete(key);
                System.out.println("DELETE " + key + ": " + result);
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
