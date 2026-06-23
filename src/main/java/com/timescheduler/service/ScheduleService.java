package com.timescheduler.service;

import com.timescheduler.dto.ScheduleDTO;
import com.timescheduler.entity.Schedule;
import com.timescheduler.entity.ScheduleItem;
import com.timescheduler.entity.User;
import com.timescheduler.repository.ScheduleRepository;
import com.timescheduler.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ScheduleService {

    @Inject
    ScheduleRepository scheduleRepository;

    @Inject
    UserRepository userRepository;

    @Transactional
    public ScheduleDTO createSchedule(Long userId, ScheduleDTO scheduleDTO) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Schedule schedule = Schedule.builder()
                .name(scheduleDTO.getName())
                .description(scheduleDTO.getDescription())
                .color(scheduleDTO.getColor() != null ? scheduleDTO.getColor() : "#000000")
                .user(user)
                .isActive(true)
                .build();

        scheduleRepository.persist(schedule);
        return mapToDTO(schedule);
    }

    public Optional<ScheduleDTO> getScheduleById(Long id) {
        return scheduleRepository.findByIdOptional(id).map(this::mapToDTO);
    }

    public List<ScheduleDTO> getSchedulesByUser(Long userId) {
        return scheduleRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ScheduleDTO> getActiveSchedulesByUser(Long userId) {
        return scheduleRepository.findActiveByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleDTO updateSchedule(Long id, ScheduleDTO scheduleDTO) {
        Schedule schedule = scheduleRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));

        schedule.setName(scheduleDTO.getName());
        schedule.setDescription(scheduleDTO.getDescription());
        schedule.setColor(scheduleDTO.getColor());
        schedule.setIsActive(scheduleDTO.getIsActive());

        scheduleRepository.persist(schedule);
        return mapToDTO(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));
        scheduleRepository.delete(schedule);
    }

    @Transactional
    public void deactivateSchedule(Long id) {
        Schedule schedule = scheduleRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));
        schedule.setIsActive(false);
        scheduleRepository.persist(schedule);
    }

    private ScheduleDTO mapToDTO(Schedule schedule) {
        return ScheduleDTO.builder()
                .id(schedule.getId())
                .name(schedule.getName())
                .description(schedule.getDescription())
                .color(schedule.getColor())
                .isActive(schedule.getIsActive())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .items(schedule.getItems().stream()
                        .map(this::mapItemToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private com.timescheduler.dto.ScheduleItemDTO mapItemToDTO(ScheduleItem item) {
        return com.timescheduler.dto.ScheduleItemDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .category(item.getCategory())
                .isCompleted(item.getIsCompleted())
                .priority(item.getPriority())
                .location(item.getLocation())
                .notes(item.getNotes())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
