package org.fog.test.perfeval;

import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.*;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.*;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.*;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * This class simulates a 5G-enabled Smart Traffic Monitoring system using the iFogSim framework.
 * It builds a fog computing hierarchy and runs an application that processes data from multiple sensors.
 */
public class TrafficMonitoringFog {

    // Lists to hold all fog devices, sensors, and actuators used in the simulation
    static List<FogDevice> fogDevices = new ArrayList<>();
    static List<Sensor> sensors = new ArrayList<>();
    static List<Actuator> actuators = new ArrayList<>();

    // Number of intersections and cameras to simulate
    static int numOfIntersections = 2;
    static int numOfCamerasPerIntersection = 2;

    // Flag to control module placement: cloud or edge
    private static boolean CLOUD = false;

    public static void main(String[] args) {
        Log.printLine("Starting 5G Smart Traffic Monitoring System...");

        try {
            // Initialize CloudSim with 1 user, current time, no trace
            CloudSim.init(1, Calendar.getInstance(), false);

            // Create FogBroker
            FogBroker broker = new FogBroker("broker");

            // Application ID
            String appId = "5g-smart-traffic";

            // Build the application model
            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            // Build the physical fog infrastructure
            createFogDevices(broker.getId(), appId);

            // Module mapping: maps modules to specific devices
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            // Map 'congestion_detector' module to all camera devices
            for (FogDevice device : fogDevices) {
                if (device.getName().startsWith("cam")) {
                    moduleMapping.addModuleToDevice("congestion_detector", device.getName());
                }
            }

            // Map 'user_interface' to the cloud device
            moduleMapping.addModuleToDevice("user_interface", "cloud");

            // If using cloud placement, map remaining modules to cloud
            if (CLOUD) {
                moduleMapping.addModuleToDevice("speed_estimator", "cloud");
                moduleMapping.addModuleToDevice("event_detector", "cloud");
            }

            // Create controller for managing simulation
            Controller controller = new Controller("controller", fogDevices, sensors, actuators);

            // Submit application and module placement strategy
            controller.submitApplication(application,
                    CLOUD
                            ? new ModulePlacementMapping(fogDevices, application, moduleMapping)
                            : new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping));

            // Start the simulation
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            Log.printLine("5G Smart Traffic Monitoring Simulation finished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates all fog devices including cloud, edge server, and intersection gateways.
     */
    private static void createFogDevices(int userId, String appId) {
        // Cloud Data Center
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 10000, 10000, 0, 0.01, 16 * 103, 16 * 83.25);
        cloud.setParentId(-1); // No parent for cloud
        fogDevices.add(cloud);

        // Edge Server (e.g., regional data center)
        FogDevice edgeServer = createFogDevice("edge-server", 5600, 8000, 10000, 10000, 1, 0, 107.339, 83.4333);
        edgeServer.setParentId(cloud.getId());
        edgeServer.setUplinkLatency(30);  // Latency from edge to cloud
        fogDevices.add(edgeServer);

        // Add multiple intersections
        for (int i = 0; i < numOfIntersections; i++) {
            addIntersection("int-" + i, userId, appId, edgeServer.getId());
        }
    }

    /**
     * Creates a gateway device and multiple camera nodes for an intersection.
     */
    private static void addIntersection(String id, int userId, String appId, int parentId) {
        // Intersection Gateway
        FogDevice gateway = createFogDevice(id + "-gateway", 2800, 4000, 10000, 10000, 2, 0, 87.53, 82.44);
        gateway.setParentId(parentId);
        gateway.setUplinkLatency(5);
        fogDevices.add(gateway);

        // Cameras under each intersection
        for (int i = 0; i < numOfCamerasPerIntersection; i++) {
            String camId = id + "-cam" + i;
            FogDevice cam = createFogDevice("cam-" + camId, 1000, 2000, 5000, 5000, 3, 0, 87.53, 82.44);
            cam.setParentId(gateway.getId());
            cam.setUplinkLatency(1);
            fogDevices.add(cam);

            // Attach 4 types of sensors to each camera
            sensors.add(new Sensor("cam-sensor-" + camId, "CAMERA", userId, appId, new DeterministicDistribution(2)));
            sensors.add(new Sensor("speed-sensor-" + camId, "SPEED", userId, appId, new DeterministicDistribution(2)));
            sensors.add(new Sensor("gps-sensor-" + camId, "GPS", userId, appId, new DeterministicDistribution(2)));
            sensors.add(new Sensor("env-sensor-" + camId, "ENV", userId, appId, new DeterministicDistribution(10)));

            for (Sensor s : sensors.subList(sensors.size() - 4, sensors.size())) {
                s.setGatewayDeviceId(cam.getId());
                s.setLatency(0.5);
            }

            // Attach actuator to control traffic signals
            Actuator signal = new Actuator("signal-" + camId, userId, appId, "TRAFFIC_SIGNAL");
            signal.setGatewayDeviceId(cam.getId());
            signal.setLatency(0.5);
            actuators.add(signal);
        }
    }

    /**
     * Creates a fog device with given resources and power model.
     */
    private static FogDevice createFogDevice(String name, long mips, int ram, long upBw, long downBw,
                                             int level, double ratePerMips, double busyPower, double idlePower) {
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // Processing Element

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000;
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage, peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );

        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                "x86", "Linux", "Xen",
                host, 10.0, 3.0, 0.05, 0.001, 0.0
        );

        FogDevice device = null;
        try {
            device = new FogDevice(
                    name, characteristics,
                    new AppModuleAllocationPolicy(hostList), new LinkedList<>(), 10,
                    upBw, downBw, 0, ratePerMips
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        device.setLevel(level);
        return device;
    }

    /**
     * Builds the application logic including modules, edges, and loops.
     */
    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        // Define modules
        application.addAppModule("congestion_detector", 10);
        application.addAppModule("speed_estimator", 10);
        application.addAppModule("event_detector", 10);
        application.addAppModule("user_interface", 10);

        // Define data edges from sensors to modules
        application.addAppEdge("CAMERA", "congestion_detector", 2000, 2000, "CAMERA_STREAM", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("SPEED", "speed_estimator", 1000, 1000, "SPEED_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("GPS", "speed_estimator", 1000, 500, "GPS_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("ENV", "event_detector", 500, 500, "ENV_DATA", Tuple.UP, AppEdge.SENSOR);

        // Module-to-module edges
        application.addAppEdge("congestion_detector", "event_detector", 1000, 1000, "CONGESTION", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("speed_estimator", "event_detector", 1000, 1000, "SPEED_STATS", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("event_detector", "user_interface", 500, 500, "ALERTS", Tuple.UP, AppEdge.MODULE);

        // Module to actuator
        application.addAppEdge("event_detector", "TRAFFIC_SIGNAL", 100, 28, "SIGNAL_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);

        // Tuple processing rules
        application.addTupleMapping("congestion_detector", "CAMERA_STREAM", "CONGESTION", new FractionalSelectivity(0.8));
        application.addTupleMapping("speed_estimator", "SPEED_DATA", "SPEED_STATS", new FractionalSelectivity(0.9));
        application.addTupleMapping("speed_estimator", "GPS_DATA", "SPEED_STATS", new FractionalSelectivity(0.9));
        application.addTupleMapping("event_detector", "ENV_DATA", "ALERTS", new FractionalSelectivity(0.7));

        // Define application loops (paths for analytics)
        application.setLoops(Arrays.asList(
                new AppLoop(Arrays.asList("congestion_detector", "event_detector")),
                new AppLoop(Arrays.asList("speed_estimator", "event_detector", "TRAFFIC_SIGNAL"))
        ));

        return application;
    }
}