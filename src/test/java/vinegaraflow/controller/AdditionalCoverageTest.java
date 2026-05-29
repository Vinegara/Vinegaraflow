package vinegaraflow.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import vinegaraflow.dto.CommentRequest;
import vinegaraflow.dto.NotificationRequest;
import vinegaraflow.dto.ProjectMemberRequest;
import vinegaraflow.entity.Attachment;
import vinegaraflow.entity.Comment;
import vinegaraflow.entity.Notification;
import vinegaraflow.entity.Project;
import vinegaraflow.entity.ProjectMember;
import vinegaraflow.entity.ProjectRole;
import vinegaraflow.entity.Task;
import vinegaraflow.entity.User;
import vinegaraflow.repository.AttachmentRepository;
import vinegaraflow.repository.CommentRepository;
import vinegaraflow.repository.NotificationRepository;
import vinegaraflow.repository.ProjectMemberRepository;
import vinegaraflow.repository.ProjectRepository;
import vinegaraflow.repository.TaskRepository;
import vinegaraflow.repository.UserRepository;
import vinegaraflow.service.AttachmentService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdditionalCoverageTest {

    @TempDir
    Path tempDir;

    private Authentication auth(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new TestingAuthenticationToken(username, "password", authorities);
    }

    private User user(long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@test.local")
                .password("encoded")
                .enabled(true)
                .build();
    }

    @Test
    void userControllerReadsAndDeletesUsers() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = user(1L, "admin");
        UserController controller = new UserController(userRepository);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(controller.getAllUsers()).containsExactly(user);
        assertThat(controller.getUserById(1L)).isEqualTo(user);

        controller.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void commentControllerCoversCrudAndAccessRules() {
        CommentRepository commentRepository = mock(CommentRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentController controller = new CommentController(commentRepository, taskRepository, userRepository);

        User author = user(1L, "author");
        User stranger = user(2L, "stranger");
        Task task = Task.builder().id(5L).title("Task").createdBy(author).build();
        Comment saved = Comment.builder().id(10L).text("old").author(author).task(task).build();

        when(commentRepository.findAll()).thenReturn(List.of(saved));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(controller.getAllComments()).containsExactly(saved);
        assertThat(controller.getCommentById(10L)).isEqualTo(saved);

        Comment created = controller.createComment(new CommentRequest("created", 5L), auth("author", "USER"));
        assertThat(created.getText()).isEqualTo("created");
        assertThat(created.getAuthor()).isEqualTo(author);
        assertThat(created.getTask()).isEqualTo(task);

        Comment updated = controller.updateComment(10L, new CommentRequest("new", 5L), auth("author", "USER"));
        assertThat(updated.getText()).isEqualTo("new");

        controller.deleteComment(10L, auth("author", "USER"));
        verify(commentRepository).deleteById(10L);

        assertThatThrownBy(() -> controller.updateComment(10L, new CommentRequest("bad", 5L), auth("stranger", "USER")))
                .hasMessage("Access denied");
        controller.deleteComment(10L, auth("admin", "ADMIN"));
        verify(commentRepository, times(2)).deleteById(10L);
    }

    @Test
    void notificationControllerCoversQueriesCreateUpdateAndDelete() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        NotificationController controller = new NotificationController(notificationRepository, userRepository);

        User recipient = user(7L, "recipient");
        User other = user(8L, "other");
        Notification notification = Notification.builder()
                .id(3L)
                .recipient(recipient)
                .message("hello")
                .type("TASK")
                .isRead(false)
                .build();

        when(userRepository.findByUsername("recipient")).thenReturn(Optional.of(recipient));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user(1L, "admin")));
        when(userRepository.findById(7L)).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByRecipientId(7L)).thenReturn(List.of(notification));
        when(notificationRepository.findByRecipientIdAndIsReadFalse(7L)).thenReturn(List.of(notification));
        when(notificationRepository.findById(3L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(controller.getMyNotifications(auth("recipient", "USER"))).containsExactly(notification);
        assertThat(controller.getUnreadNotifications(auth("recipient", "USER"))).containsExactly(notification);

        Notification created = controller.createNotification(new NotificationRequest(7L, "created", "INFO"));
        assertThat(created.getRecipient()).isEqualTo(recipient);
        assertThat(created.getIsRead()).isFalse();

        Notification read = controller.markAsRead(3L, auth("recipient", "USER"));
        assertThat(read.getIsRead()).isTrue();

        controller.deleteNotification(3L, auth("recipient", "USER"));
        verify(notificationRepository).deleteById(3L);

        assertThatThrownBy(() -> controller.markAsRead(3L, auth("other", "USER")))
                .hasMessage("Access denied");
        controller.deleteNotification(3L, auth("admin", "ADMIN"));
        verify(notificationRepository, times(2)).deleteById(3L);
    }

    @Test
    void projectMemberControllerAddsListsAndRejectsDuplicateMember() {
        ProjectMemberRepository memberRepository = mock(ProjectMemberRepository.class);
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProjectMemberController controller = new ProjectMemberController(memberRepository, projectRepository, userRepository);

        User user = user(11L, "member");
        Project project = Project.builder().id(22L).name("Project").owner(user).build();
        ProjectMember member = ProjectMember.builder()
                .id(33L)
                .user(user)
                .project(project)
                .role(ProjectRole.DEVELOPER)
                .build();

        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(memberRepository.findByProjectId(22L)).thenReturn(List.of(member));
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
        when(projectRepository.findById(22L)).thenReturn(Optional.of(project));
        when(memberRepository.existsByUserIdAndProjectId(11L, 22L)).thenReturn(false, true);
        when(memberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(controller.getAllMembers()).containsExactly(member);
        assertThat(controller.getMembersByProject(22L)).containsExactly(member);

        ProjectMember created = controller.addMember(new ProjectMemberRequest(11L, 22L, ProjectRole.DEVELOPER));
        assertThat(created.getUser()).isEqualTo(user);
        assertThat(created.getProject()).isEqualTo(project);

        assertThatThrownBy(() -> controller.addMember(new ProjectMemberRequest(11L, 22L, ProjectRole.TESTER)))
                .hasMessage("Member already exists");

        controller.removeMember(33L);
        verify(memberRepository).deleteById(33L);
    }

    @Test
    void attachmentControllerAndServiceCoverUploadDownloadDelete() throws Exception {
        AttachmentRepository attachmentRepository = mock(AttachmentRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AttachmentService attachmentService = new AttachmentService(attachmentRepository);
        ReflectionTestUtils.setField(attachmentService, "uploadDir", tempDir.toString());
        AttachmentController controller = new AttachmentController(
                attachmentRepository,
                taskRepository,
                userRepository,
                attachmentService
        );

        User uploader = user(1L, "uploader");
        User stranger = user(2L, "stranger");
        Task task = Task.builder().id(9L).title("Task").createdBy(uploader).build();
        MultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "hello".getBytes());

        when(userRepository.findByUsername("uploader")).thenReturn(Optional.of(uploader));
        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));
        when(taskRepository.findById(9L)).thenReturn(Optional.of(task));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment attachment = invocation.getArgument(0);
            attachment.setId(44L);
            return attachment;
        });

        Attachment uploaded = controller.uploadAttachment(file, 9L, auth("uploader", "USER"));
        assertThat(uploaded.getFileName()).isEqualTo("note.txt");
        assertThat(uploaded.getTask()).isEqualTo(task);
        assertThat(Files.exists(Path.of(uploaded.getFilePath()))).isTrue();

        when(attachmentRepository.findAll()).thenReturn(List.of(uploaded));
        when(attachmentRepository.findById(44L)).thenReturn(Optional.of(uploaded));
        assertThat(controller.getAllAttachments()).containsExactly(uploaded);

        Resource downloaded = controller.downloadAttachment(44L).getBody();
        assertThat(downloaded).isNotNull();
        assertThat(downloaded.exists()).isTrue();

        assertThatThrownBy(() -> controller.deleteAttachment(44L, auth("stranger", "USER")))
                .hasMessage("Access denied");
        verify(attachmentRepository, never()).delete(uploaded);

        controller.deleteAttachment(44L, auth("uploader", "USER"));
        assertThat(Files.exists(Path.of(uploaded.getFilePath()))).isFalse();
        verify(attachmentRepository).delete(uploaded);
    }
}
