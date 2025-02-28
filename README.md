# Multi-threaded Key-Value Store using Java RMI

## **Project Overview**
This project extends **Project #1** by implementing a **multi-threaded Key-Value Store** using **Remote Procedure Calls (RPC)**. Instead of using TCP/UDP sockets, this version leverages **Java RMI** (Remote Method Invocation), allowing clients to remotely invoke methods on the server.

### **Key Features:**
- **Multi-threaded Server:** Supports concurrent client requests using `ConcurrentHashMap` and `ReentrantReadWriteLock`.
- **Remote Procedure Calls (RPC):** Uses Java RMI for client-server communication.
- **Prepopulated Key-Value Store:** Clients initialize the store with sample key-value pairs and perform **5 PUT, 5 GET, and 5 DELETE operations**.
- **Thread-Safe Design:** Ensures proper synchronization using Java concurrency utilities.

---

## **Setup and Execution**

### **1. Compilation**
```bash
javac rmi/*.java
```

### **2. Start the RMI Server**
```bash
java rmi.RMIServer
```
✅ Expected Output:
```
RMI Server started on port 1099...
```

### **3. Run the RMI Client**
```bash
java rmi.RMIClient
```
✅ Expected Output:
```
Connected to RMI Server...
PUT key1: OK
PUT key2: OK
PUT key3: OK
PUT key4: OK
PUT key5: OK
GET key1: value1
GET key2: value2
GET key3: value3
GET key4: value4
GET key5: value5
DELETE key1: OK
DELETE key2: OK
DELETE key3: OK
DELETE key4: OK
DELETE key5: OK
```

---

## **Testing Concurrent Clients**
To test multiple clients accessing the server concurrently, open multiple terminal windows and run:
```bash
java rmi.RMIClient &
java rmi.RMIClient &
java rmi.RMIClient &
```
Each client will send RPC requests **simultaneously**.

---

## **Error Handling and Troubleshooting**
| **Issue** | **Cause** | **Solution** |
|----------|---------|-------------|
| `java.rmi.ConnectException` | Server not running | Start `RMIServer` first |
| `java.rmi.NotBoundException` | Client can't find `KeyValueStore` | Ensure `rebind()` is called in `RMIServer` |
| `Timeout on Client` | Server overloaded | Check server logs for bottlenecks |

---

## **Executive Summary**
This project successfully implements an **RPC-based, multi-threaded Key-Value Store**. It meets all project requirements and demonstrates how **Java RMI** can be used to handle concurrent operations efficiently. The use of **ConcurrentHashMap** and **ReentrantReadWriteLock** ensures safe access to shared data.

**Potential Use Case:** This design can be extended into a **distributed caching system**, where multiple clients interact with a centralized cache server. With **replication and persistence**, it could serve as a lightweight NoSQL database.
