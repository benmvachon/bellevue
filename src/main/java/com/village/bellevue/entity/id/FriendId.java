package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

import com.village.bellevue.entity.ScrubbedUserEntity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class FriendId implements Serializable {

    private Long user;
    private ScrubbedUserEntity friend;

    public FriendId(Long user, Long friend) {
        this.user = user;
        this.friend = new ScrubbedUserEntity();
        this.friend.setId(friend);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FriendId)) {
            return false;
        }
        FriendId that = (FriendId) o;
        return Objects.equals(user, that.user)
                && Objects.equals(friend.getId(), that.friend.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, friend.getId());
    }
}
