package lingutil;


public class TimeMeasurer {

    
    public static void measureTime(int warmupRuns, Task... tasks) {
        for (int i = 0; i < warmupRuns; i++) {
            for (Task t : tasks) {
                t.prepare();
                t.run();
                System.gc();
            }
        }
        for (Task t : tasks) {
            t.prepare();
            long ts = System.currentTimeMillis();
            t.run();
            t.displayTime(System.currentTimeMillis() - ts);
            System.gc();
        }
    }
    
    
    
    public interface Task {
        default void prepare() { return; }
        void run();
        void displayTime(long millis);
    }
    
}
