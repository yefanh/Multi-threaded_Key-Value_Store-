package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            KeyValueStoreInterface store = (KeyValueStoreInterface) registry.lookup("KeyValueStore");

            System.out.println("Connected to RMI Server...");

            // Prepopulate Key-Value Store
            String[] keys = {"key1", "key2", "key3", "key4", "key5"};
            String[] values = {"value1", "value2", "value3", "value4", "value5"};

            for (int i = 0; i < keys.length; i++) {
                System.out.println("PUT " + keys[i] + ": " + store.put(keys[i], values[i]));
            }

            for (String key : keys) {
                System.out.println("GET " + key + ": " + store.get(key));
            }

            for (String key : keys) {
                System.out.println("DELETE " + key + ": " + store.delete(key));
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
