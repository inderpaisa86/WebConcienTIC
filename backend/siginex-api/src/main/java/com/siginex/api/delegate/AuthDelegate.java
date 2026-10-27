package com.siginex.api.delegate;

import com.siginex.api.service.AuthService;
import com.siginex.api.domain.SiginexPrincipal;

/**
 * Business orchestration for authentication. Sits between the controller and
 * the service: it decides whether a use case may proceed and delegates the data
 * work to the service layer. Implemented by {@link AuthDelegateImpl}.
 */
public interface AuthDelegate {

    AuthService.IssuedSession login(String email, String password, String userAgent, String ip);

    void logout(String tokenHash, SiginexPrincipal principal);
}
