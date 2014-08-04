/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

/**
 *
 * @author jonathan
 */
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

@ManagedBean(name = "scheduler")
@SessionScoped
public class SchedulerBean extends AbstractManagedBean<Object> implements Serializable {

    private static final long serialVersionUID = 1L;
    private transient Scheduler scheduler;
    private transient List<QuartzJob> quartzJobList = new ArrayList<QuartzJob>();

    public SchedulerBean() throws SchedulerException {
        super(Object.class);
    }

    //trigger a job
    public void fireNow(String jobName, String jobGroup)
            throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);

    }
    
    public void deleteJob(String jobName, String jobGroup)
            throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);

    }

    public List<QuartzJob> getQuartzJobList() {
        try {
            scheduler = HelpDeskScheluder.getInstance();
            quartzJobList = new ArrayList<QuartzJob>();

            // loop jobs by group
            for (String groupName : scheduler.getJobGroupNames()) {

                // get jobkey
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher
                        .jobGroupEquals(groupName))) {

                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();

                    // get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler
                            .getTriggersOfJob(jobKey);
                    Date nextFireTime = triggers.get(0).getNextFireTime();

                    quartzJobList.add(new QuartzJob(jobName, jobGroup, nextFireTime));

                }

            }

            return quartzJobList;
        } catch (SchedulerException ex) {
            JsfUtil.addErrorMessage(ex, ex.getMessage());
            Logger.getLogger(SchedulerBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class QuartzJob {

        private static final long serialVersionUID = 1L;
        String jobName;
        String jobGroup;
        Date nextFireTime;

        public QuartzJob(String jobName, String jobGroup, Date nextFireTime) {

            this.jobName = jobName;
            this.jobGroup = jobGroup;
            this.nextFireTime = nextFireTime;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public String getJobGroup() {
            return jobGroup;
        }

        public void setJobGroup(String jobGroup) {
            this.jobGroup = jobGroup;
        }

        public Date getNextFireTime() {
            return nextFireTime;
        }

        public void setNextFireTime(Date nextFireTime) {
            this.nextFireTime = nextFireTime;
        }
    }
}
