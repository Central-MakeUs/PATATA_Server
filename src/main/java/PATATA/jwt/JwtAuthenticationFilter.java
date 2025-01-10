package PATATA.jwt;

import PATATA.jwt.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private JwtService jwtService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        super(authenticationManager);  //BasicAuthenticationFilter 클래스의 생성자를 호출
        this.jwtService =jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 헤더에서 토큰 가져오기
        String token = jwtService.resolveToken(request);
        //String requestURI = request.getRequestURI();

        // 토큰 존재 여부 및 토큰 검증
        if (StringUtils.isNotEmpty(token)) {
            if (jwtService.validateTokenBoolean(token)) {
                //유효한 토큰을 통해 사용자 정보 추출
                Authentication authentication = jwtService.getAuthentication(token);

                //SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute("username", authentication.getName());
            } else {
                // 유효하지 않은 토큰 처리
                throw new ExpiredJwtException(null, null, "유효하지 않은 AccessToken 입니다.");
            }
        } else {
            logger.warn("Authorization 헤더가 없거나 비어 있습니다");
        }

        chain.doFilter(request, response);
    }
}
