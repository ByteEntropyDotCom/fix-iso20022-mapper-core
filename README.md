# FIX to ISO 20022 Message Mapper

A high-performance financial messaging gateway designed to bridge the gap between **FIX (Financial Information eXchange)** trading protocols and **ISO 20022** payment standards.

## 🚀 Key Features

*   **Java 21 Virtual Threads:** Built on `newVirtualThreadPerTaskExecutor` to handle massive throughput with minimal memory overhead.
*   **Dictionary-Based Parsing:** Uses **QuickFIX/J** for professional-grade FIX message decomposition.
*   **XSD Validation:** Ensures all generated ISO 20022 XML complies with specific market schemas (sese.023) before delivery.
*   **Multi-Market Support:** Strategy-based mapping for **Singapore (MEPS+)**, **Japan (Zengin)**, **UK (CHAPS)**, and **USA (FedWire)**.
*   **Reactive Ingress:** Netty-based TCP server for low-latency message ingestion.
*   **Reliable Egress:** Integrated Kafka producer with configurable resilience and blocking timeouts.

## 🏗️ Architecture

1. **Ingress:** Netty listens on TCP port `9876`.
2. **Dispatch:** Messages are handed off to **Virtual Threads**.
3. **Transform:** `UniversalMessageMapper` parses FIX and injects data into XML templates.
4. **Validate:** XML is validated against market-specific `.xsd` files.
5. **Egress:** Final ISO payload is published to Kafka.

## 🛠️ Getting Started

### Prerequisites
* Java 21+
* Maven 3.9+
* Docker (optional)

### Installation & Build
```bash
mvn clean install
```

### Running with Docker
```
Bash
docker build -t fix-iso-mapper .
docker run -p 9876:9876 fix-iso-mapper
```

### Quick Test

Send a sample Execution Report via Netcat:

```
Bash
echo -e "8=FIX.4.4\u000135=8\u000144=100.25\u000110=031\u0001" | nc localhost 9876
```

### 📊 Performance
The included LoadTest.java demonstrates the efficiency of Virtual Threads by processing 10,000 messages in under 5 seconds on standard consumer hardware

### LICENSE
MIT