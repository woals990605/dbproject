package site.metacoding.dbproject.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.dbproject.domain.post.Post;
import site.metacoding.dbproject.domain.post.PostRepository;
import site.metacoding.dbproject.domain.user.User;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> 글목록(Integer page) {
        PageRequest pr = PageRequest.of(page, 3);
        return postRepository.findAll(pr);
    }

    // 글수정페이지로 이동할 때도 재활용이 가능하다.
    public Post 글상세보기(Integer id) {
        Optional<Post> postOp = postRepository.findById(id);

        if (postOp.isPresent()) {
            Post postEntity = postOp.get();
            return postEntity;
        } else {
            return null;
        }

    }

    @Transactional
    public void 글수정() {

    }

    @Transactional
    public void 글삭제하기(Integer id) {
        postRepository.deleteById(id);// 내부적으로 exception 터짐
    }

    @Transactional
    public void 글쓰기(Post post, User principal) {
        post.setUser(principal);// User FK 추가!!
        postRepository.save(post);
    }

}