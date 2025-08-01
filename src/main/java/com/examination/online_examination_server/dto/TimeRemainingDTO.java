package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TimeRemainingDTO {
    private long minutesRemaining;
    private long secondsRemaining;
    private long totalSecondsRemaining;
    private boolean timeExpired;
}
