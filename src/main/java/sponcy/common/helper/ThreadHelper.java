package sponcy.common.helper;

public class ThreadHelper {

    String name = null;
    boolean daemon = false;
    int priority = Thread.NORM_PRIORITY;

    protected ThreadHelper() {
    }

    public static ThreadHelper get() {
        return new ThreadHelper();
    }

    public ThreadHelper run(Runnable r) {
        Thread t = new Thread(r);
        if (name != null) t.setName(name);
        t.setDaemon(daemon);
        t.setPriority(priority);
        t.start();
        return this;
    }

    public ThreadHelper name(String name) {
        this.name = name;
        return this;
    }

    public ThreadHelper daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadHelper priority(int priority) {
        this.priority = priority;
        return this;
    }
}
