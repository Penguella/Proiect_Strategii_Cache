package cache;

import java.io.*;
import java.util.*;

public class TaskManager {
    private static final int MAX_CACHE_SIZE = 100; // Limita dimensiunii cache-ului LRU
    private static final long CACHE_EXPIRATION_TIME = 2 * 60 * 1000; // 2 minute in millisecunde

    private List<Task> tasks;
    private Map<Integer, Task> taskCache;
    private int nextId = 1;
    private Timer cacheTimer;
    private String filePath;// calea catre fisierul txt

    public TaskManager(String filePath) {
        this.filePath = filePath; 
        tasks = new ArrayList<>(); 
        //implementarea unui cache LRU cu dimensiunea si durata de viata date
        taskCache = new LinkedHashMap<Integer, Task>(MAX_CACHE_SIZE, 0.75f, true) { 
            protected boolean removeEldestEntry(Map.Entry<Integer, Task> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
        loadTasksFromFile(filePath);
        startCacheTimer();
    }
//Timer-ul pentru cache
    private void startCacheTimer() {
        if (cacheTimer != null) {
            cacheTimer.cancel();
        }
        cacheTimer = new Timer(true);
        cacheTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                invalidateCache();
            }
        }, CACHE_EXPIRATION_TIME, CACHE_EXPIRATION_TIME);
    }
//Metoda ce incarcă sarcinile din fisier si le adauga in lista de sarcini si cache
    public void loadTasksFromFile(String filePath) { 
        tasks.clear();
        taskCache.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) { // debug pentru gestionarea erorilor cauzate de editarea manuala a fisierului
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.err.println("Skipping invalid line: " + line);
                    continue; // Skip linii ce nu respecta formatul
                }
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String description = parts[1].trim();
                    String status = parts[2].trim();
                    if (!status.equals("todo") && !status.equals("done")) {
                        System.err.println("Invalid status in line: " + line);
                        continue; // Skip linii cu status invalid 
                    }
                    Task task = new Task(id, description, status);
                    tasks.add(task);
                    taskCache.put(id, task);
                    nextId = Math.max(nextId, id + 1);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid ID in line: " + line);
                    // Skip linii cu ID invalid 
                }
            }
            System.out.println("Tasks loaded from file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//Salveaza task-urile in fisier
    public void saveTasksToFile() {
        File file = new File(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Task task : tasks) {
                bw.write(task.getId() + "," + task.getDescription() + "," + task.getStatus());
                bw.newLine();
            }
            System.out.println("Tasks saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//Adauga o sarcina noua si actualizeaza fisierul
    public void addTask(String description) {
        Task task = new Task(nextId++, description, "todo");
        tasks.add(task);
        taskCache.put(task.getId(), task);
        saveTasksToFile();
    }
//Seteaza starea unei sarcini la "done" si actualizeaza fisierul
    public void completeTask(int id) {
        Task task = taskCache.get(id);
        if (task != null) {
            task.setStatus("done");
            saveTasksToFile();
        }
    }
//pentru afișarea listei cu task-uri complete
    public List<Task> getFilteredTasks(boolean showCompleted) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (showCompleted || task.getStatus().equals("todo")) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    public Map<Integer, Task> getTasks() {
        return taskCache;
    }
//Cat timp e necesar citirii din fisier
    public long measureFileReadTime(String filePath) {
        long startTime = System.nanoTime();
        loadTasksFromFile(filePath);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
  //Cat timp e necesar citirii din cache
    
    public long measureCacheReadTime() {
        long startTime = System.nanoTime();
        Map<Integer, Task> cachedTasks = getTasks();
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
//functie pentru invalidarea cache-ului
    public void invalidateCache() {
        taskCache.clear();
        System.out.println("Cache invalidated.");
        startCacheTimer(); // Resetam timer-ul cache-ului daca acesta a fost invalidat manual
    }
//functie pentru citirea task-urilor fie din fisier fie din cache
    public void readTasks(String filePath) {
        if (!taskCache.isEmpty()) {
            System.out.println("Tasks read from cache.");
        } else {
            loadTasksFromFile(filePath);
        }
    }
}
