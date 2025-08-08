
# Smart Traffic Monitoring System using iFogSim

## 🔍 Project Overview
This project simulates a **Smart Traffic Monitoring System** using **iFogSim**, an extension of CloudSim for fog computing. 
It models a real-world scenario where traffic cameras at road intersections monitor congestion, estimate speed, and control traffic signals through a fog-based architecture.

## 💻 Technologies Used
- Java (with iFogSim framework)
- CloudSim & iFogSim libraries
- NetBeans / Eclipse / IntelliJ (any Java IDE)

## 📁 Folder Structure

```
/src/org/fog/test/perfeval/TrafficMonitoringFog.java
```
This is the main simulation file.

## 🧠 Key Features

- Simulation of fog devices: cloud, edge server, gateways, and cameras
- Sensor-actuator interaction: Speed, Camera, GPS and Environmental Sensor
- Application modules: congestion detection, speed estimation, event detection
- Dynamic module placement across fog hierarchy

## 🧩 Fog Architecture

- **Cloud** – Centralized processing unit
- **Edge Server** – Intermediate fog node between cloud and gateway
- **Gateway (int-0)** – Located at intersections
- **Cameras (cam-0-0 to cam-0-1)** – Sensors collecting traffic data
- **Traffic Signals** – Actuators controlling the traffic flow

## ⚙️ Configuration Parameters

- `numOfIntersections = 2`
- `numOfCamerasPerIntersection = 2`
- Each camera is associated with a sensor and a traffic signal (actuator)

## 🔄 Data Flow

1. **Camera Sensors** collect traffic data (vehicle count, speed)
2. Data is sent to **congestion_detector** module
3. **speed_estimator** processes traffic flow data
4. Results sent to:
   - **user_interface** for visualization
   - **TRAFFIC_SIGNAL** actuator for dynamic traffic control

## 🛠️ How to Run (Java)

1. **Download iFogSim** (https://github.com/Cloudslab/iFogSim)
2. Import the project into your Java IDE
3. Replace the contents of `TrafficMonitoringFog.java` with your version
4. Run the file as a Java application

## 📌 Application Modules

- `congestion_detector`
- `speed_estimator`
- `event_detector`
- `user_interface`

## 🔁 Application Loops

1. Congestion Detector → Event Detector
2. Speed Estimator → Event Detector → Traffic Signal (Actuator)

## 📋 Notes

- The simulation only models the system behavior – no real hardware is used
- Fog placement strategy: `ModulePlacementEdgewards`
- Network latency and resource constraints are simulated

---

© Smart Traffic Monitoring – Academic Project
