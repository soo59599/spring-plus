package org.example.expert.domain.todo.repository;

import java.time.LocalDateTime;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> ,  TodoRepositoryQuery {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            SELECT t FROM Todo t
                    WHERE (:weather IS NULL OR t.weather = :weather)
                            AND(:startDate IS NULL OR t.modifiedAt >= :startDate)
                            AND(:endDate IS NULL OR t.modifiedAt <= :endDate)
            ORDER BY t.modifiedAt DESC
        """)
    Page<Todo> searchTasks(Pageable pageable,
                           @Param("weather") String weather,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);

}
