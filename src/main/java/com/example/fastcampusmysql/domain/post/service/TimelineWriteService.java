package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.repository.TimelineRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimelineWriteService {

    private final TimelineRepository timelineRepository;

    public void deliveryToTimeline(Long postId, List<Long> toMemberIds) {
        var timelines = toMemberIds.stream()
            .map(memberId -> toTimeline(postId, memberId))
            .toList();

        timelineRepository.bulkInsert(timelines);
    }

    private static Timeline toTimeline(Long postId, Long memberId) {
        return Timeline.builder()
            .memberId(memberId)
            .postId(postId)
            .build();
    }
}
