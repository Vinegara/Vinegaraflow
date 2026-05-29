package vinegaraflow.controller;

import vinegaraflow.dto.ProjectMemberRequest;
import vinegaraflow.entity.Project;
import vinegaraflow.entity.ProjectMember;
import vinegaraflow.entity.User;
import vinegaraflow.repository.ProjectMemberRepository;
import vinegaraflow.repository.ProjectRepository;
import vinegaraflow.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-members")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProjectMemberController {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<ProjectMember> getAllMembers() {
        return projectMemberRepository.findAll();
    }

    @GetMapping("/project/{projectId}")
    public List<ProjectMember> getMembersByProject(@PathVariable Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @projectRepository.findById(#request.projectId()).get().owner.username == authentication.name")
    public ProjectMember addMember(@Valid @RequestBody ProjectMemberRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (projectMemberRepository.existsByUserIdAndProjectId(user.getId(), project.getId())) {
            throw new RuntimeException("Member already exists");
        }
        ProjectMember member = ProjectMember.builder()
                .user(user)
                .project(project)
                .role(request.role())
                .build();
        return projectMemberRepository.save(member);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @projectMemberRepository.findById(#id).get().project.owner.username == authentication.name")
    public void removeMember(@PathVariable Long id) {
        projectMemberRepository.deleteById(id);
    }
}