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
            // 1) 解析命令行参数
            int port = Integer.parseInt(args[0]);
            String[] replicaPorts = args[1].split(",");

            // 2) 在本地创建 RMI 注册表
            Registry localRegistry = LocateRegistry.createRegistry(port);

            // 3) 先创建一个 store，暂时 replicas 为空
            KeyValueStoreImpl store = new KeyValueStoreImpl(new ArrayList<>());

            // 4) 在本地注册表里绑定
            localRegistry.rebind("KeyValueStore", store);

            // 5) 尝试连接其它副本
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
            // 6) 把获取到的副本存根加入到 store
            store.setReplicas(replicas);

            System.out.println("RMI Server started on port " + port + ", known replicas: " + replicas.size());
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
