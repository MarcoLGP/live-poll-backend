package service;

import dto.NotificationDTO;
import entities.Notification;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationService {

    public List<NotificationDTO> listNotifications(Long receiverUserId, int page, int size) {
        return Notification.find("receiverUserId = ?1", Sort.by("createdAt").descending(), receiverUserId)
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(notification -> (Notification) notification)
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public long countNotifications(Long receiverUserId) {
        return Notification.count("receiverUserId", receiverUserId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long receiverUserId) {
        int updated = Notification.update(
                "read = true where id = ?1 and receiverUserId = ?2",
                notificationId, receiverUserId
        );
        if (updated == 0) {
            throw new NotFoundException("Notificação não encontrada");
        }
    }

    @Transactional
    public void markAllAsRead(Long receiverUserId) {
        Notification.update("read = true where receiverUserId = ?1", receiverUserId);
    }

    public long countUnread(Long receiverUserId) {
        return Notification.count("receiverUserId = ?1 and read = false", receiverUserId);
    }


}