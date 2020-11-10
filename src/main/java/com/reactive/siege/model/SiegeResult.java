package com.reactive.siege.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Duration;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
public class SiegeResult {

    @JsonFormat(shape = STRING)
    private final Duration timeTaken;
    private final long averageTps;
    private final long totalRequestSent;
    private final long totalPayloadSizeKB;
}
