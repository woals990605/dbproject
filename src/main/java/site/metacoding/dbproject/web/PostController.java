package site.metacoding.dbproject.web;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import site.metacoding.dbproject.domain.post.Post;
import site.metacoding.dbproject.domain.post.PostRepository;
import site.metacoding.dbproject.domain.user.User;
import site.metacoding.dbproject.service.PostService;
import site.metacoding.dbproject.web.dto.ResponseDto;

// 필요한 constructor를 만들어준다. final이 붙은 애들에 대한 생성자를 만들어준다.
@RequiredArgsConstructor
@Controller
public class PostController {

    private final HttpSession session;
    private final PostService postService;
    private final PostRepository postRepository; // 다하고 지워야됨!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    // GET 글쓰기 페이지 /post/writeForm - 인증 O
    @GetMapping("/s/post/writeForm")
    public String writeForm() {

        // 인증(필터링 처리를 아직 안해서 메서드에서 직접 막아준다.)
        if (session.getAttribute("principal") == null) {
            return "redirect:/loginForm";
        }

        return "post/writeForm";
    }

    // POST 글쓰기 /post - 인증 O
    @PostMapping("/s/post")
    public String write(Post post) {
        // System.out.println("title : " + title + ", content : " + content);

        // 유효성 검사 패스

        if (session.getAttribute("principal") == null) {
            return "redirect:/loginForm";
        }

        User principal = (User) session.getAttribute("principal");
        // 오브젝트를 통째로 넣으면 알아서 foreignkey가 연결된다.
        postService.글쓰기(post, principal);
        return "redirect:/";
    }

    // 메인페이지 => 보통 주소를 두개를 걸어놓는다.
    // GET 글목록 페이지 /post/list, / - 인증 X
    @GetMapping({ "/", "/post/list" })
    public String list(@RequestParam(defaultValue = "0") Integer page, Model model) {
        // 1. PostRepository의 findAll() 호출
        // List<Post> posts = postRepository.findAll();
        // List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC,
        // "id")); // 힌트를 이용해서 거꾸로

        // List<Post> posts = postRepository.findAll(pr);

        // 2. model에 담기
        Page<Post> pagePosts = postService.글목록(page);

        model.addAttribute("posts", pagePosts);
        model.addAttribute("nextPage", page + 1);
        model.addAttribute("prevPage", page - 1);

        return "post/list";
    }

    @GetMapping("/test/post/list")
    public @ResponseBody Page<Post> listTest(@RequestParam(defaultValue = "0") Integer page) {
        PageRequest pq = PageRequest.of(page, 1);
        return postRepository.findAll(pq);
    }

    // GET 글 상세보기 페이지 / post/{id} (삭제버튼 만들어 두면됨. 수정버튼 만들어 두면됨) - 인증 X
    @GetMapping("/post/{id}") // 인증이 필요없기 때문에 주소는 이대로 놔둬야 한다. 따라서 주소는 놔두고 Get요청시에는 필터에서 /post 거르는거를 제외를 시켜준다.
    public String detail(@PathVariable Integer id, Model model) {

        User principal = (User) session.getAttribute("principal");

        Post postEntity = postService.글상세보기(id);

        // 게시불이 없으면 error 페이지로 이동
        if (postEntity == null) {
            return "error/page1";
        }

        if (principal != null) {
            // 권한 확인해서 view로 값 넘김
            if (principal.getId() == postEntity.getUser().getId()) { // 권한이 있다는 뜻
                model.addAttribute("pageOwner", true);
            } else {
                model.addAttribute("pageOwner", false);
            }
        }

        model.addAttribute("post", postEntity);
        return "post/detail";

    }

    // GET 글 수정 페이지 /post/{id}/updateForm - 인증 O
    @GetMapping("/s/post/{id}/updateForm")
    public String updateForm(@PathVariable Integer id, Model model) {

        // 인증
        User principal = (User) session.getAttribute("principal");
        if (principal == null) {
            return "error/page1";
        }

        // 권한
        Post postEntity = postService.글상세보기(id);

        if (postEntity.getUser().getId() != principal.getId()) {
            return "error/page1";
        }

        model.addAttribute("post", postEntity);

        return "post/updateForm";
    }

    // UPDATE 글수정 /post/{id} - 인증 O
    @PutMapping("/s/post/{id}")
    public @ResponseBody ResponseDto<String> update(@PathVariable Integer id, @RequestBody Post post) {

        // 인증
        User principal = (User) session.getAttribute("principal");
        if (principal == null) {
            return new ResponseDto<String>(-1, "로그인 되지 않았습니다.", null);
        }

        // 권한
        Post postEntity = postService.글상세보기(id);

        if (postEntity.getUser().getId() != principal.getId()) {
            return new ResponseDto<String>(-1, "해당 게시물을 수정할 권한이 없습니다.", null);
        }

        postService.글수정하기(post, id);

        return new ResponseDto<String>(1, "성공", null);
    }

    // DELETE 글삭제 /post/{id} - 인증 O
    @DeleteMapping("/s/post/{id}")
    public @ResponseBody ResponseDto<String> delete(@PathVariable Integer id) {

        User principal = (User) session.getAttribute("principal");

        if (principal == null) {// 로그인이 안됐다는 뜻
            return new ResponseDto<String>(-1, "로그인이 되지 않았습니다", null);
        }

        Post postEntity = postService.글상세보기(id);

        if (principal.getId() != postEntity.getUser().getId()) { // 권한이 없다는 뜻
            return new ResponseDto<String>(-1, "해당 글을 삭제할 권한이 없습니다.", null);
        }

        postService.글삭제하기(id);// 내부적으로 exception이 터지면 무조건 스택 트레이스를 리턴한다.

        return new ResponseDto<String>(1, "성공", null);
    }

}