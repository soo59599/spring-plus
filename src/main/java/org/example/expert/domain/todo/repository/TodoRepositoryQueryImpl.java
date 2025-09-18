package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.TodoSearchCond;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class TodoRepositoryQueryImpl implements TodoRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
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
        JPQLQuery<Long> managerCount = createCountSubquery(manager, manager.todo);

        //Comment 수
        JPQLQuery<Long> commentCount = createCountSubquery(comment, comment.todo);

        List<TodoSearchResponse> todos = applyPageable(
                createQuery(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        managerCount,
                        commentCount
                ), cond),
                pageable
        ).fetch();

        Long totalCount = countQuery(cond).fetchOne();

        return PageableExecutionUtils.getPage(todos, pageable, () -> totalCount != null ? totalCount : 0L);
    }

    // 쿼리용
    private <T> JPAQuery<T> createQuery(Expression<T> expr, TodoSearchCond cond) {
        return jpaQueryFactory
                .select(expr)
                .from(todo)
                .leftJoin(todo.user, user) // nickname 조건용
                .where(
                        titleContains(cond.getTitle()),
                        createdAfter(cond.getStartDate()),
                        nicknameContains(cond.getNickname())
                );
    }

    // count용
    private JPQLQuery<Long> countQuery(TodoSearchCond cond) {
        return createQuery(Wildcard.count, cond);
    }

    // Projections의 필드를 위한 갯수 세는 용도
    private JPQLQuery<Long> createCountSubquery(EntityPath<?> entityPath, QTodo foreignKeyToTodo) {
        return JPAExpressions
                .select(Wildcard.count)
                .from(entityPath)
                .where(foreignKeyToTodo.eq(todo));
    }

    //페이징
    private <T> JPAQuery<T> applyPageable(JPAQuery<T> query, Pageable pageable) {
        return query
                .orderBy(getDefaultOrder())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
    }

    private OrderSpecifier<?> getDefaultOrder() {
        return todo.createdAt.desc();
    }

    //조건 용
    private BooleanExpression titleContains(String title) {
        return title != null ? todo.title.contains(title) : null;
    }

    private BooleanExpression createdAfter(LocalDate startDate) {
        return startDate != null ? todo.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression nicknameContains(String nickname) {
        return nickname != null ?
                JPAExpressions
                        .selectOne()
                        .from(manager)
                        .where(
                                manager.todo.eq(todo)
                                        .and(manager.user.nickname.contains(nickname))
                        )
                        .exists() : null;
    }
}
