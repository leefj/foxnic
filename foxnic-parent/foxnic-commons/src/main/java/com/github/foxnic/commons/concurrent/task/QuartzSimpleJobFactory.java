package com.github.foxnic.commons.concurrent.task;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class QuartzSimpleJobFactory extends SimpleJobFactory{
	@Override
	public Job newJob(TriggerFiredBundle bundle, Scheduler Scheduler) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();
        return (Job)jobDetail.getJobDataMap().get("instance");
    }
}
