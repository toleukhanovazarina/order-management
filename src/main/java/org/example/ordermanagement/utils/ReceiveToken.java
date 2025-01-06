package org.example.ordermanagement.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReceiveToken {
    private final JwtService jwtService;

    public Map<String,String> tokenData (){
        HttpServletRequest request = getCurrentHttpRequest();
        String token = jwtService.extractJwtToken(request);
        return jwtService.extractTokenData(token);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

}
