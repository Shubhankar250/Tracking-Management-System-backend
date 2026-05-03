package com.trackingpath.mapper;
import org.springframework.stereotype.Component;

import com.trackingpath.dtos.ReportDTO;
import com.trackingpath.entities.ReportEntity;
import com.trackingpath.entities.Users;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.stream.Collectors;

@Component
public class ReportMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /* ===============================
       DTO -> ENTITY (Insert / Update)
       =============================== */
    public ReportEntity toEntity(ReportDTO dto, Users user) {

        String deviceStr = dto.getDevices() != null
                ? dto.getDevices().stream().map(String::valueOf).collect(Collectors.joining(","))
                : null;

        String geofenceStr = dto.getGeofences() != null
                ? dto.getGeofences().stream().map(String::valueOf).collect(Collectors.joining(","))
                : null;

        String skipCols = dto.getSkip_column() != null
                ? String.join(",", dto.getSkip_column())
                : null;

        return ReportEntity.builder()
                .title(dto.getTitle())
                .type(dto.getType())
                .format(dto.getFormat())
                .period(dto.getPeriod())
                .devices(deviceStr)
                .geofences(geofenceStr)
                .emails(dto.getEmails())
                .speed_limit(dto.getSpeed_limit())
                .stops(dto.getStops())
                .daily(dto.getDaily())
                .weekly(dto.getWeekly())
                .monthly(dto.getMonthly())
                .skip_column(skipCols)
                .adminId(user.getAdminId())
                .userId(user.getId())
                .from_date(dto.getFrom_date())
                .to_date(dto.getTo_date())
                .build();
    }

    /* ====================================
       DTO -> DTO (Calculate Period Dates)
       ==================================== */
    public ReportDTO mapWithPeriodDates(ReportDTO r) {

        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        LocalDate toDate;

        switch (r.getPeriod()) {
            case "today":
                fromDate = toDate = today;
                break;

            case "yesterday":
                fromDate = today.minusDays(1);
                toDate = today;
                break;

            case "2_days":
                fromDate = today.minusDays(2);
                toDate = today;
                break;

            case "3_days":
                fromDate = today.minusDays(3);
                toDate = today;
                break;

            case "this_week":
                fromDate = today.with(DayOfWeek.MONDAY);
                toDate = today;
                break;

            case "last_week":
                fromDate = today.minusWeeks(1).with(DayOfWeek.MONDAY);
                toDate = fromDate.plusDays(6);
                break;

            case "this_month":
                fromDate = today.withDayOfMonth(1);
                toDate = today;
                break;

            case "last_month":
                fromDate = today.minusMonths(1).withDayOfMonth(1);
                toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());
                break;

            default:
                fromDate = toDate = today;
        }

        ReportDTO dto = new ReportDTO();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setType(r.getType());
        dto.setFormat(r.getFormat());
        dto.setPeriod(r.getPeriod());
        dto.setFrom_date(fromDate.format(FORMATTER));
        dto.setTo_date(toDate.format(FORMATTER));

        return dto;
    }
}
