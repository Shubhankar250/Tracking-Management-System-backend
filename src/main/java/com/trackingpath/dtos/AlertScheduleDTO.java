package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertScheduleDTO {

    private Long id;
    private Boolean status;

    private Long alertId;

    private List<SlotDTO> data; // Assuming SlotBean is also converted to SlotDTO
}

