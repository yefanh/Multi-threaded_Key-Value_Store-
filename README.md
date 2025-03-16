# Multi-threaded Key-Value Store using RPC (Java RMI)

This project implements a **distributed, multi-threaded Key-Value Store** using **Remote Procedure Calls (RPC) with Java RMI**. It supports **replication across 5 servers** and ensures **strong consistency** through **Two-Phase Commit (2PC)** for `PUT` and `DELETE` operations.

---

## 1. Features
✔ **Distributed Key-Value Store** – Runs across **5 replica servers**  
✔ **Remote Procedure Calls (RPC)** – Uses **Java RMI** for communication  
✔ **Multi-threaded** – Supports concurrent requests using **ConcurrentHashMap**  
✔ **Two-Phase Commit (2PC)** – Ensures consistency across replicas  
✔ **Strong Consistency Guarantees** – All replicas commit or abort together  
✔ **Fault-Tolerant Design** – Handles connection failures gracefully  

---

## 2. Code Structure
```
proj3/
│── rmi/
│   │── KeyValueStoreInterface.java  # Defines the RMI interface
│   │── KeyValueStoreImpl.java       # Implements the Key-Value Store with 2PC
│   │── RMIServer.java               # Server that registers itself and connects to replicas
│   │── RMIClient.java               # Client that interacts with the Key-Value Store
│── executive_summary.txt            # Assignment Overview & Technical Impression
│── README.md                        # Project documentation
```

---

## 3. Compilation & Execution
### Step 1: Compile the Project
```bash
javac -d . rmi/*.java
```

### Step 2: Start 5 Servers (each in a separate terminal)
```bash
java rmi.RMIServer 1099 "1100,1101,1102,1103"
java rmi.RMIServer 1100 "1099,1101,1102,1103"
java rmi.RMIServer 1101 "1099,1100,1102,1103"
java rmi.RMIServer 1102 "1099,1100,1101,1103"
java rmi.RMIServer 1103 "1099,1100,1101,1102"
```
Each server will attempt to connect to its replicas.

### Step 3: Run the Client
After **all servers** are up, open a new terminal and execute:
```bash
java rmi.RMIClient
```
The client will randomly connect to one of the servers and perform:
- **5 `PUT` operations** ✅
- **5 `GET` operations** ✅
- **5 `DELETE` operations** ✅

Example Output:
```
Connected to RMI Server on port 1101
PUT key1: OK
GET key1: value1
DELETE key1: OK
```

---

## 4. System Design
### 🔹 Two-Phase Commit (2PC) for Consistency
1️⃣ **Prepare Phase**:  
   - The coordinating server sends `preparePut()` or `prepareDelete()` to all replicas.  
   - If all replicas respond with `"YES"`, proceed to commit. Otherwise, abort.  

2️⃣ **Commit Phase**:  
   - If all replicas approved, commit changes using `commitPut()` or `commitDelete()`.  
   - If any replica fails, the entire operation is **aborted**, ensuring consistency.  

### 🔹 Multi-threading & Concurrency Control
- **Storage**: Uses `ConcurrentHashMap` for efficient thread-safe storage.  
- **Synchronization**: Uses `ReentrantReadWriteLock` to prevent race conditions during writes.

---

## 5. Known Issues & Possible Improvements
❌ **Replica Discovery on Startup**: Initial connection to replicas may fail if they haven't started yet.  
✅ **Solution**: Handles this gracefully and dynamically updates known replicas.  

❌ **No Automatic Recovery for Crashed Servers**  
✅ **Potential Improvement**: Implement **failure detection & retry logic** to make the system more fault-tolerant.  
