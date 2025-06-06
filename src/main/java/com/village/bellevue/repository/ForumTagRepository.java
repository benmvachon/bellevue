package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.ForumTagEntity;
import com.village.bellevue.entity.id.ForumTagId;

public interface ForumTagRepository extends JpaRepository<ForumTagEntity, ForumTagId> {
  @Query(
    "SELECT DISTINCT(t.tag) FROM ForumTagEntity t " +
    "LEFT JOIN FriendEntity forumFriend ON t.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = t.forum.id AND fs.user.id = :user " +
    "WHERE LOWER(t.tag) LIKE LOWER(CONCAT(:prefix, '%')) " +
    "AND (" +
      "(t.forum.user IS NULL) " +
      "OR (t.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL) " +
    ") " +
    "ORDER BY t.tag ASC"
  )
  @Transactional(readOnly = true)
  Page<String> findByPrefix(@Param("user") Long user, @Param("prefix") String prefix, Pageable pageable);
}
