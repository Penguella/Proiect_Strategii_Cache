package cache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        String filePath = "E:\\JavaProiecte\\Cache4\\src\\cache\\tasks.txt"; //calea catre fisier
        TaskManager taskManager = new TaskManager(filePath);

        //Cream o fereastra JFrame pentru interfata grafica
        JFrame frame = new JFrame("Task Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 500);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> taskList = new JList<>(listModel);
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Checkbox pentru aratarea/ascunderea task-urilor completate
        JCheckBox showCompletedCheckBox = new JCheckBox("Show Completed Tasks");
        showCompletedCheckBox.setSelected(true); // Afiseaza task-urile completate by default

        // Metoda pentru actualizarea listei de task-uri afisate
        Runnable updateTaskList = () -> {
            listModel.clear();
            boolean showCompleted = showCompletedCheckBox.isSelected();
            for (Task task : taskManager.getFilteredTasks(showCompleted)) {
                listModel.addElement(task.toString());
            }
        };

        updateTaskList.run(); // Initializarea task-urilor

        JPanel panel = new JPanel();
        JTextField taskField = new JTextField(20);
        JButton addButton = new JButton("Add Task");
        JButton completeButton = new JButton("Complete Task");
        panel.add(taskField);
        panel.add(addButton);
        panel.add(completeButton);
        panel.add(showCompletedCheckBox);
        frame.add(panel, BorderLayout.SOUTH);//spatiul pt butoane

        //Butonul de adaugare task nou
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String description = taskField.getText();
                taskManager.addTask(description);
                updateTaskList.run();
                taskField.setText("");
            }
        });

        //Butonul de completare a task-ului selectat
        completeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedTask = listModel.get(selectedIndex);
                    String[] parts = selectedTask.split(",");
                    int id = Integer.parseInt(parts[0].trim());
                    taskManager.completeTask(id);
                    updateTaskList.run();
                }
            }
        });

        //Checkbox-ul ce permite ascunderea task-urilor realizate
        showCompletedCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateTaskList.run();
            }
        });

        // Butoane aditionale pentru citirea task-urilor, masurarea timpilor de citire, memoriei utilizate
        //si invalidarea cache-ului
        JButton measureFileReadPerformanceButton = new JButton("Measure File Read Performance");
        JButton measureCacheReadPerformanceButton = new JButton("Measure Cache Read Performance");
        JButton invalidateCacheButton = new JButton("Invalidate Cache");
        JButton readTasksButton = new JButton("Read Tasks");

        // Adăugarea acțiunilor pentru butoanele de măsurare a timpului
        measureFileReadPerformanceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long time = taskManager.measureFileReadTime(filePath);
                long memoryUsedBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                double memoryUsedMegabits = (double) memoryUsedBytes / (8 * 1024 * 1024); // Convertim din bytes în megabiti
                String memoryFormatted = String.format("%.2f", memoryUsedMegabits); // Formatăm memoria cu 2 zecimale
                JOptionPane.showMessageDialog(frame, "File Read Time: " + time + " ns\nMemory Used: " + memoryFormatted + " Mb");
            }
        });

        measureCacheReadPerformanceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long time = taskManager.measureCacheReadTime();
                long memoryUsedBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                double memoryUsedMegabits = (double) memoryUsedBytes / (8 * 1024 * 1024); // Convertim din bytes în megabiti
                String memoryFormatted = String.format("%.2f", memoryUsedMegabits); // Formatăm memoria cu 2 zecimale
                JOptionPane.showMessageDialog(frame, "Cache Read Time: " + time + " ns\nMemory Used: " + memoryFormatted + " Mb");
            }
        });

        invalidateCacheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taskManager.invalidateCache();
                JOptionPane.showMessageDialog(frame, "Cache invalidated.");
                updateTaskList.run();
            }
        });

        readTasksButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taskManager.readTasks(filePath);
                updateTaskList.run();
            }
        });
        //adaugarea butoanelor de test.
        panel.add(measureFileReadPerformanceButton);
        panel.add(measureCacheReadPerformanceButton);
        panel.add(invalidateCacheButton);
        panel.add(readTasksButton);
        //Butoanele ce sunt utilizate doar pentru testarea valorilor de timp si memorie vor fi colorate in galben
        //pentru a sublinia faptul ca ele sunt relevante doar pentru partea de testare
        measureFileReadPerformanceButton.setBackground(Color.YELLOW);
        measureCacheReadPerformanceButton.setBackground(Color.YELLOW);
        invalidateCacheButton.setBackground(Color.YELLOW);
        readTasksButton.setBackground(Color.YELLOW);
        // Legenda care să explice rolul butoanelor
        JPanel legendPanel = new JPanel();
        JLabel legendLabel = new JLabel("Yellow buttons are for testing purposes only");
        legendPanel.add(legendLabel);
        legendPanel.setBackground(Color.LIGHT_GRAY);
        frame.add(legendPanel, BorderLayout.NORTH);

        frame.setVisible(true);
    }
}
