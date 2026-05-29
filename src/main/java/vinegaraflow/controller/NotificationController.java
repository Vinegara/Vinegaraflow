package vinegaraflow.controller;

import vinegaraflow.dto.NotificationRequest;
import vinegaraflow.entity.Notification;
import vinegaraflow.entity.User;
import vinegaraflow.repository.NotificationRepository;
import vinegaraflow.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Notification> getMyNotifications(Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByRecipientId(currentUser.getId());
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByRecipientIdAndIsReadFalse(currentUser.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Notification createNotification(@Valid @RequestBody NotificationRequest request) {
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(request.message())
                .type(request.type())
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id, Authentication authentication) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!notification.getRecipient().getId().equals(currentUser.getId()) &&
                authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id, Authentication authentication) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!notification.getRecipient().getId().equals(currentUser.getId()) &&
                authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }
        notificationRepository.deleteById(id);
    }
}