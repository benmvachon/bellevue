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
      "LEFT JOIN FriendEntity forumFriend ON p.forum.user = forumFriend.friend.id AND forumFriend.user = :user " +
      "LEFT JOIN FriendEntity postFriend ON p.user.id = postFriend.friend.id AND postFriend.user = :user " +
      "WHERE p.forum.id = :forum " +
      "AND p.parent IS NULL " +
      "AND (p.forum.user IS NULL OR p.forum.user = :user OR forumFriend.status = 'accepted') " +
      "AND (p.user.id = :user OR postFriend.status = 'accepted') " +
      "ORDER BY p.created DESC")
  Page<PostEntity> findRecentTopLevelByForum(@Param("user") Long user, @Param("forum") Long forum, Pageable pageable);

  @Query(
    value = """
      SELECT p.* FROM post p
      LEFT JOIN forum f ON p.forum = f.id
      LEFT JOIN friend ff ON f.user = ff.friend AND ff.user = :user AND ff.status = 'accepted'
      LEFT JOIN friend pf ON p.user = pf.friend AND pf.user = :user AND pf.status = 'accepted'
      LEFT JOIN (
          SELECT parent, COUNT(*) AS child_count
          FROM post
          WHERE parent IS NOT NULL
          GROUP BY parent
      ) child_counts ON p.id = child_counts.parent
      LEFT JOIN aggregate_rating ar ON ar.post = p.id AND ar.user = :user
      WHERE p.forum = :forum
        AND p.parent IS NULL
        AND (f.user IS NULL OR f.user = :user OR ff.status = 'accepted')
        AND (p.user = :user OR pf.status = 'accepted')
      ORDER BY IFNULL(child_counts.child_count, 0) + IFNULL(ar.rating_count, 0) DESC
      """,
    countQuery = """
      SELECT COUNT(*) FROM post p
      LEFT JOIN forum f ON p.forum = f.id
      LEFT JOIN friend ff ON f.user = ff.friend AND ff.user = :user AND ff.status = 'accepted'
      LEFT JOIN friend pf ON p.user = pf.friend AND pf.user = :user AND pf.status = 'accepted'
      WHERE p.forum = :forum
        AND p.parent IS NULL
        AND (f.user IS NULL OR f.user = :user OR ff.status = 'accepted')
        AND (p.user = :user OR pf.status = 'accepted')
      """,
    nativeQuery = true
  )
  Page<PostEntity> findRelevantTopLevelByForum(@Param("user") Long user, @Param("forum") Long forum, Pageable pageable);

  @Query(
      "SELECT p FROM PostEntity p " +
      "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
      "WHERE p.parent.id = :post " +
      "AND (p.user.id = :user OR f.status = 'accepted') " +
      "ORDER BY p.created DESC")
  Page<PostEntity> findRecentChildren(@Param("user") Long user, @Param("post") Long post, Pageable pageable);

  @Query(
    value = """
      SELECT p.* FROM post p
      LEFT JOIN friend f ON p.user = f.friend AND f.user = :user AND f.status = 'accepted'
      LEFT JOIN (
          SELECT parent, COUNT(*) AS child_count
          FROM post
          WHERE parent IS NOT NULL
          GROUP BY parent
      ) child_counts ON p.id = child_counts.parent
      LEFT JOIN aggregate_rating ar ON ar.post = p.id AND ar.user = :user
      WHERE p.parent = :post
      AND (p.user = :user OR f.status = 'accepted')
      ORDER BY IFNULL(child_counts.child_count, 0) + IFNULL(ar.rating_count, 0) DESC
      """,
    countQuery = """
      SELECT COUNT(*) FROM post p
      LEFT JOIN friend f ON p.user = f.friend AND f.user = :user AND f.status = 'accepted'
      WHERE p.parent = :post
      AND (p.user = :user OR f.status = 'accepted')
      """,
    nativeQuery = true
  )
  Page<PostEntity> findRelevantChildren(@Param("user") Long user, @Param("post") Long post, Pageable pageable);
  

  @Query(
    "SELECT COUNT(p) FROM PostEntity p " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "WHERE p.parent.id = :post " +
    "AND (p.user.id = :user OR f.status = 'accepted')")
  Long countChildren(@Param("user") Long user, @Param("post") Long post);

  @Query(
      "SELECT p.user.id FROM PostEntity p " +
      "JOIN FriendEntity f ON f.friend.id = p.user.id " +
      "WHERE (p.user.id = :user OR f.user = :user) " +
      "AND f.status = 'accepted' " +
      "AND p.id = :post")
  Long getAuthor(@Param("post") Long post, @Param("user") Long user);

  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
      "FROM PostEntity p " +
      "JOIN FriendEntity f ON f.friend.id = p.user.id " +
      "WHERE (p.user.id = :user OR f.user = :user) " +
      "AND f.status = 'accepted' " +
      "AND p.id = :post")
  Boolean canRead(@Param("post") Long post, @Param("user") Long user);

  @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.user.id = :user AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Integer countAllPostsByUserContainingKeyword(@Param("user") Long user, @Param("keyword") String keyword);

  @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.user.id = :user")
  Integer countAllPostsByUser(@Param("user") Long user);

}
