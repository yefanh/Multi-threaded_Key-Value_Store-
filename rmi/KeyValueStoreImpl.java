package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of a fault-tolerant distributed Key-Value Store using Paxos consensus.
 * This class plays all Paxos roles (Proposer, Acceptor, Learner) and includes simulated 
 * random failure (for acceptors) to demonstrate fault tolerance.
 */
public class KeyValueStoreImpl extends UnicastRemoteObject implements KeyValueStoreInterface {

    // Local thread-safe key-value store
    private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    // Read/write lock for safe concurrent access
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // List of replicas (other KeyValueStoreInterface objects) for Paxos messaging.
    private List<KeyValueStoreInterface> replicas;

    // Map to hold Paxos instance state for each proposal (keyed by proposalId)
    private ConcurrentHashMap<String, PaxosInstance> paxosInstances = new ConcurrentHashMap<>();

    // Atomic counter for unique proposal numbering
    private static AtomicLong proposalCounter = new AtomicLong(0);

    // Random instance to simulate random acceptor failures.
    private Random random = new Random();

    /**
     * Inner class to store Paxos instance state.
     */
    private class PaxosInstance {
        long promisedProposalNumber = 0;
        long acceptedProposalNumber = 0;
        // For a more advanced implementation, accepted operation details can be stored.
        String acceptedOperation = null;
        String acceptedValue = null;
    }

    /**
     * Constructor.
     *
     * @param replicas List of other replicas (KeyValueStoreInterface) for Paxos communication.
     * @throws RemoteException if an RMI error occurs.
     */
    public KeyValueStoreImpl(List<KeyValueStoreInterface> replicas) throws RemoteException {
        super();
        this.replicas = replicas;
    }

    /**
     * Set or update the list of replica servers.
     *
     * @param replicas List of other KeyValueStoreInterface instances.
     */
    public void setReplicas(List<KeyValueStoreInterface> replicas) {
        this.replicas = replicas;
    }

    /**
     * Proposes an update operation (PUT or DELETE) using Paxos consensus.
     * This method runs the three phases: Prepare, Accept, and Commit.
     *
     * @param operation "PUT" or "DELETE"
     * @param key       The key to operate on.
     * @param value     The value for a PUT operation (null for DELETE).
     * @return "OK" if consensus is reached and the operation is committed,
     *         "FAIL" otherwise.
     * @throws RemoteException if an RMI error occurs.
     */
    private String paxosPropose(String operation, String key, String value) throws RemoteException {
        // Generate a unique proposal ID and a proposal number.
        String proposalId = "proposal-" + System.currentTimeMillis() + "-" + proposalCounter.incrementAndGet();
        long proposalNumber = System.currentTimeMillis();  // using current time as proposal number

        int totalNodes = replicas.size() + 1; // total nodes (replicas plus self)
        int majority = (totalNodes / 2) + 1;

        int promiseCount = 0;
        // Phase 1: Prepare
        // Send paxosPrepare to self.
        String localResponse = this.paxosPrepare(proposalId, operation, key, value, proposalNumber);
        if ("PROMISE".equals(localResponse)) {
            promiseCount++;
        }
        // Send to all replicas.
        for (KeyValueStoreInterface replica : replicas) {
            try {
                String resp = replica.paxosPrepare(proposalId, operation, key, value, proposalNumber);
                if ("PROMISE".equals(resp)) {
                    promiseCount++;
                }
            } catch (Exception e) {
                // Communication failure or simulated failure.
                System.err.println("paxosPrepare failed on replica: " + e.getMessage());
            }
        }
        if (promiseCount < majority) {
            System.err.println("Paxos Prepare phase failed: " + promiseCount + "/" + totalNodes + " promises");
            return "FAIL";
        }

        int acceptCount = 0;
        // Phase 2: Accept
        String localAcceptResponse = this.paxosAccept(proposalId, operation, key, value, proposalNumber);
        if ("ACCEPTED".equals(localAcceptResponse)) {
            acceptCount++;
        }
        for (KeyValueStoreInterface replica : replicas) {
            try {
                String resp = replica.paxosAccept(proposalId, operation, key, value, proposalNumber);
                if ("ACCEPTED".equals(resp)) {
                    acceptCount++;
                }
            } catch (Exception e) {
                System.err.println("paxosAccept failed on replica: " + e.getMessage());
            }
        }
        if (acceptCount < majority) {
            System.err.println("Paxos Accept phase failed: " + acceptCount + "/" + totalNodes + " accepts");
            return "FAIL";
        }

        // Phase 3: Commit
        // Commit locally and notify replicas.
        this.paxosCommit(proposalId, operation, key, value);
        for (KeyValueStoreInterface replica : replicas) {
            try {
                replica.paxosCommit(proposalId, operation, key, value);
            } catch (Exception e) {
                System.err.println("paxosCommit failed on replica: " + e.getMessage());
            }
        }

        // Finally, perform the operation in the local store.
        lock.writeLock().lock();
        try {
            if ("PUT".equals(operation)) {
                store.put(key, value);
            } else if ("DELETE".equals(operation)) {
                store.remove(key);
            }
        } finally {
            lock.writeLock().unlock();
        }
        return "OK";
    }

    /**
     * Remote method for PUT.
     * Delegates to paxosPropose to perform a Paxos-based update.
     *
     * @param key   The key to store.
     * @param value The value to associate with the key.
     * @return "OK" if successful, "FAIL" otherwise.
     * @throws RemoteException if an RMI error occurs.
     */
    @Override
    public String put(String key, String value) throws RemoteException {
        return paxosPropose("PUT", key, value);
    }

    /**
     * Remote method for GET.
     * GET operations are local and do not require consensus.
     *
     * @param key The key to retrieve.
     * @return The corresponding value or "NOT FOUND".
     * @throws RemoteException if an RMI error occurs.
     */
    @Override
    public String get(String key) throws RemoteException {
        lock.readLock().lock();
        try {
            return store.getOrDefault(key, "NOT FOUND");
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Remote method for DELETE.
     * Delegates to paxosPropose to perform a Paxos-based deletion.
     *
     * @param key The key to delete.
     * @return "OK" if successful, "FAIL" if consensus fails or "NOT FOUND" if key absent.
     * @throws RemoteException if an RMI error occurs.
     */
    @Override
    public String delete(String key) throws RemoteException {
        return paxosPropose("DELETE", key, null);
    }

    /**
     * Paxos Phase 1: Prepare. Simulates the acceptor's behavior.
     * Randomly fails to simulate acceptor crash/restart.
     *
     * @param proposalId     Unique proposal ID.
     * @param operation      The proposed operation.
     * @param key            The key for the operation.
     * @param value          The value for PUT (null for DELETE).
     * @param proposalNumber The proposal number.
     * @return "PROMISE" if the proposal is accepted, "REJECT" if not, or "FAIL" on simulated failure.
     * @throws RemoteException if an RMI error occurs.
     */
    @Override
    public String paxosPrepare(String proposalId, String operation, String key, String value, long proposalNumber) throws RemoteException {
        // Simulate random failure (e.g., a 20% chance to fail).
        if (random.nextDouble() < 0.2) {
            System.err.println("Simulated failure in paxosPrepare for proposal " + proposalId);
            return "FAIL";
        }
        // Get or create a PaxosInstance for this proposal.
        PaxosInstance instance = paxosInstances.computeIfAbsent(proposalId, k -> new PaxosInstance());
        synchronized (instance) {
            if (proposalNumber > instance.promisedProposalNumber) {
                instance.promisedProposalNumber = proposalNumber;
                return "PROMISE";
            } else {
                return "REJECT";
            }
        }
    }

    /**
     * Paxos Phase 2: Accept. Simulates the acceptor's behavior.
     * Also includes a chance to simulate random failure.
     *
     * @param proposalId     Unique proposal ID.
     * @param operation      The proposed operation.
     * @param key            The key for the operation.
     * @param value          The value for PUT (null for DELETE).
     * @param proposalNumber The proposal number.
     * @return "ACCEPTED" if accepted, "REJECT" if rejected, or "FAIL" on simulated failure.
     * @throws RemoteException if an RMI error occurs.
     */
    @Override
    public String paxosAccept(String proposalId, String operation, String key, String value, long proposalNumber) throws RemoteException {
        // Simulate random failure of acceptor.
        if (random.nextDouble() < 0.2) {
            System.err.println("Simulated failure in paxosAccept for proposal " + proposalId);
            return "FAIL";
        }
        PaxosInstance instance = paxosInstances.get(proposalId);
        if (instance == null) {
            return "REJECT";
        }
        synchronized (instance) {
            if (proposalNumber >= instance.promisedProposalNumber) {
                instance.acceptedProposalNumber = proposalNumber;
                instance.acceptedOperation = operation;
                instance.acceptedValue = value;
                return "ACCEPTED";
            } else {
                return "REJECT";
            }
        }
    }

    /**
     * Paxos Commit phase. Notifies the acceptor to commit the operation.
     * Clears the Paxos state associated with the proposal.
     *
     * @param proposalId Unique proposal ID.
     * @param operation  The operation being committed.
     * @param key        The key for the operation.
     * @param value      The value for a PUT (null for DELETE).
     * @throws RemoteException if an RMI error occurs.
     */
    @Override
    public void paxosCommit(String proposalId, String operation, String key, String value) throws RemoteException {
        // Remove the Paxos instance as it is now committed.
        paxosInstances.remove(proposalId);
        // For a full implementation, learners would be notified here.
        System.out.println("Paxos committed proposal " + proposalId + ": " + operation + " on key " + key);
    }
}
