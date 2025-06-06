package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.village.bellevue.entity.NotificationSettingEntity;
import com.village.bellevue.entity.id.NotificationSettingId;

public interface NotificationSettingRepository extends JpaRepository<NotificationSettingEntity, NotificationSettingId> {
}
