package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for a distributed, fault-tolerant Key-Value Store 
 * that uses Paxos consensus for update operations.
 */
public interface KeyValueStoreInterface extends Remote {

    /**
     * Initiates a put operation using Paxos consensus.
     *
     * @param key   The key to store.
     * @param value The value to associate with the key.
     * @return "OK" if consensus is reached and the key-value pair is stored,
     *         "FAIL" if consensus fails.
     * @throws RemoteException if an RMI error occurs.
     */
    String put(String key, String value) throws RemoteException;

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key The key to retrieve.
     * @return The value if found, or "NOT FOUND" if the key does not exist.
     * @throws RemoteException if an RMI error occurs.
     */
    String get(String key) throws RemoteException;

    /**
     * Initiates a delete operation using Paxos consensus.
     *
     * @param key The key to delete.
     * @return "OK" if consensus is reached and the key is removed,
     *         "NOT FOUND" if the key does not exist,
     *         "FAIL" if consensus fails.
     * @throws RemoteException if an RMI error occurs.
     */
    String delete(String key) throws RemoteException;

    /**
     * Paxos Phase 1: Prepare.
     *
     * @param proposalId     Unique identifier for this proposal.
     * @param operation      The operation being proposed ("PUT" or "DELETE").
     * @param key            The key for the operation.
     * @param value          The value for PUT operations (null for DELETE).
     * @param proposalNumber The proposal number (used for ordering).
     * @return "PROMISE" if the acceptor promises to not accept a lower proposal,
     *         "REJECT" if the proposal number is too low,
     *         "FAIL" if simulated failure occurs.
     * @throws RemoteException if an RMI error occurs.
     */
    String paxosPrepare(String proposalId, String operation, String key, String value, long proposalNumber) throws RemoteException;

    /**
     * Paxos Phase 2: Accept.
     *
     * @param proposalId     Unique identifier for this proposal.
     * @param operation      The operation being proposed.
     * @param key            The key for the operation.
     * @param value          The value for PUT operations (null for DELETE).
     * @param proposalNumber The proposal number.
     * @return "ACCEPTED" if the proposal is accepted,
     *         "REJECT" if not,
     *         "FAIL" if simulated failure occurs.
     * @throws RemoteException if an RMI error occurs.
     */
    String paxosAccept(String proposalId, String operation, String key, String value, long proposalNumber) throws RemoteException;

    /**
     * Paxos Commit: Notifies the acceptor to commit the proposed operation.
     *
     * @param proposalId Unique identifier for the proposal.
     * @param operation  The operation that is committed.
     * @param key        The key for the operation.
     * @param value      The value for PUT operations (null for DELETE).
     * @throws RemoteException if an RMI error occurs.
     */
    void paxosCommit(String proposalId, String operation, String key, String value) throws RemoteException;
}
