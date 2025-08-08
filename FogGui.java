package org.fog.test.perfeval;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.BiConsumer;

public class FogGui extends JFrame {
    private JTextArea logArea;
    private JPanel graphPanel;

    public FogGui() {
        setTitle("5G Smart Traffic Monitoring Simulator");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JButton startButton = new JButton("Start Simulation");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runSimulation();
            }
        });

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(1000, 150));

        graphPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawNetworkDiagram(g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(1000, 600));

        add(startButton, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void runSimulation() {
        logArea.append("Simulation started...\n");
        // TrafficMonitoringFog.main(new String[]{}); // Hook to run simulation
        logArea.append("Simulation completed.\n");
    }

    private void drawNetworkDiagram(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int baseY = 50;
        int boxWidth = 180;
        int boxHeight = 60;
        int spacingY = 100;

        Color boxColor = new Color(70, 130, 180); // Steel blue
        Color shadowColor = boxColor.darker();

        BiConsumer<Integer, Integer> draw3DBox = (x, y) -> {
            g2.setColor(shadowColor);
            g2.fillRoundRect(x + 5, y + 5, boxWidth, boxHeight, 15, 15);
            g2.setColor(boxColor);
            g2.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);
        };

        // Cloud
        int cloudX = centerX - boxWidth / 2;
        int cloudY = baseY;
        draw3DBox.accept(cloudX, cloudY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Cloud", cloudX + 60, cloudY + 35);

        // Edge Server
        int edgeY = cloudY + spacingY;
        draw3DBox.accept(cloudX, edgeY);
        g2.drawString("Edge Server", cloudX + 40, edgeY + 35);
        g2.drawLine(centerX, cloudY + boxHeight, centerX, edgeY);

        // Intersection Gateway
        int gatewayY = edgeY + spacingY;
        draw3DBox.accept(cloudX, gatewayY);
        g2.drawString("Intersection Gateway", cloudX + 10, gatewayY + 35);
        g2.drawLine(centerX, edgeY + boxHeight, centerX, gatewayY);

        // Sensor Types
        int sensorY = gatewayY + spacingY;
        String[] sensorLabels = {"Camera Sensor", "Speed Sensor", "GPS Sensor", "Environmental Sensor"};
        int sensorBoxW = 150;
        int sensorBoxH = 50;
        int sensorSpacing = 180;
        int startSensorX = centerX - ((sensorLabels.length - 1) * sensorSpacing) / 2;

        for (int i = 0; i < sensorLabels.length; i++) {
            int sensorX = startSensorX + i * sensorSpacing;
            g2.setColor(shadowColor);
            g2.fillRoundRect(sensorX + 5, sensorY + 5, sensorBoxW, sensorBoxH, 15, 15);
            g2.setColor(boxColor);
            g2.fillRoundRect(sensorX, sensorY, sensorBoxW, sensorBoxH, 15, 15);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(sensorX, sensorY, sensorBoxW, sensorBoxH, 15, 15);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(sensorLabels[i], sensorX + 15, sensorY + 30);

            // Connect to Gateway
            int sx = sensorX + sensorBoxW / 2;
            int sy = sensorY;
            int gx = centerX;
            int gy = gatewayY + boxHeight;
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(gx, gy, sx, sy);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FogGui::new);
    }
}
