package kr.board.entity;

import lombok.Data;

@Data // 롬복 자동 setter, getter 생성
public class AuthVO {
	private int no; // 일련번호
	private String memID; // 회원 아이디
	private String auth; // 회원권한(3가지 부여: ROLE_USER, ROLE_MANAGER, ROLE_ADMIN )
}
