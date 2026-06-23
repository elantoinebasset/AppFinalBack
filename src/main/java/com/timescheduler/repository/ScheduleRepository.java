package com.timescheduler.repository;

import com.timescheduler.entity.Schedule;
import com.timescheduler.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ScheduleRepository implements PanacheRepository<Schedule> {

    public List<Schedule> findByUser(User user) {
        return find("user", user).list();
    }

    public List<Schedule> findActiveByUser(User user) {
        return find("user = ?1 and isActive = true", user).list();
    }

    public List<Schedule> findByUserId(Long userId) {
        return find("user.id", userId).list();
    }

    public List<Schedule> findActiveByUserId(Long userId) {
        return find("user.id = ?1 and isActive = true", userId).list();
    }
}
