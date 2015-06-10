package ru.mail.teamcity.ssh;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Author: g.chernyshev
 * Date: 09.06.15
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext context = null;

    public ApplicationContext getApplicationContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

}
