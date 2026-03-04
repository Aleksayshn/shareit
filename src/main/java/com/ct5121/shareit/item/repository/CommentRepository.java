package com.ct5121.shareit.item.repository;

import com.ct5121.shareit.item.model.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

    @EntityGraph(attributePaths = {"author", "item"})
    List<Comment> findByItemIdInOrderByItemIdAscCreatedDesc(Collection<Long> itemIds);
}
