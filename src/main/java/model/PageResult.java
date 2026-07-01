package kz.aceflow.model;

import java.util.List;

/**
 * Paginated list result produced by the service layer.
 * Controllers pass these attributes to the view without computing pagination themselves.
 */
public record PageResult<T>(List<T> items, int currentPage, int totalPages, int totalItems) {

    public static <T> PageResult<T> of(List<T> items, int page, int pageSize, int totalItems) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        return new PageResult<>(items, page, totalPages, totalItems);
    }
}
