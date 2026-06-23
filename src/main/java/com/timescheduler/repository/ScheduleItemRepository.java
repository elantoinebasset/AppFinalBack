package com.timescheduler.repository;

import com.timescheduler.entity.Schedule;
import com.timescheduler.entity.ScheduleItem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ScheduleItemRepository implements PanacheRepository<ScheduleItem> {

    public List<ScheduleItem> findBySchedule(Schedule schedule) {
        return find("schedule", schedule).list();
    }

    public List<ScheduleItem> findByScheduleId(Long scheduleId) {
        return find("schedule.id", scheduleId).list();
    }

    public List<ScheduleItem> findByScheduleIdAndDateRange(Long scheduleId, LocalDateTime startDate, LocalDateTime endDate) {
        return find("schedule.id = ?1 and startTime >= ?2 and endTime <= ?3 order by startTime asc",
                   scheduleId, startDate, endDate).list();
    }

    public List<ScheduleItem> findUpcoming(Long scheduleId, LocalDateTime now) {
        return find("schedule.id = ?1 and startTime >= ?2 and isCompleted = false order by startTime asc",
                   scheduleId, now).list();
    }

    public List<ScheduleItem> findCompleted(Long scheduleId) {
        return find("schedule.id = ?1 and isCompleted = true order by endTime desc", scheduleId).list();
    }

    public List<ScheduleItem> findByPriority(Long scheduleId, Integer priority) {
        return find("schedule.id = ?1 and priority = ?2 order by startTime asc",
                   scheduleId, priority).list();
    }
}
