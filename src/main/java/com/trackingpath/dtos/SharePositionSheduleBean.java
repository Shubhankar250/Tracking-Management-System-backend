package com.trackingpath.dtos;

import java.util.List;
import lombok.Data;

@Data
public class SharePositionSheduleBean {

    private Long id;
    private boolean status;

    private Long sharePositionId;

    private List<SlotBean> data;
}
