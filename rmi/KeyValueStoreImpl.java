package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KeyValueStoreImpl extends UnicastRemoteObject implements KeyValueStoreInterface {
    private ConcurrentHashMap<String, String> store;
    private ReentrantReadWriteLock lock;

    public KeyValueStoreImpl() throws RemoteException {
        store = new ConcurrentHashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public String put(String key, String value) throws RemoteException {
        lock.writeLock().lock();
        try {
            store.put(key, value);
            return "OK";
        } finally {
            lock.writeLock().unlock();
        }
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
        lock.writeLock().lock();
        try {
            return store.remove(key) != null ? "OK" : "NOT FOUND";
        } finally {
            lock.writeLock().unlock();
        }
    }
}
