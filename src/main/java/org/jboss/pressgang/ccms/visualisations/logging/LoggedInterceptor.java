package org.jboss.pressgang.ccms.visualisations.logging;

import org.jetbrains.annotations.NotNull;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * A logging interceptor.
 */
@Logged
@Interceptor
public class LoggedInterceptor implements Serializable{
    private static final Logger LOGGER = Logger.getLogger(LoggedInterceptor.class.getName());

    public LoggedInterceptor() {
    }

    @AroundInvoke
    public Object logMethodEntry(@NotNull final InvocationContext invocationContext) throws Exception {
        LOGGER.info("Entering method: "
                + invocationContext.getMethod().getName() + " in class "
                + invocationContext.getMethod().getDeclaringClass().getName());

        final Object retValue = invocationContext.proceed();

        LOGGER.info("Exiting method: "
                + invocationContext.getMethod().getName() + " in class "
                + invocationContext.getMethod().getDeclaringClass().getName());

        return retValue;
    }
}
