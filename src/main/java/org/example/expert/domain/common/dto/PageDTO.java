package org.example.expert.domain.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
@Builder
public class PageDTO {
    private final Integer currentPage;
    private final Integer size;
    private String sortBy;

    public Pageable toPageable() {

        if (sortBy == null) {
            return PageRequest.of(currentPage - 1, size);
        } else {
            return PageRequest.of(currentPage - 1, size, Sort.by(sortBy).descending());
        }
    }

    public Pageable toPageable(String sortBy) {
        return PageRequest.of(currentPage - 1, size, Sort.by(sortBy).descending());
    }
}
