package fr.nocsy.mcpets.utils;

import fr.nocsy.mcpets.MCPets;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PetTimer {

    @Getter
    private static Map<PetTimer, ScheduledTask> runningTimers = new ConcurrentHashMap<>();

    @Getter
    private int cooldown;
    @Getter
    private volatile int remainingTime;
    private long frequency;

    private volatile ScheduledTask task;

    private final Runnable endingRunnable;

    /**
     * Constructor
     * Frequency giving the tick when repeating the task
     */
    public PetTimer(int cooldown, long frequency, Runnable endingRunnable) {
        this.cooldown = cooldown;
        this.remainingTime = 0;
        this.frequency = frequency;
        this.endingRunnable = endingRunnable;
    }

    public void launch(Runnable runnable) {
        // If it's running then cancel the current scheduler
        if (isRunning())
            stop(null);
        // A disabled plugin cannot schedule, and nothing would run the timer after the stop anyway
        if (!MCPets.getInstance().isEnabled())
            return;
        remainingTime = cooldown;
        // Counts down on the global region: a timer outlives the pet entity it belongs to.
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(MCPets.getInstance(), t -> {
            if (cooldown != Integer.MAX_VALUE)
                remainingTime--;
            if (remainingTime <= 0)
                stop(endingRunnable);

            if (runnable != null)
                runnable.run();
        }, 1L, frequency);
        runningTimers.put(this, task);
    }

    public void stop(Runnable runnable) {
        final ScheduledTask running = task;
        if (running != null)
            running.cancel();
        runningTimers.remove(this);
        remainingTime = 0;
        if (runnable != null)
            runnable.run();
    }

    public boolean isRunning() {
        return remainingTime > 0;
    }
}
