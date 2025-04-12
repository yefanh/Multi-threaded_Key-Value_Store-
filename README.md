# Project 4 - Distributed Key-Value Store with Paxos (Java RMI)

This project implements a fault-tolerant, replicated Key-Value Store using the Paxos consensus algorithm and Java RMI.

---

## How to Run

### Step 1: Compile the Code

```bash
javac -d . rmi/*.java
```

---

### Step 2: Start 5 Replica Servers

Open **five separate terminal windows or tabs**, and run the following commands **one in each**:

```bash
java rmi.RMIServer 1099 "1100,1101,1102,1103"
java rmi.RMIServer 1100 "1099,1101,1102,1103"
java rmi.RMIServer 1101 "1099,1100,1102,1103"
java rmi.RMIServer 1102 "1099,1100,1101,1103"
java rmi.RMIServer 1103 "1099,1100,1101,1102"
```

Each server:
- Starts an RMI registry on its port
- Connects to the other replicas (may fail if others not yet started, that's OK)

---

### Step 3: Run the Client

Once all servers are running, open a **new terminal** and run:

```bash
java rmi.RMIClient
```

The client will:
- Connect to a random replica
- Perform 5 PUTs, 5 GETs, and 5 DELETEs

Example output:

```
Connected to RMI Server on port 1102
PUT key1: OK
GET key1: value1
DELETE key1: OK
...
```

---

## Notes

- Paxos is used for `PUT` and `DELETE` operations
- `GET` is served locally without consensus
- Acceptor failure is simulated randomly (20% failure chance)
- System still reaches consensus if a majority of replicas respond

---

## 📂 Project Files

- `KeyValueStoreInterface.java` – RMI interface
- `KeyValueStoreImpl.java` – Paxos logic and store implementation
- `RMIServer.java` – Server launcher
- `RMIClient.java` – Test client
