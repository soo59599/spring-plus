package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@NoArgsConstructor
public class Log  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requestUserId; // 일정 작성자

    private Long managerUserId; // 일정 작상자가 배치하는 유저 id

    private Long todoId; //배치 할 일정

    @Enumerated(EnumType.STRING)
    private LogResult result;   // "SUCCESS", "FAIL"


    public Log(Long requestUserId, Long managerUserId, Long todoId, LogResult result) {
        this.requestUserId = requestUserId;
        this.managerUserId = managerUserId;
        this.todoId = todoId;
        this.result = result;
    }

    @CreatedDate
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Getter
    public enum LogResult {
        SUCCESS("매니저 등록 성공"),
        FAIL("매니저 등록 실패");

        private final String message;

        LogResult(String message) {
            this.message = message;
        }
    }
}
