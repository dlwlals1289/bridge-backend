package com.Bridge.bridge.service;

import com.Bridge.bridge.domain.*;
import com.Bridge.bridge.dto.request.FilterRequestDto;
import com.Bridge.bridge.dto.response.*;
import com.Bridge.bridge.dto.request.ProjectRequestDto;
import com.Bridge.bridge.exception.notfound.NotFoundSearchWordException;
import com.Bridge.bridge.repository.*;
import com.Bridge.bridge.dto.response.ProjectResponseDto;
import com.Bridge.bridge.repository.BookmarkRepository;
import com.Bridge.bridge.domain.ApplyProject;
import com.Bridge.bridge.domain.Part;
import com.Bridge.bridge.domain.User;
import com.Bridge.bridge.dto.request.FilterRequestDto;
import com.Bridge.bridge.dto.response.ApplyProjectResponse;
import com.Bridge.bridge.dto.response.ApplyUserResponse;
import com.Bridge.bridge.dto.response.ProjectListResponseDto;
import com.Bridge.bridge.dto.request.ProjectRequestDto;
import com.Bridge.bridge.dto.response.ProjectResponseDto;
import com.Bridge.bridge.exception.notfound.NotFoundProjectException;
import com.Bridge.bridge.exception.notfound.NotFoundUserException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Tuple;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PartRepository partRepository;
    private final BookmarkRepository bookmarkRepository;
    private final SearchWordRepository searchWordRepository;

    private final ApplyProjectRepository applyProjectRepository;


    /*
        Func : 프로젝트 모집글 생성
        Parameter : 프로젝트 입력 폼
        Return : 새로 생성된 프로젝트 ID
    */
    public Long createProject(ProjectRequestDto projectRequestDto){
        try {
            // 모집글 작성한 user 찾기
            User user = userRepository.findById(projectRequestDto.getUserId())
                    .orElseThrow(() -> new NotFoundUserException());

            Project newProject = projectRequestDto.toEntityOfProject(user);

            // 모집 분야, 인원 -> Part entity화 하기
            List<Part> recruit = projectRequestDto.getRecruit().stream()
                            .map((p) -> p.toEntity())
                            .collect(Collectors.toList());

            // Part- Project 매핑
            recruit.stream()
                            .forEach((part -> part.setProject(newProject)));

            // User - Project 매핑
            user.setProject(newProject);

            // 모집글 DB에 저장하기정
            Project save = projectRepository.save(newProject);

            return save.getId();
        }
        catch (Exception e){
            System.out.println(e);
            return null;
        }

    }

    /*
        Func : 프로젝트 모집글 삭제
        Parameter : 프로젝트 모집글 ID
        Return : 삭제 여부 -> HttpStatus
    */
    public Boolean deleteProject(Long projectId, Long userId){
        try {
            // 삭제할 프로젝트 모집글 찾기
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new NotFoundProjectException());

            // 삭제할 모집글을 작성한 유저 찾기
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundUserException());

            // 해당 모집글 삭제하기
            if (user.getId().equals(project.getUser().getId())) { // 찾은 프로젝트 유저가 삭제를 요청한 유저가 맞는지 확인
                projectRepository.delete(project);

                return true;
            }
            return false;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    /*
        Func : 프로젝트 모집글 수정
        Parameter : 프로젝트 모집글 수정폼
        Return : PrjectResponseDto -> 수정본
    */
    @Transactional
    public ProjectResponseDto updateProject(Long projectId, ProjectRequestDto projectRequestDto){
        // 모집글 작성한 user 찾기
        User user = userRepository.findById(projectRequestDto.getUserId())
                .orElseThrow(() -> new NotFoundUserException());

        // 모집글 찾기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        // 모집글 작성자와 유저가 같은지 확인하기
        if (user.getId().equals(project.getUser().getId())) {
            // 모집 분야, 인원 -> Part entity화 하기
            List<Part> recruit = projectRequestDto.getRecruit().stream()
                    .map((p) -> p.toEntity())
                    .collect(Collectors.toList());

            // 모집분야, 인원 초기화
            partRepository.deleteAll(project.getRecruit());

            // Part- Project 매핑
            Project finalProject = project;
            recruit.stream()
                    .forEach((part -> part.setProject(finalProject)));

            project = project.update(user, projectRequestDto, recruit);

            projectRepository.save(project);

            return project.toDto();
        }
        else {
            throw new NullPointerException("작성자와 요청자가 같지 않습니다.");
        }

    }

    /*
        Func : 프로젝트 모집글 검색(제목+내용)
        Parameter : 검색어
        Return : 프로젝트 모집글 List
    */
    @Transactional
    public List<ProjectListResponseDto> findByTitleAndContent(Long userId, String theSearchWord){

        // 모집글 작성한 user 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());


        // 최근 검색어 저장하기
        SearchWord searchWord = SearchWord.builder()
                .content(theSearchWord)
                .history(LocalDateTime.now())
                .user(user)
                .build();
        searchWordRepository.save(searchWord);

        user.getSearchWords().add(searchWord);

        List<Project> allProject = projectRepository.findAll();

        List<Project> findProject = allProject.stream()
                .filter((project) ->
                { return project.getOverview().contains(theSearchWord) || project.getTitle().contains(theSearchWord);
                })
                .collect(Collectors.toList());

        final int[] recruitTotal = {0};

        findProject.stream()
                .forEach((project -> project.getRecruit().stream()
                        .forEach((part -> recruitTotal[0] += part.getRecruitNum()))
                ));

        List<ProjectListResponseDto> response = findProject.stream()
                .map((project) -> ProjectListResponseDto.builder()
                        .projectId(project.getId())
                        .title(project.getTitle())
                        .dueDate(project.getDueDate())
                        .recruitTotalNum(recruitTotal[0])
                        .build()
                )
                .collect(Collectors.toList());

        return response;
    }

     /*
        Func : 프로젝트 모집글 상세보기
        Parameter : projectID - 모집글 ID
        Return : projectResponse
    */
    public ProjectResponseDto getProject(Long projectId){

        // 해당 모집글 찾기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        return project.toDto();
    }

    /*
        Func : 필터링 후 프로젝트 목록 반환
        Parameter : List<String>,
        Return : projectResponse
    */
    public List<ProjectListResponseDto> filterProjectList(FilterRequestDto filterRequestDto){


        List<Part> parts = partRepository.findAllByRecruitSkillInAndAndRecruitPart(filterRequestDto.getSkills(), filterRequestDto.getPart());

        List<Project> projects = projectRepository.findAllByRecruitIn(parts);

        final int[] recruitTotal = {0};

        projects.stream()
                .forEach((project -> project.getRecruit().stream()
                        .forEach((part -> recruitTotal[0] += part.getRecruitNum()))
                ));

        List<ProjectListResponseDto> response = projects.stream()
                .map((project) -> ProjectListResponseDto.builder()
                        .projectId(project.getId())
                        .title(project.getTitle())
                        .dueDate(project.getDueDate())
                        .recruitTotalNum(recruitTotal[0])
                        .build()
                )
                .collect(Collectors.toList());

        return response;
    }

    /*
        Func : 자신이 작성한 모집글 리스트 보여주기
        Parameter : userId
        Return : List<projectListResponseDto>
    */
    public List<MyProjectResponseDto> findMyProjects(Long userId){
        // 모집글 작성한 user 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        // 요청자가 작성한 작성글 모두 불러오기
        List<Project> myProjects = projectRepository.findAllByUser(user);

        // 작성한 모집글이 없다면
        if(myProjects.isEmpty()){
            throw new NotFoundProjectException();
        }

        // 총 모집인원
        final int[] recruitTotal = {0};

        // 총 모집인원 구하기
        myProjects.stream()
                .forEach((project -> project.getRecruit().stream()
                        .forEach((part -> recruitTotal[0] += part.getRecruitNum()))
                ));


        List<MyProjectResponseDto> response = new ArrayList<>();

        return myProjects.stream()
                .map((project -> MyProjectResponseDto.builder()
                        .projectId(project.getId())
                        .overview(project.getOverview())
                        .dueDate(project.getDueDate())
                        .recruitTotalNum(recruitTotal[0])
                        .build()))
                .collect(Collectors.toList());
    }




    /*
        Func : 모든 모집글 리스트 보여주기
        Return : List<projectListResponseDto>
    */
    public List<ProjectListResponseDto> allProjects(){
        List<Project> allProjects = projectRepository.findAll();

        // 작성글이 하나도 없다면
        if(allProjects.isEmpty()){
            throw new NotFoundProjectException();
        }

        // 총 모집인원
        final int[] recruitTotal = {0};

        // 총 모집인원 구하기
        allProjects.stream()
                .forEach((project -> project.getRecruit().stream()
                        .forEach((part -> recruitTotal[0] += part.getRecruitNum()))
                ));

        List<ProjectListResponseDto> response = allProjects.stream()
                .map((project -> ProjectListResponseDto.builder()
                        .projectId(project.getId())
                        .title(project.getTitle())
                        .dueDate(project.getDueDate())
                        .recruitTotalNum(recruitTotal[0])
                        .build()
                ))
                .collect(Collectors.toList());

        return response;

    }

    /*
        Func : 내 분야 모집글 리스트 보여주기
        Parameter : String - 모집분야
        Return : List<projectListResponseDto>
    */
    public List<ProjectListResponseDto> findMyPartProjects(String myPart){
        List<Part> parts = partRepository.findAllByRecruitPart(myPart);

        List<Project> myPartProjects = projectRepository.findAllByRecruitIn(parts);

        // 작성글이 하나도 없다면
        if(myPartProjects.isEmpty()){
            throw new NotFoundProjectException();
        }

        // 총 모집인원
        final int[] recruitTotal = {0};

        // 총 모집인원 구하기
        myPartProjects.stream()
                .forEach((project -> project.getRecruit().stream()
                        .forEach((part -> recruitTotal[0] += part.getRecruitNum()))
                ));

        List<ProjectListResponseDto> response = myPartProjects.stream()
                .map((project -> ProjectListResponseDto.builder()
                        .projectId(project.getId())
                        .title(project.getTitle())
                        .dueDate(project.getDueDate())
                        .recruitTotalNum(recruitTotal[0])
                        .build()
                ))
                .collect(Collectors.toList());

        return response;

    }

    /*
        Func : 모집글 마감 기능
        Parameter : projectId
        Return : ProjectResponseDto
    */
    public ProjectResponseDto closeProject(Long projectId, Long userId){
        // 해당 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        // 마감하고자 하는 프로젝트 찾기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        LocalDateTime localDateTime = LocalDateTime.now();

        // 포맷
        String formatedNow = localDateTime.format(DateTimeFormatter.ofPattern("YYYYMMDDHHmmss"));

        if (project.getUser().getId().equals(userId)){ // 프로젝트를 작성한 유저인가
            if(project.getDueDate().compareTo(formatedNow)<0){ // 마감시간이 이미 지난 경우
                throw new IllegalStateException("이미 마감이 된 모집글입니다.");
            }
            else {
                project = project.updateDeadline();
                projectRepository.save(project);

                return project.toDto();
            }
        }
        else {
            throw new IllegalStateException("프로젝트 작성자가 아닙니다.");
        }

    }

    /*
        Func : 모집글 스크랩 기능
        Parameter : projectId, userId
        Return : Boolean - 스크랩 여부
    */
    @Transactional
    public BookmarkResponseDto scrap(Long projectId, Long userId){
        // 해당 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        // 스크랩 하고자 하는 프로젝트 찾기
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        Bookmark bookmark = bookmarkRepository.findByProjectAndUser(project, user);

        if (bookmark == null){ // 스크랩 되어 있지 않다면
            Bookmark newBookmark = Bookmark.builder()
                    .user(user)
                    .project(project)
                    .build();

            newBookmark = bookmarkRepository.save(newBookmark);

            // user - bookmark 연관관계 맵핑
            user.setBookmarks(newBookmark);

            // project - bookmark 연관관계 맵핑
            project.setBookmarks(newBookmark);
            project.increaseBookmarksNum();

            return BookmarkResponseDto.builder()
                    .projectId(projectId)
                    .userId(userId)
                    .scrap("스크랩이 설정되었습니다.")
                    .build();
        }
        else {
            user.getBookmarks().remove(bookmark);
            bookmarkRepository.delete(bookmark); // 스크랩 해제

            project.decreaseBookmarksNum();

            return BookmarkResponseDto.builder()
                    .projectId(projectId)
                    .userId(userId)
                    .scrap("스크랩이 해제되었습니다.")
                    .build();
        }
    }

    /*
        Func : 최근 검색어 조회 기능
        Parameter : userId
        Return : List<SearchWordResponseDto>
    */
    public List<SearchWordResponseDto> resentSearchWord(Long userId){
        // 해당 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        return user.getSearchWords().stream()
                .map((searchWord -> SearchWordResponseDto.builder()
                        .searchWordId(searchWord.getId())
                        .searchWord(searchWord.getContent())
                        .build()))
                .collect(Collectors.toList());
    }

    /*
        Func : 최근 검색어 삭제 기능
        Parameter : userId, searchWordId
        Return : List<SearchWordResponseDto>
    */
    @Transactional
    public List<SearchWordResponseDto> deleteSearchWord(Long userId, Long searchWordId){
        // 해당 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        // 해당 검색어 찾기
        SearchWord theSearchWord = searchWordRepository.findById(searchWordId)
                .orElseThrow(()-> new NotFoundSearchWordException());

        user.getSearchWords().remove(theSearchWord);
        searchWordRepository.delete(theSearchWord);

        return user.getSearchWords().stream()
                .map((searchWord -> SearchWordResponseDto.builder()
                        .searchWordId(searchWord.getId())
                        .searchWord(searchWord.getContent())
                        .build()))
                .collect(Collectors.toList());
    }

    /*
        Func : 인기글 조회
        Parameter :
        Return : List<TopProjectResponseDto>
    */
    public List<TopProjectResponseDto> topProjects(){
        LocalDateTime localDateTime = LocalDateTime.now();
        int year = localDateTime.getYear();;
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();

        String now = LocalDateTime.of(year, month, day, 0,0,0).toString();

        List<Project> top20 = projectRepository.findTop20ByDueDateGreaterThanEqualOrderByBookmarkNumDesc(now);

        List<TopProjectResponseDto> topProjectResponseDtos = new ArrayList<>();

        for (int i=0; i<top20.size(); i++){
            topProjectResponseDtos.add(TopProjectResponseDto.builder()
                    .rank(i+1)
                    .title(top20.get(i).getTitle())
                    .dueDate(top20.get(i).getDueDate())
                    .build());
        }
        return topProjectResponseDtos;
    }

    /**
     * 지원한 프로젝트 목록 반환
     */
    public List<ApplyProjectResponse> getApplyProjects(Long userId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        List<ApplyProjectResponse> applyProjects = findUser.getApplyProjects().stream()
                .map(p -> new ApplyProjectResponse(p.getProject(), p.getStage()))
                .collect(Collectors.toList());

        return applyProjects;
    }

    /**
     * 프로젝트 지원하기
     */
    @Transactional
    public boolean apply(Long userId, Long projectId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        Project applyProject = projectRepository.findById(projectId)
                        .orElseThrow(() -> new NotFoundProjectException());

        ApplyProject project = new ApplyProject();
        project.setUserAndProject(findUser, applyProject);

        findUser.getApplyProjects().add(project);
        applyProject.getApplyProjects().add(project);

        return true;
    }

    /**
     * 프로젝트 지원 취소하기
     */
    @Transactional
    public boolean cancelApply(Long userId, Long projectId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        Project applyProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        ApplyProject findProject = applyProjectRepository.findByUserAndProject(findUser, applyProject)
                        .orElseThrow(() -> new NotFoundProjectException());

        findUser.getApplyProjects().remove(findProject);
        applyProject.getApplyProjects().remove(findProject);
        applyProjectRepository.deleteByUserAndProject(findUser, applyProject);

        return true;
    }

    /**
     * 프로젝트 지원자 목록
     */
    public List<ApplyUserResponse> getApplyUsers(Long projectId) {
        Project findProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        List<ApplyUserResponse> applyUsers = findProject.getApplyProjects().stream()
                .map(p -> {
                    User user = p.getUser();
                    List<String> fields = user.getFields().stream()
                            .map(f -> f.getFieldName())
                            .collect(Collectors.toList());

                    return ApplyUserResponse.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .fields(fields)
                            .career(user.getProfile().getCareer())
                            .build();
                })
                .collect(Collectors.toList());

        return applyUsers;
    }

    /**
     * 프로젝트 수락하기
     */
    @Transactional
    public void acceptApply(Long projectId, Long userId) {
        //TODO : 수락할 권한이 있는지?
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        //지원한 유저
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        ApplyProject applyProject = applyProjectRepository.findByUserAndProject(findUser, project)
                .orElseThrow(() -> new NotFoundProjectException());

        applyProject.changeStage("수락");

        //TODO : 이후 지원자 목록에서 처리
    }

    /**
     * 프로젝트 거절하기
     */
    @Transactional
    public void rejectApply(Long projectId, Long userId) {
        //TODO : 거절할 권한이 있는지?
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundProjectException());

        //지원한 유저
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException());

        ApplyProject applyProject = applyProjectRepository.findByUserAndProject(findUser, project)
                .orElseThrow(() -> new NotFoundProjectException());

        applyProject.changeStage("거절");

        //TODO : 이후 지원자 목록에서 처리
    }
}
