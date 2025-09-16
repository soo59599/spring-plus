package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.TodoSearchCond;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class TodoRepositoryQueryImpl implements TodoRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;
        Todo todoOptional = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(todoOptional);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(Pageable pageable, TodoSearchCond cond) {
        // 매니저 수
        JPQLQuery<Long> managerCount = JPAExpressions
                .select(manager.count())
                .from(manager)
                .where(manager.todo.eq(todo));

        //Comment 수
        JPQLQuery<Long> commentCount = JPAExpressions
                .select(comment.count())
                .from(comment)
                .where(comment.todo.eq(todo));

        List<TodoSearchResponse> todos = jpaQueryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        managerCount,
                        commentCount
                ))
                .from(todo)
                .leftJoin(todo.user, user) //닉네임 비교용
                .where(
                        titleContains(cond.getTitle()),
                        createdAfter(cond.getStartDate()),
                        nicknameContains(cond.getNickname())
                )
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(todo.count())
                .from(todo)
                .leftJoin(todo.user, user)
                .where(
                        titleContains(cond.getTitle()),
                        createdAfter(cond.getStartDate()),
                        nicknameContains(cond.getNickname())
                )
                .fetchOne();

        return new PageImpl<>(todos, pageable, totalCount != null ? totalCount : 0L);
    }

    private BooleanExpression titleContains(String title) {
        return title != null ? todo.title.contains(title) : null;
    }

    private BooleanExpression createdAfter(LocalDate startDate) {
        return startDate != null ? todo.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression nicknameContains(String nickname) {
        return nickname != null ? todo.user.nickname.contains(nickname) : null;
    }
}
