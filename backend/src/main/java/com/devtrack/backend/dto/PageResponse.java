package com.devtrack.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated response containing a list of resources and pagination metadata")
public class PageResponse<T> {

    @Schema(
            description = "Resources contained in the current page"
    )
    private List<T> content;
    @Schema(
            description = "Current page number, starting from 0",
            example = "0"
    )
    private int page;
    @Schema(
            description = "Maximum number of elements per page",
            example = "20"
    )
    private int size;
    @Schema(
            description = "Total number of elements matching the request",
            example = "42"
    )
    private long totalElements;
    @Schema(
            description = "Total number of available pages",
            example = "3"
    )
    private int totalPages;

    public PageResponse(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {

        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<T> getContent() { return content;}
    public int getPage() { return page;}
    public int getSize() { return size;}
    public long getTotalElements() { return totalElements;}
    public int getTotalPages() { return totalPages;}
}
