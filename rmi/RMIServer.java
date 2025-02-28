package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            KeyValueStoreImpl store = new KeyValueStoreImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("KeyValueStore", store);
            System.out.println("RMI Server started on port 1099...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
