package kr.board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.board.entity.Board;
import kr.board.mapper.BoardMapper;

@RequestMapping("/board")
@RestController // ajax 사용시 쓰는 어노테이션 : @ResponseBody(json)응답 제거가능. 
public class BoardRestController {
	
	@Autowired
	BoardMapper boardMapper;
	
//  @ResponseBody ->jsckson-databind(객체를->JSON 데이터포맷으로 변환)
	@GetMapping("/all")
	public List<Board> boardList() {
		List<Board> list = boardMapper.getLists();
		// 객체를 json 데이터 형식으로 변환해서 리턴(응답)하겟다 : @ResponseBody ->api 가 동작해야한다.: jackson-databind API
		return list; // return 값이 jsp파일명도, 리다이렉트도 아닌, 객체를 리턴
	}
	
	@PostMapping("/new")
	public void boardInsert(Board vo) {
		boardMapper.boardInsert(vo); // 등록성공
	}
	
	@DeleteMapping("/{idx}")
	public void boardDelete(@PathVariable("idx") int idx) {
		boardMapper.boardDelete(idx);
	}
	
	@PutMapping("/update")
	public void boardUpdate(@RequestBody Board vo){
		boardMapper.boardUpdate(vo);
	}
	
	@GetMapping("/{idx}")
	public Board boardContnt(@PathVariable("idx") int idx) {
		Board vo = boardMapper.boardContent(idx);
		return vo; // vo->JSON
	}
	
	@PutMapping("/count/{idx}")
	public Board boardCount(@PathVariable("idx") int idx) {
		boardMapper.boardCount(idx);
		Board vo = boardMapper.boardContent(idx);
		return vo;
	}
}
