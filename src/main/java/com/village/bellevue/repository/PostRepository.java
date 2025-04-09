package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.PostEntity;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

  @Query(
      "SELECT p FROM PostEntity p " +
      "LEFT JOIN FriendEntity forumFriend ON p.forum.user.id = forumFriend.friend.id AND forumFriend.user = :user " +
      "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user " +
      "WHERE p.forum.id = :forum " +
      "AND p.parent IS NULL " +
      "AND (p.forum.user IS NULL OR p.forum.user.id = :user OR forumFriend.status = 'accepted') " +
      "AND (p.user.id = :user OR postFriend.status = 'accepted') " +
      "ORDER BY p.created DESC")
  Page<PostEntity> findAllTopLevelByForum(@Param("user") Long user, @Param("forum") Long forum, Pageable pageable);

  @Query(
      "SELECT p FROM PostEntity p " +
      "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
      "WHERE p.parent.id = :post " +
      "AND (p.user.id = :user OR f.status = 'accepted') " +
      "ORDER BY p.created ASC")
  Page<PostEntity> findAllChildren(@Param("user") Long user, @Param("post") Long post, Pageable pageable);

  @Query(
    "SELECT COUNT(p) FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "WHERE p.parent.id = :post " +
    "AND (p.user.id = :user OR f.status = 'accepted')")
  long countChildren(@Param("user") Long user, @Param("post") Long post);

  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
      "FROM PostEntity p " +
      "JOIN FriendEntity f ON f.friend.id = p.user.id " +
      "WHERE (p.user.id = :user OR f.user = :user) " +
      "AND f.status = 'accepted' " +
      "AND p.id = :post")
  boolean canRead(@Param("post") Long post, @Param("user") Long user);
}
