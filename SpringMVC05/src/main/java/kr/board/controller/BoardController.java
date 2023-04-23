package kr.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // anotation 으로 역할 규정
public class BoardController {
	
	@RequestMapping("/boardMain.do")
	public String main() {
		return "board/main";
	}
	
}
