package com.kkvat.automation.service;

import com.kkvat.automation.dto.ReportScheduleRequest;
import com.kkvat.automation.dto.ReportScheduleResponse;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.Report;
import com.kkvat.automation.model.ReportSchedule;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.ReportRepository;
import com.kkvat.automation.repository.ReportScheduleRepository;
import com.kkvat.automation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportScheduleService {
    private final ReportScheduleRepository reportScheduleRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public Page<ReportScheduleResponse> getAllSchedules(Pageable pageable) {
        // In a real app, you'd want pagination. For now, get all and convert
        List<ReportSchedule> schedules = reportScheduleRepository.findAll();
        return new org.springframework.data.domain.PageImpl<>(
                schedules.stream().map(ReportScheduleResponse::from).collect(Collectors.toList()),
                pageable,
                schedules.size()
        );
    }

    public ReportScheduleResponse getScheduleById(Long id) {
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return ReportScheduleResponse.from(schedule);
    }

    public ReportScheduleResponse createSchedule(ReportScheduleRequest request) {
        Report report = reportRepository.findById(request.getReportId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ReportSchedule schedule = ReportSchedule.builder()
                .report(report)
                .scheduleName(request.getScheduleName())
                .frequency(ReportSchedule.Frequency.valueOf(request.getFrequency()))
                .dayOfWeek(request.getDayOfWeek())
                .dayOfMonth(request.getDayOfMonth())
                .timeOfDay(request.getTimeOfDay())
                .emailRecipients(request.getEmailRecipients())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(user)
                .nextExecution(calculateNextExecution(request.getFrequency(), request.getTimeOfDay(), request.getDayOfWeek(), request.getDayOfMonth()))
                .build();

        ReportSchedule saved = reportScheduleRepository.save(schedule);
        auditService.logSuccess("CREATE", "REPORT_SCHEDULE", saved.getId(), "Created schedule: " + request.getScheduleName());
        return ReportScheduleResponse.from(saved);
    }

    public ReportScheduleResponse updateSchedule(Long id, ReportScheduleRequest request) {
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        schedule.setScheduleName(request.getScheduleName());
        schedule.setFrequency(ReportSchedule.Frequency.valueOf(request.getFrequency()));
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setDayOfMonth(request.getDayOfMonth());
        schedule.setTimeOfDay(request.getTimeOfDay());
        schedule.setEmailRecipients(request.getEmailRecipients());
        schedule.setIsActive(request.getIsActive());
        schedule.setUpdatedBy(user);
        schedule.setNextExecution(calculateNextExecution(request.getFrequency(), request.getTimeOfDay(), request.getDayOfWeek(), request.getDayOfMonth()));

        ReportSchedule updated = reportScheduleRepository.save(schedule);
        auditService.logSuccess("UPDATE", "REPORT_SCHEDULE", updated.getId(), "Updated schedule: " + request.getScheduleName());
        return ReportScheduleResponse.from(updated);
    }

    public void deleteSchedule(Long id) {
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        
        reportScheduleRepository.delete(schedule);
        auditService.logSuccess("DELETE", "REPORT_SCHEDULE", id, "Deleted schedule: " + schedule.getScheduleName());
    }

    public List<ReportSchedule> getPendingSchedules() {
        return reportScheduleRepository.findByIsActiveTrueAndNextExecutionBefore(LocalDateTime.now());
    }

    private LocalDateTime calculateNextExecution(String frequency, LocalTime timeOfDay, Integer dayOfWeek, Integer dayOfMonth) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecution = now.with(timeOfDay);

        if (nextExecution.isBefore(now)) {
            nextExecution = nextExecution.plusDays(1);
        }

        switch (frequency) {
            case "DAILY":
                // Next execution is tomorrow at the specified time
                if (nextExecution.isBefore(now)) {
                    nextExecution = now.plusDays(1).with(timeOfDay);
                }
                break;

            case "WEEKLY":
                // Next execution is on the specified day of week
                if (dayOfWeek != null) {
                    LocalDateTime target = nextExecution.with(TemporalAdjusters.next(DayOfWeek.of(dayOfWeek + 1)));
                    nextExecution = target.with(timeOfDay);
                }
                break;

            case "MONTHLY":
                // Next execution is on the specified day of month
                if (dayOfMonth != null) {
                    LocalDateTime target = nextExecution.withDayOfMonth(dayOfMonth);
                    if (target.isBefore(now)) {
                        target = target.plusMonths(1);
                    }
                    nextExecution = target.with(timeOfDay);
                }
                break;

            case "QUARTERLY":
                // Every 3 months on the specified day
                if (dayOfMonth != null) {
                    LocalDateTime target = nextExecution.withDayOfMonth(dayOfMonth);
                    if (target.isBefore(now)) {
                        target = target.plusMonths(3);
                    }
                    nextExecution = target.with(timeOfDay);
                }
                break;

            case "ANNUALLY":
                // Once a year on the specified day
                if (dayOfMonth != null) {
                    LocalDateTime target = nextExecution.withDayOfMonth(dayOfMonth);
                    if (target.isBefore(now)) {
                        target = target.plusYears(1);
                    }
                    nextExecution = target.with(timeOfDay);
                }
                break;
        }

        return nextExecution;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
