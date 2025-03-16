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

    // Store local key-value data
    private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    // Read and write locks to ensure secure access under multi-threads
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // List of replicas (other nodes) for 2PC
    private List<KeyValueStoreInterface> replicas;

    /**
     * Constructor
     *
     * @param replicas List of other replicas for 2PC
     */
    public KeyValueStoreImpl(List<KeyValueStoreInterface> replicas) throws RemoteException {
        super();
        this.replicas = replicas;
    }

    /**
     * Set the list of replicas for this store.
     *
     * @param replicas List of other replicas for 2PC
     */
    public void setReplicas(List<KeyValueStoreInterface> replicas) {
        this.replicas = replicas;
    }

    @Override
    public String put(String key, String value) throws RemoteException {
        // first phase: prepare
        for (KeyValueStoreInterface replica : replicas) {
            if (!replica.preparePut(key, value).equals("YES")) {
                return "ABORT";
            }
        }
        // second phase: commit
        for (KeyValueStoreInterface replica : replicas) {
            replica.commitPut(key, value);
        }
        // commit to local store
        lock.writeLock().lock();
        try {
            store.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
        return "OK";
    }

    @Override
    public String get(String key) throws RemoteException {
        lock.readLock().lock();
        try {
            return store.getOrDefault(key, "NOT FOUND");
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String delete(String key) throws RemoteException {
        // first phase: prepare
        for (KeyValueStoreInterface replica : replicas) {
            if (!replica.prepareDelete(key).equals("YES")) {
                return "ABORT";
            }
        }
        // second phase: commit
        for (KeyValueStoreInterface replica : replicas) {
            replica.commitDelete(key);
        }
        // commit to local store
        lock.writeLock().lock();
        try {
            return store.remove(key) != null ? "OK" : "NOT FOUND";
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String preparePut(String key, String value) throws RemoteException {
        // All return "YES" by default to indicate submission
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
