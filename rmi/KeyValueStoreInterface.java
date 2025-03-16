package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for a distributed Key-Value Store.
 * 
 * This interface defines the contract for a remote key-value store that allows 
 * clients to perform distributed operations using Java RMI.
 * 
 * The store supports basic CRUD operations: PUT, GET, and DELETE,
 * plus prepare/commit methods for 2PC.
 */
public interface KeyValueStoreInterface extends Remote {

    /**
     * Stores a key-value pair in the remote Key-Value store.
     * If the key already exists, its value is updated.
     * 
     * @param key   The unique identifier for the value.
     * @param value The data to be stored.
     * @return "OK" if the operation is successful; otherwise "ABORT".
     * @throws RemoteException If an RMI communication error occurs.
     */
    String put(String key, String value) throws RemoteException;

    /**
     * Retrieves the value associated with a given key from the remote Key-Value store.
     * If the key does not exist, returns "NOT FOUND".
     * 
     * @param key The key to retrieve.
     * @return The value associated with the key, or "NOT FOUND" if not found.
     * @throws RemoteException If an RMI communication error occurs.
     */
    String get(String key) throws RemoteException;

    /**
     * Removes a key-value pair from the remote Key-Value store.
     * If the key does not exist, returns "NOT FOUND".
     * 
     * @param key The key to delete.
     * @return "OK" if the key was successfully removed, "NOT FOUND" otherwise.
     * @throws RemoteException If an RMI communication error occurs.
     */
    String delete(String key) throws RemoteException;

    // ===============  2PC 协议的远程方法  ===============
    String preparePut(String key, String value) throws RemoteException;
    String commitPut(String key, String value) throws RemoteException;
    String prepareDelete(String key) throws RemoteException;
    String commitDelete(String key) throws RemoteException;
}
