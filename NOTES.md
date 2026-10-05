# Notes

## Summary of Changes

- **Fixed SQL Operator Precedence**: Added parentheses around `(LOWER(title) LIKE :term OR LOWER(description) LIKE :term)` in `TaskRepository.java` and `task_search_pkg.sql`. This stops archived records from leaking and ensures status filtering is consistently enforced.
- **Removed Thread Blocking**: Eliminated artificial `Thread.sleep()` in `TaskController.java` that was blocking servlet threads for up to 1 second on short queries.
- **Safe Status Parsing**: Added validation and exception handling for `TaskStatus.valueOf()` to prevent unhandled 500 errors on invalid inputs or "ALL".
- **Pagination State Reset**: Updated `App.jsx` to reset the active page to 1 whenever search terms or status filters change, preventing blank screens caused by out-of-range page offsets.

## What I Chose Not to Change

- **In-Memory subList Pagination**: Retained `allResults.subList()` in `TaskController` to maintain existing method signatures and keep the diff minimal within the 90-minute timebox.
- **Frontend State Architecture**: Kept the existing custom hook and component tree rather than introducing external state management libraries.

## Biggest Remaining Risk

- **Database Scalability**: The backend loads all matched records into JVM memory before slicing with `subList`. As the dataset expands, full table scans via `%term%` and large in-memory result sets will drive high memory pressure and risk `OutOfMemoryError`. Switching to SQL `LIMIT`/`OFFSET` (or Spring Data `Pageable`) is required for production scale.

## Tools & AI Used

- Used AI to inspect SQL operator precedence boundaries across H2 and Oracle dialects, and verify clean exception handling patterns for Java Enums. All fixes were manually validated against local endpoints.
