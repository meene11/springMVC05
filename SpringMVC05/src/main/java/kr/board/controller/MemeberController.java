package kr.board.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import kr.board.entity.AuthVO;
import kr.board.entity.Member;
import kr.board.mapper.MemberMapper;

@Controller
public class MemeberController {
	
	@Autowired
	MemberMapper memberMapper;
	
	// 추가
	@Autowired
	PasswordEncoder pwEncoder;
	
	@RequestMapping("/memJoin.do")
	public String memJoin() {
		return "member/join";
	}
	
	@RequestMapping("/memRegisterCheck.do")
	public @ResponseBody int memRegisterCheck(@RequestParam("memID") String memID) {
		
		Member m = memberMapper.registerCheck(memID);
		if(m != null || memID.equals("")) {
			return 0; // 이미 존재하는 회원, 입력불가
		}
		
		return 1; // 사용가능한 아이디 
	}
	
	@RequestMapping("/memRegister.do")
	public String memRegister(Member m, String memPassword1, String memPassword2,
			                  RedirectAttributes rttr, HttpSession session) {
		if(m.getMemID()==null || m.getMemID().equals("") ||
		   memPassword1==null || memPassword1.equals("") ||
		   memPassword2==null || memPassword2.equals("") ||
		   m.getMemName()==null || m.getMemName().equals("") ||	
		   m.getMemAge()==0 || m.getAuthList().size() == 0 ||
		   m.getMemGender()==null || m.getMemGender().equals("") ||
		   m.getMemEmail()==null || m.getMemEmail().equals("")) {
		   // 누락메세지를 가지고 가기? =>객체바인딩(Model, HttpServletRequest, HttpSession)
		   // redirect : join.jsp 로 보낼때 객체바인딩 하기 위해서는 RedirectAttributes 를 이용한다!!!
		   rttr.addFlashAttribute("msgType", "실패 메세지");
		   rttr.addFlashAttribute("msg", "모든 내용을 입력하세요.");
		   return "redirect:/memJoin.do";  // ${msgType} , ${msg} // join.jsp 에서 ${msgType} ${msg} 이엘태그로 메세지 전달가능. RedirectAttributes를 이용하면 가능함.
		}
		
		// 비밀번호/비밀번호확인 동일하지 않을 경우 분기처리
		if(!memPassword1.equals(memPassword2)) {
		   rttr.addFlashAttribute("msgType", "실패 메세지");
		   rttr.addFlashAttribute("msg", "비밀번호가 서로 다릅니다.");
		   return "redirect:/memJoin.do";  // ${msgType} , ${msg}
		}
		
		m.setMemProfile(""); // 사진이미는 없다는 의미 ""
		
		// 회원을 테이블에 저장하기
		// 추가 : 비밀번호를 암호화 하기(api 이용 passwordEncoder)
		String encyptPw = pwEncoder.encode(m.getMemPassword());
		m.setMemPassword(encyptPw); // 평문비번->암호화시키고:encyptPw 이값을 set 비번에 넣어준다
		// register() 수정 
		int result = memberMapper.register(m);
		if(result == 1) { // 회원가입 성공 메세지
			// 추가 : 권한테이블에 회원의 권한을 저장하기
			List<AuthVO> list = m.getAuthList();
			for(AuthVO authVO : list) {
				if(authVO.getAuth() != null) {
					AuthVO saveVO = new AuthVO();
					saveVO.setMemID(m.getMemID()); // 회원아이디
					saveVO.setAuth(authVO.getAuth()); // 회원의 권한
					memberMapper.authInsert(saveVO);
				}
			}
			
		    rttr.addFlashAttribute("msgType", "성공 메세지");
		    rttr.addFlashAttribute("msg", "회원가입에 성공했습니다.");
		    // 회원가입이 성공하면=>로그인처리하기
		    // getMember() -> 회원정보+권한정보
		    Member mvo = memberMapper.getMember(m.getMemID());
		    System.out.println(" mvo :" + mvo);
		    session.setAttribute("mvo", mvo); // ${!empty mvo}  // ${!empty m}  // header.jsp 에서 회원인지 아닌지 체크하는 부분있음:  <c:if test="${!empty mvo}"> 
		   return "redirect:/";
		}else {
		   rttr.addFlashAttribute("msgType", "실패 메세지");
		   rttr.addFlashAttribute("msg", "이미 존재하는 회원입니다.");
		   return "redirect:/memJoin.do";
		}		
	}
	
	// 로그아웃 처리
	@RequestMapping("/memLogout.do")
	public String memLogout(HttpSession session) {
		session.invalidate(); //invalidate() 세션을 무효화
		return "redirect:/";
	}
	
	// 로그일 화면으로 이동
	@RequestMapping("/memLoginForm.do")
	public String memLoginForm() {
		return "member/memLoginForm"; // memLoginForm.jsp
	}
	
	// 로그인 기능 구현
	@RequestMapping("/memLogin.do")
	public String memeLogin(Member m, RedirectAttributes rttr, HttpSession session) {
		if(m.getMemID()==null || m.getMemID().equals("") ||
		   m.getMemPassword()==null || m.getMemPassword().equals("") ) {
			rttr.addFlashAttribute("msgType", "실패 메세지");
			rttr.addFlashAttribute("msg", "모든 내용을 입력해주세요.");
			return "redirect:/memLoginForm.do";
		}
		
		Member mvo = memberMapper.memLogin(m);
		// 추가: 비밀번호 일치여부 체크(패쓰워드 암호화 인코딩)
		if(mvo != null && pwEncoder.matches(m.getMemPassword(), mvo.getMemPassword())) { // 로그인에 성공  
								// matches((CharSequence rawPassword, String encodedPassword))
								//                       오리지널패스워드, mvo는 DB에 저장된(인코딩 암호화된)패쓰워드
			rttr.addFlashAttribute("msgType", "성공 메세지");
			rttr.addFlashAttribute("msg", "로그인에 성공했습니다.");
			session.setAttribute("mvo", mvo); // 세션처리 : ${!empty mvo} header.jsp 에서 분기처리 로그인성공/실패
			return "redirect:/";
		} else { // 로그인에 실패
			rttr.addFlashAttribute("msgType", "실패 메세지");
			rttr.addFlashAttribute("msg", "다시 로그인 해주세요.");
			return "redirect:/memLoginForm.do";
		}
		
	}
	
	// 회원정보수정화면 memUpdateForm
	@RequestMapping("/memUpdateForm.do")
	public String memUpdateForm() {
		
		return "member/memUpdateForm"; // memUpdateForm.jsp
	}
	
	// 회원정보수정
	@RequestMapping("/memUpdate.do")
	public String memUpdate(Member m, RedirectAttributes rttr,
			String memPassword1, String memPassword2, HttpSession session) {
		if(m.getMemID()==null || m.getMemID().equals("") ||
		   memPassword1==null || memPassword1.equals("") ||
		   memPassword2==null || memPassword2.equals("") ||
		   m.getMemName()==null || m.getMemName().equals("") ||	
		   m.getMemAge()==0 ||
		   m.getMemGender()==null || m.getMemGender().equals("") ||
		   m.getMemEmail()==null || m.getMemEmail().equals("")) {
		   // 누락메세지를 가지고 가기? =>객체바인딩(Model, HttpServletRequest, HttpSession)
		   rttr.addFlashAttribute("msgType", "실패 메세지");
		   rttr.addFlashAttribute("msg", "모든 내용을 입력하세요.");
		   return "redirect:/memUpdateForm.do";  // memUpdateForm.jsp  ${msgType} , ${msg}
		}
		if(!memPassword1.equals(memPassword2)) {
		   rttr.addFlashAttribute("msgType", "실패 메세지");
		   rttr.addFlashAttribute("msg", "비밀번호가 서로 다릅니다.");
		   return "redirect:/memUpdateForm.do";  // ${msgType} , ${msg}
		}		
		// 회원을 수정저장하기
		// 추가 : 비밀번호 암호화
		String encyptPw = pwEncoder.encode(m.getMemPassword());
		m.setMemPassword(encyptPw);
		
		int result=memberMapper.memUpdate(m);
		if(result==1) { // 수정성공 메세지
			// 1. 기존권한을 삭제하고
			// 2. 새로운 권한을 추가하기
			
			memberMapper.authDelete(m.getMemID());
			
			// 새로운권한추가
			List<AuthVO> list = m.getAuthList();
			for(AuthVO authVO : list) {
				if(authVO.getAuth() != null) {
					AuthVO saveVO = new AuthVO();
					saveVO.setMemID(m.getMemID());
					saveVO.setAuth(authVO.getAuth());
					memberMapper.authInsert(saveVO);
				}
			}
			
		    rttr.addFlashAttribute("msgType", "성공 메세지");
		    rttr.addFlashAttribute("msg", "회원정보 수정에 성공했습니다.");
		    // 회원수정이 성공하면=>로그인처리하기
		    Member mvo = memberMapper.getMember(m.getMemID());
		    session.setAttribute("mvo", mvo); // ${!empty mvo}
		    return "redirect:/";
		}else {
		    rttr.addFlashAttribute("msgType", "실패 메세지");
		    rttr.addFlashAttribute("msg", "회원정보 수정에 실패했습니다.");
		    return "redirect:/memUpdateForm.do";
		}
	}
	
	// 회원의 사진등록화면
	@RequestMapping("/memImageForm.do")
	public String memImageForm() {
		return "member/memImageForm"; // memImageForm.jsp
	}
	
	// 회원사진 이미지 업로드 : upload, DB저장
	@RequestMapping("/memImageUpdate.do")
	public String memImageUpdate(HttpServletRequest request, HttpSession session, RedirectAttributes rttr) {
		// 파일업로드 api(cos.jar, 그외3가지 방법이 있다)
		MultipartRequest multi = null;
		int fileMaxSize = 10*1024*1024; // 10MB
		
		// C:/eGovFrame-4.0.0/workspace.edu/SpringMVC03/src/main/webapp/resources/upload : 우클릭-propeties : (주석도 에러나서 역슬러쉬->/ 변경)
		// C:/eGovFrame-4.0.0/workspace.edu/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/SpringMVC03/resources/upload 
		// 이클립스는 프로젝트를 따로 관리. 실제로 배포할때. 백업본. 기록본. 완성본 와 같다. 이클립스가 최종적으로 관리하는 곳. 
		String savePath = request.getRealPath("resources/upload"); // 실제 경로 : request.getRealPath 는 이클립스가 최종적으로 관리하는 .metadata 폴더안에.. 가리키며, 사진 업로드되는 곳임.
		
		try {
			// 이미지 업로드                                                           // ex) 1.png 또 이미지명 동일-> 1_1.png
			multi = new MultipartRequest(request, savePath, fileMaxSize, "UTF-8", new DefaultFileRenamePolicy()); // MultipartRequest 생성할때 request 객체, 경로를 전달해줘야한다. 어느 경로에 저장할지. 사이즈, 파일명 중복일때 reName해주는 것 등
		} catch (Exception e) {
			e.printStackTrace();
			rttr.addFlashAttribute("msgType", "실패 메세지");
			rttr.addFlashAttribute("msg", "파일의 크기는 10MB를 넘을 수 없습니다.");
			return "redirect:/memImageForm.do";
		}
		
		// 데이터베이스 테이블에 회원이미지를 업데이트
		String memID = multi.getParameter("memID");
		String newProfile = "";
		String fileName = "";
		File file = multi.getFile("memProfile"); // memProfile는 memImageForm.jsp 이미지업로드버튼:<input type="file" name="memProfile" />
		// multi객체는 tmp0/wtpwebapps/SpringMVC03/resources/upload 폴더안에 업로드된 파일들을 알고있다.
		// memProfile 로 클라이언트에서 넘어온 파일명을 알고있다. ex)1.png 
		// 파일 객체는 업로드된 파일(사진)을 가리킨다.
		if(file != null) { // 업로드가 된상태 : png, jpg, gif
			// 파일 여부 체크 -> 이미지 파일이 아니면 삭제
			String ext = file.getName().substring(file.getName().lastIndexOf(".")+1); // 확장자명 구하기 ex 1.png -> png
			ext = ext.toUpperCase(); // 대소문.. ->대문자 : PNG, GIF, JPG
			if(ext.equals("PNG") || ext.equals("GIF") || ext.equals("JPG")) {
				// 새로업로드된 이미지 new, 현재 DB에 있는 이미지 old
				String oldProfile = memberMapper.getMember(memID).getMemProfile();
				File oldFile = new File(savePath + "/" + oldProfile);
				if(oldFile.exists()) {
					oldFile.delete();
				}
				newProfile = file.getName();
			}else { // 이미지 파일 아니면 삭제
				if(file.exists()) {
					file.delete(); // 삭제
				}
				rttr.addFlashAttribute("msgType", "실패 메세지");
				rttr.addFlashAttribute("msg", "이미지 파일만 업로드 가능합니다.");
				return "redirect:/memImageForm.do";
			}
		}
		
		// 새로운 이미지를 테이블에 엡데이트 
		Member mvo = new Member();
		mvo.setMemID(memID);
		mvo.setMemProfile(newProfile);
		memberMapper.memProfileUpdate(mvo); // 이미지 업데이트 성공
		Member m = memberMapper.getMember(memID);
		// 세션을 새롭게 생성한다.
		session.setAttribute("mvo", m); // 로그인시 세션 생성하지만, 다시 세션 생성하는 이유는 프로필사진 이번에 저장적용한 세션으로 주기위함.
		
		rttr.addFlashAttribute("msgType", "성공 메세지");
		rttr.addFlashAttribute("msg", "이미지 변경이 성공했습니다.");
		
		return "redirect:/"; // root.메인페이지로 이동.
	}
}
