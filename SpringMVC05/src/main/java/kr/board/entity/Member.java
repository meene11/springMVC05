package kr.board.entity;

import java.util.List;

import lombok.Data;

@Data
public class Member {
	private int memIdx; 
	private String memID;
	private String memPassword;
	private String memName;
	private int memAge; // <- 등록시 미입력: null이 들어갈수없다. 타입이 int
	private String memGender;
	private String memEmail;
	private String memProfile;
	
	private List<AuthVO> authList;
	// 클래스AuthVO(객체) no, memID, auth
	// authList[0].auth, authList[1].auth, authList[2].auth 
	
}
