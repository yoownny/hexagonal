package com.ahnlab.edr.sample.out.opensearch.store;

import lombok.Data;

/**
 * OpenSearch에 저장되는 이벤트 엔티티.
 */
@Data
public class EventEntity {

    private String id;
    private String message;
}
