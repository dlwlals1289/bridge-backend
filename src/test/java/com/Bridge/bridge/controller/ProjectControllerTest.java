package com.Bridge.bridge.controller;

import com.Bridge.bridge.domain.ApplyProject;
import com.Bridge.bridge.domain.Field;
import com.Bridge.bridge.domain.Part;
import com.Bridge.bridge.domain.Platform;
import com.Bridge.bridge.domain.Profile;
import com.Bridge.bridge.domain.Project;
import com.Bridge.bridge.domain.User;
import com.Bridge.bridge.dto.request.FilterRequestDto;
import com.Bridge.bridge.dto.request.PartRequestDto;
import com.Bridge.bridge.dto.request.ProjectRequestDto;
import com.Bridge.bridge.repository.ProjectRepository;
import com.Bridge.bridge.repository.UserRepository;
import com.Bridge.bridge.service.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("모집글 생성")
    void createProject() throws Exception {
        // given
        User user = new User("bridge", "test1@gmaill.com", Platform.APPLE, "11");
        User newUser = userRepository.save(user);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<PartRequestDto> recruit = new ArrayList<>();
        recruit.add(PartRequestDto.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        ProjectRequestDto newProject = ProjectRequestDto.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userId(newUser.getId())
                .stage("Before Start")
                .build();

        String body = objectMapper.writeValueAsString(newProject);

        // when
        mockMvc.perform(post("/project")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk()) // 응답 status를 ok로 테스트
                .andDo(print());

    }

    @Test
    @DisplayName("모집글 삭제")
    void deleteProject() throws Exception {
        // given
        User user = new User("bridge", "test1@gmaill.com", Platform.APPLE, "11");
        userRepository.save(user);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<PartRequestDto> recruit = new ArrayList<>();
        recruit.add(PartRequestDto.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        ProjectRequestDto newProject = ProjectRequestDto.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userId(user.getId())
                .stage("Before Start")
                .build();

        projectService.createProject(newProject);

        Long userId = user.getId();
        Long projectId = projectRepository.findByUser_Id(userId).get().getId();


        // when
        mockMvc.perform(delete("/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("projectId", String.valueOf(projectId))
                        .content(objectMapper.writeValueAsString(userId)))
                .andExpect(status().is(202)) // 응답 status를 ok로 테스트
                .andDo(print());

    }

    @Test
    @DisplayName("모집글 수정")
    void updateProject() throws Exception {
        // given
        User user = new User("bridge", "test1@gmaill.com", Platform.APPLE, "11");
        userRepository.save(user);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<PartRequestDto> recruit = new ArrayList<>();
        recruit.add(PartRequestDto.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        ProjectRequestDto newProject = ProjectRequestDto.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userId(user.getId())
                .stage("Before Start")
                .build();

        projectService.createProject(newProject);

        List<String> updateSkill = new ArrayList<>();
        updateSkill.add("Javascript");
        updateSkill.add("React");

        List<PartRequestDto> updateRecruit = new ArrayList<>();
        updateRecruit.add(PartRequestDto.builder()
                .recruitPart("frontend")
                .recruitNum(2)
                .recruitSkill(updateSkill)
                .requirement("화이팅")
                .build());

        ProjectRequestDto updateProject = ProjectRequestDto.builder()
                .title("Update project")
                .overview("This is Updated Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(updateRecruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userId(user.getId())
                .stage("Before Start")
                .build();

        Long userId = user.getId();
        Long projectId = projectRepository.findByUser_Id(userId).get().getId();

        // when
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", userId.toString());
        data.add("ProjectRequestDto", updateProject.toString());

        mockMvc.perform(put("/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("projectId", String.valueOf(projectId))
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().is(202)) // 응답 status를 ok로 테스트
                .andDo(print());
    }

    @Test
    @DisplayName("모집글 상세보기")
    void detailProject() throws Exception {
        // given
        User user1 = new User("bridge", "detail_Wrong@gmail.com", Platform.APPLE, "detailTest_wrongProjectID");
        userRepository.save(user1);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        Project newProject = Project.builder()
                .title("Find project")
                .overview("This is the project that i find")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();

        Project theProject = projectRepository.save(newProject);


        // when
        mockMvc.perform(get("/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("projectId", String.valueOf(theProject.getId())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(newProject.getTitle()))
                .andDo(print());

    }

    @Test
    void filtering() throws Exception {
        // given
        User user = new User("bridge", "test1@gmaill.com", Platform.APPLE, "11");
        userRepository.save(user);

        List<String> skill1 = new ArrayList<>();
        skill1.add("Java");
        skill1.add("Spring boot");

        List<String> skill2 = new ArrayList<>();
        skill2.add("Java");
        skill2.add("Spring boot");

        List<PartRequestDto> recruit1 = new ArrayList<>();
        recruit1.add(PartRequestDto.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill1)
                .requirement("backend")
                .build());

        List<PartRequestDto> recruit2 = new ArrayList<>();
        recruit2.add(PartRequestDto.builder()
                .recruitPart("frontend")
                .recruitNum(1)
                .recruitSkill(skill2)
                .requirement("frontend")
                .build());

        ProjectRequestDto newProject1 = ProjectRequestDto.builder()
                .title("This is what i find")
                .overview("This is backend Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit1)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userId(user.getId())
                .stage("Before Start")
                .build();

        ProjectRequestDto newProject2 = ProjectRequestDto.builder()
                .title("This is not what i find")
                .overview("This is frontend Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit2)
                .tagLimit(new ArrayList<>())
                .meetingWay("ONline")
                .userId(user.getId())
                .stage("Before Start")
                .build();

        projectService.createProject(newProject1);
        projectService.createProject(newProject2);

        List<String> findSkills = new ArrayList<>();
        findSkills.add("Java");
        findSkills.add("Spring boot");

        FilterRequestDto filterRequestDto = FilterRequestDto.builder()
                .part("backend")
                .skills(findSkills)
                .build();

        String body = objectMapper.writeValueAsString(filterRequestDto);

        // when
        String expectByTitle = "$.[?(@.title == '%s')]";

        mockMvc.perform(post("/project/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(expectByTitle, "This is what i find").exists())
                .andDo(print());

    }

    @Test
    @DisplayName("지원한 프로젝트 목록 조회")
    void getApplyProjects() throws Exception {
        //given
        User user1 = new User("bridge1", "bridge1@apple.com", Platform.APPLE, "1");

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .dueDate("23-10-10")
                .build();

        projectRepository.save(project1);

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);


        user1.getApplyProjects().add(applyProject1);
        User saveUser1 = userRepository.save(user1);

        //expected
        mockMvc.perform(get("/projects/apply")
                    .param("userId", saveUser1.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stage").value("stage1"))
                .andExpect(jsonPath("$[0].title").value("title1"))
                .andExpect(jsonPath("$[0].overview").value("overview1"))
                .andExpect(jsonPath("$[0].dueDate").value("23-10-10"))
                .andDo(print());
    }

    @Test
    @DisplayName("프로젝트 지원하기")
    void applyProjects() throws Exception {
        //given
        User user1 = new User("bridge1", "bridge1@apple.com", Platform.APPLE, "1");
        userRepository.save(user1);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .dueDate("23-10-10")
                .build();

        Project saveProject = projectRepository.save(project1);
        User saveUser1 = userRepository.save(user1);

        //expected
        mockMvc.perform(post("/projects/apply")
                        .param("userId", saveUser1.getId().toString())
                        .param("projectId", saveProject.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("프로젝트 지원 취소하기")
    void cancelApply() throws Exception {
        //given
        User user1 = new User("bridge1", "bridge1@apple.com", Platform.APPLE, "1");

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .dueDate("23-10-10")
                .build();

        Project saveProject = projectRepository.save(project1);

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);

        user1.getApplyProjects().add(applyProject1);
        User saveUser1 = userRepository.save(user1);

        //expected
        mockMvc.perform(post("/projects/apply/cancel")
                        .param("userId", saveUser1.getId().toString())
                        .param("projectId", saveProject.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("프로젝트 지원자 목록")
    void getApplyUsersDetail() throws Exception {
        //given
        User user1 = new User("bridge1", "bridge1@apple.com", Platform.APPLE, "1");

        Field field1 = new Field("Backend");
        field1.updateFieldUser(user1);

        user1.getFields().add(field1);

        Profile profile1 = Profile.builder()
                .career("career1")
                .build();

        user1.updateProfile(profile1);

        userRepository.save(user1);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .dueDate("23-10-10")
                .build();

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);

        project1.getApplyProjects().add(applyProject1);
        Project saveProject = projectRepository.save(project1);

        //expected
        mockMvc.perform(get("/projects/apply/users")
                        .param("projectId", saveProject.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(user1.getId()))
                .andExpect(jsonPath("$[0].name").value("bridge1"))
                .andExpect(jsonPath("$[0].fields[0]").value("Backend"))
                .andExpect(jsonPath("$[0].career").value("career1"))
                .andDo(print());
    }
}