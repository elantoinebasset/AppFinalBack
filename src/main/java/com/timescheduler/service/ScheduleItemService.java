package com.timescheduler.service;

import com.timescheduler.dto.ScheduleItemDTO;
import com.timescheduler.entity.Schedule;
import com.timescheduler.entity.ScheduleItem;
import com.timescheduler.repository.ScheduleItemRepository;
import com.timescheduler.repository.ScheduleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ScheduleItemService {

    @Inject
    ScheduleItemRepository itemRepository;

    @Inject
    ScheduleRepository scheduleRepository;

    @Transactional
    public ScheduleItemDTO createItem(Long scheduleId, ScheduleItemDTO itemDTO) {
        Schedule schedule = scheduleRepository.findByIdOptional(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        if (itemDTO.getStartTime().isAfter(itemDTO.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        ScheduleItem item = ScheduleItem.builder()
                .title(itemDTO.getTitle())
                .description(itemDTO.getDescription())
                .startTime(itemDTO.getStartTime())
                .endTime(itemDTO.getEndTime())
                .category(itemDTO.getCategory())
                .priority(itemDTO.getPriority() != null ? itemDTO.getPriority() : 0)
                .location(itemDTO.getLocation())
                .notes(itemDTO.getNotes())
                .isCompleted(false)
                .schedule(schedule)
                .build();

        itemRepository.persist(item);
        return mapToDTO(item);
    }

    public Optional<ScheduleItemDTO> getItemById(Long id) {
        return itemRepository.findByIdOptional(id).map(this::mapToDTO);
    }

    public List<ScheduleItemDTO> getItemsBySchedule(Long scheduleId) {
        return itemRepository.findByScheduleId(scheduleId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ScheduleItemDTO> getItemsByDateRange(Long scheduleId, LocalDateTime startDate, LocalDateTime endDate) {
        return itemRepository.findByScheduleIdAndDateRange(scheduleId, startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ScheduleItemDTO> getUpcomingItems(Long scheduleId) {
        return itemRepository.findUpcoming(scheduleId, LocalDateTime.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ScheduleItemDTO> getCompletedItems(Long scheduleId) {
        return itemRepository.findCompleted(scheduleId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ScheduleItemDTO> getItemsByPriority(Long scheduleId, Integer priority) {
        return itemRepository.findByPriority(scheduleId, priority).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleItemDTO updateItem(Long id, ScheduleItemDTO itemDTO) {
        ScheduleItem item = itemRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule item not found: " + id));

        if (itemDTO.getStartTime().isAfter(itemDTO.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        item.setTitle(itemDTO.getTitle());
        item.setDescription(itemDTO.getDescription());
        item.setStartTime(itemDTO.getStartTime());
        item.setEndTime(itemDTO.getEndTime());
        item.setCategory(itemDTO.getCategory());
        item.setPriority(itemDTO.getPriority());
        item.setLocation(itemDTO.getLocation());
        item.setNotes(itemDTO.getNotes());
        item.setIsCompleted(itemDTO.getIsCompleted());

        itemRepository.persist(item);
        return mapToDTO(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        ScheduleItem item = itemRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule item not found: " + id));
        itemRepository.delete(item);
    }

    @Transactional
    public ScheduleItemDTO markAsCompleted(Long id) {
        ScheduleItem item = itemRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule item not found: " + id));
        item.setIsCompleted(true);
        itemRepository.persist(item);
        return mapToDTO(item);
    }

    @Transactional
    public ScheduleItemDTO markAsIncompleted(Long id) {
        ScheduleItem item = itemRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule item not found: " + id));
        item.setIsCompleted(false);
        itemRepository.persist(item);
        return mapToDTO(item);
    }

    private ScheduleItemDTO mapToDTO(ScheduleItem item) {
        return ScheduleItemDTO.builder()
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
