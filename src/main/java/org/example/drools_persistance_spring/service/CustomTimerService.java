package org.example.drools_persistance_spring.service;

import lombok.extern.slf4j.Slf4j;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.JDKTimerService;
import org.drools.core.time.impl.TimerJobInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom drools timer-job service extension with common thread pool
 *
 * @author bajura-ea
 */
@Slf4j
public class CustomTimerService extends JDKTimerService {

    private static final ScheduledThreadPoolExecutor COMMON_SCHEDULED_TASK_POOL;

    private final AtomicBoolean isRunning = new AtomicBoolean();

    private AtomicLong idCounter = new AtomicLong();

    protected List<TimerJobInstance> timerJobInstances = new ArrayList<>();

    static {
        COMMON_SCHEDULED_TASK_POOL = new ScheduledThreadPoolExecutor(1);
        COMMON_SCHEDULED_TASK_POOL.setMaximumPoolSize(10);
        COMMON_SCHEDULED_TASK_POOL.setKeepAliveTime(30, TimeUnit.SECONDS);
        COMMON_SCHEDULED_TASK_POOL.setRemoveOnCancelPolicy(true);
        log.debug("*** CustomTimerService initialized");
    }

    public CustomTimerService() {
        this.scheduler = COMMON_SCHEDULED_TASK_POOL;
        isRunning.set(true);
    }

    public CustomTimerService(int size) {
        this();
    }

    public void setCounter(long counter) {
        idCounter = new AtomicLong(counter);
    }


    @Override
    public JobHandle scheduleJob(Job job,
                                 JobContext ctx,
                                 Trigger trigger) {

        Date date = trigger.hasNextFireTime();


        if (isRunning.get() && date != null) {
            JDKJobHandle jobHandle = new JDKJobHandle(idCounter.getAndIncrement());

            TimerJobInstance jobInstance = jobFactoryManager.createTimerJobInstance(job,
                    ctx,
                    trigger,
                    jobHandle,
                    this);
            jobHandle.setTimerJobInstance(jobInstance);
            internalSchedule(jobInstance);
            if (isRunning.get()) {
                timerJobInstances.add(jobInstance);
            }

            return jobHandle;
        } else {
            return null;
        }
    }


    @Override
    public void shutdown() {
        isRunning.set(false);
        timerJobInstances.stream()
                .forEach(timerJobInstance -> {
                    boolean result = ((JDKJobHandle) timerJobInstance.getJobHandle()).getFuture().cancel(true);
                    log.debug("*** Timer job {} is {}", timerJobInstance.getJobHandle(), result ? "canceled" : "done");
                });
        COMMON_SCHEDULED_TASK_POOL.purge();
    }
}
