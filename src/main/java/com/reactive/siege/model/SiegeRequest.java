package com.reactive.siege.model;

import lombok.Data;

@Data
public class SiegeRequest {

    private final String url;
    private final Integer tps;
}
