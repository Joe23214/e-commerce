package com.company.provacarrello.app;

import com.company.provacarrello.entity.User;
import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {
    @Autowired
    private ObjectProvider<SessionData> sessionDataProvider;

    @EventListener
    public void onAuthenticationSuccess(final AuthenticationSuccessEvent event) {
        try {
            User user = (User) event.getAuthentication().getPrincipal();

            String username = user.getUsername();
            if (!username.equalsIgnoreCase("system")) {
                Integer listino = 1;
                if (listino == null) {
                    listino = 1;
                }

                sessionDataProvider.getObject().setAttribute("listino", listino);
                sessionDataProvider.getObject().setAttribute("username", username);
            }
        }catch (Exception ex){
            System.out.println("onAuthenticationSucess : "+ex.getMessage());
        }
    }
}