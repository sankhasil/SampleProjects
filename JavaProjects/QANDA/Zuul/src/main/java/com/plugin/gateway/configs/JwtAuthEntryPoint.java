package com.plugin.gateway.configs;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        final String expired = (String) httpServletRequest.getAttribute("Expired");
        if(expired!=null){
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,expired);
        }
        else{
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid login details");
        }
    }
}
