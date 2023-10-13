package com.Bridge.bridge.dto.response;

import com.Bridge.bridge.domain.User;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ApplyUserResponse {

    private Long userId;

    private String name;

    private List<String> fields; //관심 분야

    private String career;  //경력 사항

    @Builder
    public ApplyUserResponse(Long userId, String name, List<String> fields, String career) {
        this.userId = userId;
        this.name = name;
        this.fields = fields;
        this.career = career;
    }
}