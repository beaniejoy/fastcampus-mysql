package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.repository.TimelineRepository;
import com.example.fastcampusmysql.utils.CursorRequest;
import com.example.fastcampusmysql.utils.PageCursor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimelineReadService {

    private final TimelineRepository timelineRepository;

    public PageCursor<Timeline> getTimelines(Long memberId, CursorRequest cursorRequest) {
        List<Timeline> timelines = findAllBy(memberId, cursorRequest);
        long nextKey = getNextKey(timelines);

        return new PageCursor<>(cursorRequest.next(nextKey), timelines);
    }

    private List<Timeline> findAllBy(Long memberId, CursorRequest cursorRequest) {
        if (cursorRequest.hasKey()) {
            return timelineRepository.findAllByLessThanIdAndMemberIdAndOrderByIdDesc(
                cursorRequest.key(),
                memberId,
                cursorRequest.size()
            );
        } else {
            return timelineRepository.findAllByMemberIdAndOrderByIdDesc(
                memberId,
                cursorRequest.size()
            );
        }
    }

    private static long getNextKey(List<Timeline> timelines) {
        return timelines.stream()
            .mapToLong(Timeline::getId)
            .min()
            .orElse(CursorRequest.NONE_KEY);
    }
}
