package dto;

import entities.Notification;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;

@RegisterForReflection
public record NotificationDTO(
        Long id,
        Long receiverUserId,
        Long actorUserId,
        String actorUsername,
        String type,
        Long referenceId,
        String referenceTitle,
        String subReferenceTitle,
        LocalDateTime createdAt,
        boolean read
) {
    public static NotificationDTO fromEntity(Notification notif) {
        return new NotificationDTO(
                notif.id,
                notif.receiverUserId,
                notif.actorUserId,
                notif.actorUsername,
                notif.type,
                notif.referenceId,
                notif.referenceTitle,
                notif.subReferenceTitle,
                notif.createdAt,
                notif.read
        );
    }
}