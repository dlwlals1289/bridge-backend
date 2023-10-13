package com.Bridge.bridge.domain;

import com.Bridge.bridge.dto.request.ProjectRequestDto;
import com.Bridge.bridge.dto.response.PartResponseDto;
import com.Bridge.bridge.dto.response.ProjectResponseDto;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.awt.print.Book;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    private String title;           //제목

    private String overview;        // 개요, 프로젝트에 대한 간단한 소개

    private String dueDate;         //기간

    private String startDate;       // 프로젝트 시작일

    private String endDate;         // 프로젝트 종료일

    private String uploadTime;      // 프로젝트 작성시간 -> 최신순

    @JsonManagedReference
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Part> recruit = new ArrayList<>(); // 모집 분야, 모집 인원

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> tagLimit;        //지원자 태그 제한록

    private String meetingWay;      //대면 or 비대면 여부

    private String stage;           // 진행 단계

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                                              // 해당 프로젝트 글을 만든 유저

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ApplyProject> applyProjects = new ArrayList<>();   // 해당 프로젝트 글에 지원한 유저 목록

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarks = new ArrayList<>();           // 해당 프로젝트 글을 북마크한 유저 목록

    private int bookmarkNum; // 스크랩 횟수

    @Builder
    public Project(String title, String overview, String dueDate, String startDate, String endDate, List<Part> recruit, List<String> tagLimit, String meetingWay, String stage, User user) {
        LocalDateTime localDateTime = LocalDateTime.now();

        // 포맷
        String formatedNow = localDateTime.format(DateTimeFormatter.ofPattern("YYMMDDHHmmss"));

        this.title = title;
        this.overview = overview;
        this.dueDate = dueDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.uploadTime = formatedNow;
        this.recruit = recruit;
        this.tagLimit = tagLimit;
        this.meetingWay = meetingWay;
        this.stage = stage;
        this.bookmarkNum = 0;
        this.user = user;
    }

    public void setRecruit(List<Part> recruit) {
        this.recruit = recruit;
    }

    public void setBookmarks(Bookmark bookmark){
        this.bookmarks.add(bookmark);
    }

    public ProjectResponseDto toDto(){
        List<PartResponseDto> recruit = this.getRecruit().stream()
                .map((part) -> part.toDto())
                .collect(Collectors.toList());

        return ProjectResponseDto.builder()
                .title(this.getTitle())
                .overview(this.getOverview())
                .dueDate(this.getDueDate())
                .startDate(this.getStartDate())
                .endDate(this.getEndDate())
                .recruit(recruit)
                .tagLimit(this.getTagLimit())
                .meetingWay(this.getMeetingWay())
                .stage(this.getStage())
                .bookmarksNum(this.bookmarkNum)
                .userName(this.getUser().getName())
                .build();
    }

    public Project update(User user,  ProjectRequestDto projectRequestDto, List<Part> recruit){
        this.title = projectRequestDto.getTitle();
        this.overview = projectRequestDto.getOverview();
        this.dueDate = projectRequestDto.getDueDate();
        this.startDate = projectRequestDto.getStartDate();
        this.endDate = projectRequestDto.getEndDate();
        this.recruit = recruit;
        this.tagLimit = projectRequestDto.getTagLimit();
        this.meetingWay = projectRequestDto.getMeetingWay();
        this.stage = projectRequestDto.getStage();
        this.user = user;

        return this;
    }

    public Project updateDeadline(){
        LocalDateTime localDateTime = LocalDateTime.now();

        // 포맷
        String formatedNow = localDateTime.format(DateTimeFormatter.ofPattern("YYYYMMDDHHmmss"));

        this.dueDate = formatedNow;

        return this;
    }

    public int increaseBookmarksNum(){
        this.bookmarkNum = this.bookmarkNum + 1;

        return this.bookmarkNum;
    }

    public int decreaseBookmarksNum(){
        this.bookmarkNum = this.bookmarkNum - 1;

        return this.bookmarkNum;
    }

}
