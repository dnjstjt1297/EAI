package main.java.order.scheduler.service;

import main.java.global.logging.annotation.LogExecution;
import main.java.order.scheduler.OrderJob;
import main.java.order.scheduler.OrderJobFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;


public class QuartzOrderSchedulerService implements OrderSchedulerService {

    private static final String JOB_NAME = "SHIPMENT_JOB";
    private static final String JOB_GROUP = "SHIPMENT_GROUP";
    private static final String TRIGGER_NAME = "SHIPMENT_UPDATE";
    private static final String TRIGGER_GROUP = "SHIPMENT_GROUP";
    private static final String CRON_EXPRESSION = "0 0/5 * * * ?";

    private Scheduler scheduler;

    private final OrderJobFactory orderJobFactory;

    public QuartzOrderSchedulerService(OrderJobFactory orderJobFactory) {
        this.orderJobFactory = orderJobFactory;
    }

    @Override
    @LogExecution
    public void start() {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.setJobFactory(orderJobFactory);

            JobDetail jobDetail = JobBuilder.newJob(OrderJob.class)
                    .withIdentity(JOB_NAME, JOB_GROUP)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_NAME, TRIGGER_GROUP)
                    .withSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            if (!scheduler.isStarted()) {
                scheduler.start();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @LogExecution
    public void stop() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }


}
