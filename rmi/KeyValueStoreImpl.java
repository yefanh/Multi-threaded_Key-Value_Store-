package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of the distributed Key-Value Store interface,
 * including 2PC (two-phase commit) logic for PUT and DELETE.
 */
public class KeyValueStoreImpl extends UnicastRemoteObject implements KeyValueStoreInterface {

    // 存储本地的 key-value 数据
    private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    // 读写锁，确保多线程下的安全访问
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 保存对其他副本的引用
    private List<KeyValueStoreInterface> replicas;

    /**
     * Constructor
     *
     * @param replicas 其他副本的存根列表
     */
    public KeyValueStoreImpl(List<KeyValueStoreInterface> replicas) throws RemoteException {
        super();
        this.replicas = replicas;
    }

    /**
     * 可以在服务器启动后动态设置或更新副本列表
     */
    public void setReplicas(List<KeyValueStoreInterface> replicas) {
        this.replicas = replicas;
    }

    // ================ 1) PUT 操作 ================
    @Override
    public String put(String key, String value) throws RemoteException {
        // 第一阶段：prepare
        for (KeyValueStoreInterface replica : replicas) {
            if (!replica.preparePut(key, value).equals("YES")) {
                return "ABORT";
            }
        }
        // 第二阶段：commit
        for (KeyValueStoreInterface replica : replicas) {
            replica.commitPut(key, value);
        }
        // 自身也进行提交
        lock.writeLock().lock();
        try {
            store.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
        return "OK";
    }

    // ================ 2) GET 操作 ================
    @Override
    public String get(String key) throws RemoteException {
        lock.readLock().lock();
        try {
            return store.getOrDefault(key, "NOT FOUND");
        } finally {
            lock.readLock().unlock();
        }
    }

    // ================ 3) DELETE 操作 ================
    @Override
    public String delete(String key) throws RemoteException {
        // 第一阶段：prepare
        for (KeyValueStoreInterface replica : replicas) {
            if (!replica.prepareDelete(key).equals("YES")) {
                return "ABORT";
            }
        }
        // 第二阶段：commit
        for (KeyValueStoreInterface replica : replicas) {
            replica.commitDelete(key);
        }
        // 自身也进行提交
        lock.writeLock().lock();
        try {
            return store.remove(key) != null ? "OK" : "NOT FOUND";
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ============== 2PC 的 prepare/commit ==============
    @Override
    public String preparePut(String key, String value) throws RemoteException {
        // 简化处理：默认都返回 "YES" 表示可提交
        return "YES";
    }

    @Override
    public String commitPut(String key, String value) throws RemoteException {
        lock.writeLock().lock();
        try {
            store.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
        return "ACK";
    }

    @Override
    public String prepareDelete(String key) throws RemoteException {
        return "YES";
    }

    @Override
    public String commitDelete(String key) throws RemoteException {
        lock.writeLock().lock();
        try {
            store.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
        return "ACK";
    }
}
