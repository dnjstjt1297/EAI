package main.java.global.logging;

public class LogContext {

    private static final ThreadLocal<Integer> depthHolder = ThreadLocal.withInitial(() -> 0);

    public static void increment() {
        depthHolder.set(depthHolder.get() + 1);
    }

    public static void decrement() {
        depthHolder.set(depthHolder.get() - 1);
    }

    public static String getIndent() {
        int depth = depthHolder.get();
        if (depth <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("|   ");
        }
        sb.append("| - ");
        return sb.toString();
    }
}