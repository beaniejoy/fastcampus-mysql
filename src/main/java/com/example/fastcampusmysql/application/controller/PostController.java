package com.example.fastcampusmysql.application.controller;

import com.example.fastcampusmysql.application.usecase.CreatePostLikeUseCase;
import com.example.fastcampusmysql.application.usecase.CreatePostUseCase;
import com.example.fastcampusmysql.application.usecase.GetTimelinePostsUseCase;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.dto.PostCommand;
import com.example.fastcampusmysql.domain.post.dto.PostDto;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import com.example.fastcampusmysql.domain.post.service.PostWriteService;
import com.example.fastcampusmysql.utils.CursorRequest;
import com.example.fastcampusmysql.utils.PageCursor;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final PostReadService postReadService;
    private final GetTimelinePostsUseCase getTimelinePostsUseCase;
    private final PostWriteService postWriteService;
    private final CreatePostLikeUseCase createPostLikeUseCase;

    @PostMapping
    public Long create(PostCommand command) {
        return createPostUseCase.execute(command);
    }

    @GetMapping("/daily-post-counts")
    public List<DailyPostCount> getDailyPostCounts(@ModelAttribute DailyPostCountRequest request) {
        return postReadService.getDailyPostCount(request);
    }

    @PageableAsQueryParam
    @GetMapping("/members/{memberId}")
    public Page<PostDto> getPosts(
        @PathVariable("memberId") Long memberId,
        @Parameter(hidden = true)
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return postReadService.getPosts(memberId, pageable);
    }

    @GetMapping("/members/{memberId}/by-cursor")
    public PageCursor<Post> getPostsByCursor(
        @PathVariable("memberId") Long memberId,
        @ParameterObject @ModelAttribute CursorRequest cursorRequest
    ) {
        return postReadService.getPostsByCursor(memberId, cursorRequest);
    }

    @GetMapping("/members/{memberId}/timeline")
    public PageCursor<Post> getTimeline(
        @PathVariable("memberId") Long memberId,
        @ParameterObject @ModelAttribute CursorRequest cursorRequest
    ) {
        return getTimelinePostsUseCase.executeByTimeline(memberId, cursorRequest);
    }

    @PostMapping("/{postId}/like/v1")
    public void likePost(@PathVariable Long postId) {
//        postWriteService.likePost(postId);
        postWriteService.likePostByOptimisticLock(postId);
    }

    @PostMapping("/{postId}/like/v2")
    public void likePostV2(
        @PathVariable Long postId,
        @RequestParam Long memberId
    ) {
        createPostLikeUseCase.execute(postId, memberId);
    }
}
