package com.village.bellevue.entity;

import org.springframework.data.annotation.Immutable;

import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Immutable
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "scrubbed_user")
public class ScrubbedUserEntity {

    @Id
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private UserStatus status = UserStatus.OFFLINE;

    @Column(nullable = false)
    private AvatarType avatar = AvatarType.CAT;

    public ScrubbedUserEntity(UserEntity fullUserEntity) {
        this(
                fullUserEntity.getId(),
                fullUserEntity.getName(),
                fullUserEntity.getUsername(),
                fullUserEntity.getStatus(),
                fullUserEntity.getAvatar()
        );
    }
}
