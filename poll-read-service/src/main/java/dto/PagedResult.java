package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record PagedResult<T>(
        List<T> content,
        long totalElements,
        int page,
        int size,
        boolean hasNext
) {}
