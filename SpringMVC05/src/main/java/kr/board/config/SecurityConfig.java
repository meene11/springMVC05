package kr.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableWebSecurity // web과 연결하기 위한 연결고리 기능의 어노테이션
public class SecurityConfig extends WebSecurityConfigurerAdapter { //Spring Security 환경설정파일

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 요청에 대한 설정
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF-8");
		filter.setForceEncoding(true);
		http.addFilterBefore(filter,CsrfFilter.class); 
	} 

	// SecurityConfig에 객체가 하나 필요하다. 패쓰워드를 인코딩해줄수있는 객체. bean에 의해서 메모리(메모리에 올리기위함)에 객체를 만들어서 의존성 주입을 하기위함 
	// 패스워드 인코딩 객체 설정
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); //평문을 -> 암호화 만들어줌
	}

}
