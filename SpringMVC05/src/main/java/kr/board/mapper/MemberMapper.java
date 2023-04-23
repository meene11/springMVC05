package kr.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import kr.board.entity.AuthVO;
import kr.board.entity.Board;
import kr.board.entity.Member;

@Mapper // Mybatis API 
public interface MemberMapper { // 구현체는 sqlSessionFactory가 구현 및 실행 한다(스프링내부)  
	public Member registerCheck(String memID);
	public int register(Member m); // 회원등록(성공1, 실패0)
	public Member memLogin(Member m); // 로그인체크
	public int memUpdate(Member m); // 수정하기
	public Member getMember(String memID);
	public void memProfileUpdate(Member mvo);
	public void authInsert(AuthVO saveVO);
	public void authDelete(String memID);
}
