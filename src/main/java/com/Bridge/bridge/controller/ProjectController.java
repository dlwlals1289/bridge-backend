package com.Bridge.bridge.controller;

import com.Bridge.bridge.dto.ProjectListDto;
import com.Bridge.bridge.dto.ProjectRequestDto;
import com.Bridge.bridge.dto.response.ProjectResponseDto;
import com.Bridge.bridge.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;


    // 프로젝트 모집글 작성
    @PostMapping("/project")
    public Long createProject(@RequestBody ProjectRequestDto projectRequestDto, Long userId){
        return projectService.createProject(projectRequestDto, userId);
    }

    // 검색어 기준으로 프로젝트 모집글 조회
    @GetMapping("/project/list")
    public List<ProjectListDto> searchProject(@RequestParam String searchWord){
        return projectService.findByTitleAndContent(searchWord);
    }

    // 프로젝트 모집글 삭제
    @DeleteMapping("/project")
    public ResponseEntity deleteProject(@RequestParam Long projectId, @RequestBody Long userId){
        Boolean result = projectService.deleteProject(projectId, userId);

        if (result.equals(true)){
            return new ResponseEntity(HttpStatus.ACCEPTED);
        }
        else{
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    // 프로젝트 모집글 수정
    @PutMapping("/project")
    public ProjectResponseDto updateProject(@RequestParam Long projectId, @RequestBody Long userId, ProjectRequestDto projectRequestDto){
        return projectService.updateProject(projectId, userId, projectRequestDto);
    }

    // 프로젝트 모집글 상세보기
    @GetMapping("/project")
    public ProjectResponseDto detailProject(@RequestParam Long projectId){
        return projectService.getProject(projectId);
    }

}
