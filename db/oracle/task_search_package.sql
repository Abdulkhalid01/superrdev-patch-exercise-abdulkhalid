CREATE OR REPLACE PACKAGE BODY task_search_pkg AS

    PROCEDURE search_tasks(
        p_search_term IN  VARCHAR2 DEFAULT NULL,
        p_status      IN  VARCHAR2 DEFAULT NULL,
        p_page        IN  NUMBER   DEFAULT 1,
        p_page_size   IN  NUMBER   DEFAULT 10,
        p_results     OUT task_cursor,
        p_total_count OUT NUMBER
    ) IS
        v_term   VARCHAR2(257);
        v_offset NUMBER;
    BEGIN
        v_term   := '%' || LOWER(NVL(p_search_term, '')) || '%';
        v_offset := (p_page - 1) * p_page_size;

        -- Total count for pagination metadata (FIXED: added parentheses around OR conditions)
        SELECT COUNT(*)
          INTO p_total_count
          FROM tasks
         WHERE archived = 0
           AND (LOWER(title) LIKE v_term OR LOWER(description) LIKE v_term)
           AND (p_status IS NULL OR status = p_status);

        -- Paginated results using ROWNUM (FIXED: added parentheses around OR conditions)
        OPEN p_results FOR
            SELECT id, title, description, status, priority, assignee, created_at
              FROM (
                  SELECT t.*, ROWNUM AS rn
                    FROM (
                        SELECT id, title, description, status, priority,
                               assignee, created_at
                          FROM tasks
                         WHERE archived = 0
                           AND (LOWER(title) LIKE v_term OR LOWER(description) LIKE v_term)
                           AND (p_status IS NULL OR status = p_status)
                         ORDER BY created_at DESC
                    ) t
                   WHERE ROWNUM <= v_offset + p_page_size
              )
             WHERE rn > v_offset;

    END search_tasks;

END task_search_pkg;
/