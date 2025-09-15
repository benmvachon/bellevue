package com.village.bellevue.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.PostEntity;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

  @Query("SELECT p.id FROM PostEntity p WHERE p.user.id = :user AND p.forum.id = :forum")
  @Transactional(readOnly = true)
  List<Long> findAllByUserInForum(@Param("user") Long user, @Param("forum") Long forum);

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user AND postFriend.status = 'ACCEPTED' " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = p.forum.id AND fs.user.id = :user " +
    "WHERE p.parent IS NULL " +
    "AND p.deleted = FALSE " +
    "AND (" +
      "(p.forum.user IS NULL AND (p.user.id = :user OR postFriend.id IS NOT NULL)) " +
      "OR (p.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL) " +
    ") " +
    "AND (:excludedForums IS NULL OR p.forum.id NOT IN :excludedForums) " +
    "AND (p.created < :createdCursor " +
      "OR (p.created = :createdCursor AND p.id < :idCursor)) " +
    "ORDER BY p.created DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findRecentTopLevel(
    @Param("user") Long user,
    @Param("excludedForums") List<Long> excludedForums,
    @Param("createdCursor") Timestamp createdCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user AND postFriend.status = 'ACCEPTED' " +
    "LEFT JOIN AggregateRatingEntity a ON a.post = p.id AND a.user = :user " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = p.forum.id AND fs.user.id = :user " +
    "WHERE p.parent IS NULL " +
    "AND p.deleted = FALSE " +
    "AND (" +
      "(p.forum.user IS NULL AND (p.user.id = :user OR postFriend.id IS NOT NULL)) " +
      "OR (p.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL) " +
    ") " +
    "AND (:excludedForums IS NULL OR p.forum.id NOT IN :excludedForums) " +
    "AND (a.popularity < :popularityCursor " +
    "OR (a.popularity = :popularityCursor AND p.id < :idCursor)) " +
    "ORDER BY a.popularity DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findPopularTopLevel(
    @Param("user") Long user,
    @Param("excludedForums") List<Long> excludedForums,
    @Param("popularityCursor") Long popularityCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT COUNT(DISTINCT(p.id)) FROM PostEntity p " +
    "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user AND postFriend.status = 'ACCEPTED' " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = p.forum.id AND fs.user.id = :user " +
    "WHERE p.parent IS NULL " +
    "AND (:excludedForums IS NULL OR p.forum.id NOT IN :excludedForums) " +
    "AND p.deleted = FALSE " +
    "AND (" +
      "(p.forum.user IS NULL AND (p.user.id = :user OR postFriend.id IS NOT NULL)) " +
      "OR (p.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL) " +
    ")"
  )
  @Transactional(readOnly = true)
  Long countTopLevel(@Param("user") Long user, @Param("excludedForums") List<Long> excludedForums);

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user AND postFriend.status = 'ACCEPTED' " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = p.forum.id AND fs.user.id = :user " +
    "WHERE p.forum.id = :forum " +
    "AND p.parent IS NULL " +
    "AND p.deleted = FALSE " +
    "AND (" +
      "(p.forum.user IS NULL AND (p.user.id = :user OR postFriend.id IS NOT NULL)) " +
      "OR (p.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL) " +
    ") " +
    "AND (p.created < :createdCursor " +
    "OR (p.created = :createdCursor AND p.id < :idCursor)) " +
    "ORDER BY p.created DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findRecentTopLevelByForum(
    @Param("user") Long user,
    @Param("forum") Long forum,
    @Param("createdCursor") Timestamp createdCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user AND postFriend.status = 'ACCEPTED' " +
    "LEFT JOIN AggregateRatingEntity a ON a.post = p.id AND a.user = :user " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = p.forum.id AND fs.user.id = :user " +
    "WHERE p.forum.id = :forum " +
    "AND p.parent IS NULL " +
    "AND p.deleted = FALSE " +
    "AND (" +
      "(p.forum.user IS NULL AND (p.user.id = :user OR postFriend.id IS NOT NULL)) " +
      "OR (p.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL) " +
    ") " +
    "AND (a.popularity < :popularityCursor " +
    "OR (a.popularity = :popularityCursor AND p.id < :idCursor)) " +
    "ORDER BY a.popularity DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findPopularTopLevelByForum(
    @Param("user") Long user,
    @Param("forum") Long forum,
    @Param("popularityCursor") Long popularityCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT COUNT(p) FROM PostEntity p " +
    "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user AND postFriend.status = 'ACCEPTED' " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = p.forum.id AND fs.user.id = :user " +
    "WHERE p.forum.id = :forum " +
    "AND p.parent IS NULL " +
    "AND p.deleted = FALSE " +
    "AND (" +
      "(p.forum.user IS NULL AND (p.user.id = :user OR postFriend.id IS NOT NULL)) " +
      "OR (p.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL) " +
    ")"
  )
  @Transactional(readOnly = true)
  Long countTopLevelByForum(
    @Param("user") Long user,
    @Param("forum") Long forum
  );

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "WHERE p.parent.id = :post " +
    "AND p.deleted = FALSE " +
    "AND (p.user.id = :user OR f.status = 'ACCEPTED' OR p.forum.user IS NOT NULL) " +
    "AND (p.created < :createdCursor " +
    "OR (p.created = :createdCursor AND p.id < :idCursor)) " +
    "ORDER BY p.created DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findRecentChildren(
    @Param("user") Long user,
    @Param("post") Long post,
    @Param("createdCursor") Timestamp createdCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "LEFT JOIN AggregateRatingEntity a ON a.post = p.id AND a.user = :user " +
    "WHERE p.parent.id = :post " +
    "AND p.deleted = FALSE " +
    "AND (p.user.id = :user OR f.status = 'ACCEPTED' OR p.forum.user IS NOT NULL) " +
    "AND (a.popularity < :popularityCursor " +
    "OR (a.popularity = :popularityCursor AND p.id < :idCursor)) " +
    "ORDER BY a.popularity DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findPopularChildren(
    @Param("user") Long user,
    @Param("post") Long post,
    @Param("popularityCursor") Long popularityCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT COUNT(p) FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "WHERE p.parent.id = :post " +
    "AND p.deleted = FALSE " +
    "AND (p.user.id = :user OR f.status = 'ACCEPTED' OR p.forum.user IS NOT NULL)"
  )
  @Transactional(readOnly = true)
  Long countChildren(@Param("user") Long user, @Param("post") Long post);

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "WHERE p.parent.id = :post AND p.id != :child " +
    "AND p.deleted = FALSE " +
    "AND (p.user.id = :user OR f.status = 'ACCEPTED' OR p.forum.user IS NOT NULL) " +
    "AND (p.created < :createdCursor " +
    "OR (p.created = :createdCursor AND p.id < :idCursor)) " +
    "ORDER BY p.created DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findRecentOtherChildren(
    @Param("user") Long user,
    @Param("post") Long post,
    @Param("child") Long child,
    @Param("createdCursor") Timestamp createdCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT p FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "LEFT JOIN AggregateRatingEntity a ON a.post = p.id AND a.user = :user " +
    "WHERE p.parent.id = :post AND p.id != :child " +
    "AND p.deleted = FALSE " +
    "AND (p.user.id = :user OR f.status = 'ACCEPTED' OR p.forum.user IS NOT NULL) " +
    "AND (a.popularity < :popularityCursor " +
    "OR (a.popularity = :popularityCursor AND p.id < :idCursor)) " +
    "ORDER BY a.popularity DESC, p.id DESC " +
    "LIMIT :limit"
  )
  @Transactional(readOnly = true)
  List<PostEntity> findPopularOtherChildren(
    @Param("user") Long user,
    @Param("post") Long post,
    @Param("child") Long child,
    @Param("popularityCursor") Long popularityCursor,
    @Param("idCursor") Long idCursor,
    @Param("limit") Long limit
  );

  @Query(
    "SELECT COUNT(p) FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "WHERE p.parent.id = :post AND p.id != :child " +
    "AND p.deleted = FALSE " +
    "AND (p.user.id = :user OR f.status = 'ACCEPTED' OR p.forum.user IS NOT NULL)"
  )
  @Transactional(readOnly = true)
  Long countOtherChildren(@Param("user") Long user, @Param("post") Long post, @Param("child") Long child);

  @Query(
    "SELECT p.user.id FROM PostEntity p " +
    "WHERE p.deleted = FALSE " +
    "AND p.id = :post"
  )
  @Transactional(readOnly = true)
  Long getAuthor(@Param("post") Long post, @Param("user") Long user);

  @Query(
    "SELECT p.forum.id FROM PostEntity p " +
    "WHERE p.deleted = FALSE " +
    "AND p.id = :post"
  )
  @Transactional(readOnly = true)
  Long getForum(@Param("post") Long post, @Param("user") Long user);

  @Query(
    "SELECT CASE WHEN p.forum.user IS NOT NULL THEN true ELSE false END FROM PostEntity p " +
    "WHERE p.deleted = FALSE " +
    "AND p.id = :post"
  )
  @Transactional(readOnly = true)
  Boolean isCustomForum(@Param("post") Long post, @Param("user") Long user);

  @Query(
    "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
    "FROM PostEntity p " +
    "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user AND forumFriend.status = 'ACCEPTED' " +
    "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user AND postFriend.status = 'ACCEPTED' " +
    "LEFT JOIN ForumSecurityEntity fs ON fs.forum = p.forum.id AND fs.user.id = :user " +
    "WHERE p.id = :post " +
    "AND p.deleted = FALSE " +
    "AND (" +
      "p.user.id = :user " +
      "OR (p.forum.user IS NULL AND postFriend.id IS NOT NULL) " +
      "OR (p.forum.user = :user) " +
      "OR (forumFriend.id IS NOT NULL AND fs.user.id IS NOT NULL)" +
    ")"
  )
  @Transactional(readOnly = true)
  Boolean canRead(@Param("post") Long post, @Param("user") Long user);

  @Query(
    "SELECT p.parent.id " +
    "FROM PostEntity p " +
    "JOIN FriendEntity f ON f.friend.id = p.user.id " +
    "WHERE (p.user.id = :user OR f.user = :user OR p.forum.user IS NOT NULL) " +
    "AND p.deleted = FALSE " +
    "AND f.status = 'ACCEPTED' " +
    "AND p.id = :post"
  )
  @Transactional(readOnly = true)
  Long getParent(@Param("post") Long post, @Param("user") Long user);

  @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.user.id = :user AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.deleted = FALSE")
  @Transactional(readOnly = true)
  Integer countAllPostsByUserContainingKeyword(@Param("user") Long user, @Param("keyword") String keyword);

  @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.user.id = :user AND p.deleted = FALSE")
  @Transactional(readOnly = true)
  Integer countAllPostsByUser(@Param("user") Long user);

}
