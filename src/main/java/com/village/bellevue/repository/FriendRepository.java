package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.id.FriendId;

@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, FriendId> {

    @Query("SELECT f FROM FriendEntity f WHERE f.user = :user AND status = 'accepted' AND f.friend.id NOT IN " +
        "(SELECT b.friend.id FROM FriendEntity b WHERE (b.user = :currentUser AND b.status = 'blocked_them') OR (b.user = :currentUser AND b.status = 'blocked_you'))")
    Page<FriendEntity> findFriendsExcludingBlocked(@Param("user") Long user, @Param("currentUser") Long currentUser, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FriendEntity f WHERE f.friend.id = :user AND f.user = :currentUser AND status = 'accepted'")
    Boolean areFriends(@Param("user") Long user, @Param("currentUser") Long currentUser);
}
