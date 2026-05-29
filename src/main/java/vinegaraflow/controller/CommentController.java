package vinegaraflow.controller;

import vinegaraflow.dto.CommentRequest;
import vinegaraflow.entity.Comment;
import vinegaraflow.entity.Task;
import vinegaraflow.entity.User;
import vinegaraflow.repository.CommentRepository;
import vinegaraflow.repository.TaskRepository;
import vinegaraflow.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CommentController {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Comment getCommentById(@PathVariable Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @PostMapping
    public Comment createComment(@Valid @RequestBody CommentRequest request,
                                 Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Comment comment = Comment.builder()
                .text(request.text())
                .task(task)
                .author(currentUser)
                .build();
        return commentRepository.save(comment);
    }

    @PutMapping("/{id}")
    public Comment updateComment(@PathVariable Long id,
                                 @Valid @RequestBody CommentRequest request,
                                 Authentication authentication) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        String currentUser = authentication.getName();
        if (!comment.getAuthor().getUsername().equals(currentUser) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }
        comment.setText(request.text());
        // taskId игнорируем при обновлении
        return commentRepository.save(comment);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id, Authentication authentication) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        String currentUser = authentication.getName();
        if (!comment.getAuthor().getUsername().equals(currentUser) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Access denied");
        }
        commentRepository.deleteById(id);
    }
}