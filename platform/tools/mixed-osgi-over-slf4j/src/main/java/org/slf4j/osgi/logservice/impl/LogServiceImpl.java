/*
 * Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.slf4j.osgi.logservice.impl;

import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>LogServiceImpl</code> is a simple OSGi LogService implementation that delegates to a slf4j
 * Logger.
 */
public class LogServiceImpl implements LogService {

    private static final String UNKNOWN = "[Unknown]";

    private final Logger delegate;

    /**
     * Creates a new instance of LogServiceImpl.
     *
     * @param bundle The bundle to create a new LogService for.
     */
    public LogServiceImpl(Bundle bundle) {

        String name = bundle.getSymbolicName();
        Version version = bundle.getVersion();
        if (version == null) {
            version = Version.emptyVersion;
        }
        delegate = LoggerFactory.getLogger(name + '.' + version);
    }

	@SuppressWarnings("unchecked")
	@Override
	public <L extends org.osgi.service.log.Logger> L getLogger(String name, Class<L> loggerType) {
		if(loggerType == org.osgi.service.log.Logger.class) {
			return (L) new org.osgi.service.log.Logger(){
				private Logger delegate = LoggerFactory.getLogger(name);
				
				@Override
				public boolean isDebugEnabled() {
					return delegate.isDebugEnabled();
				}

				@Override
				public void debug(String message) {
					delegate.debug(message);
				}

				@Override
				public void debug(String format, Object arg) {
					delegate.debug(format, arg);
				}

				@Override
				public boolean isInfoEnabled() {
					return delegate.isInfoEnabled();
				}

				@Override
				public void info(String message) {
					delegate.info(message);
				}

				@Override
				public void info(String format, Object arg) {
					delegate.info(format, arg);
				}

				@Override
				public boolean isWarnEnabled() {
					return delegate.isWarnEnabled();
				}

				@Override
				public void warn(String message) {
					delegate.warn(message);
				}

				@Override
				public void warn(String format, Object arg) {
					delegate.warn(format,arg);
				}

				@Override
				public boolean isErrorEnabled() {
					return delegate.isErrorEnabled();
				}

				@Override
				public void error(String message) {
					delegate.error(message);
				}

				@Override
				public void error(String format, Object arg) {
					delegate.error(format, arg);
				}
			};
		}
		if(loggerType.isInterface())
			return null;
		return ReflectUtils.getTheBestInstance(loggerType, new Object[] {name});
	}
	
	@Override
    public void log(int level, String message) {

        switch (level) {
        case LOG_DEBUG:
            delegate.debug(message);
            break;
        case LOG_ERROR:
            delegate.error(message);
            break;
        case LOG_INFO:
            delegate.info(message);
            break;
        case LOG_WARNING:
            delegate.warn(message);
            break;
        default:
            break;
        }
    }

	@Override
    public void log(int level, String message, Throwable exception) {

        switch (level) {
        case LOG_DEBUG:
            delegate.debug(message, exception);
            break;
        case LOG_ERROR:
            delegate.error(message, exception);
            break;
        case LOG_INFO:
            delegate.info(message, exception);
            break;
        case LOG_WARNING:
            delegate.warn(message, exception);
            break;
        default:
            break;
        }
    }

	@Override
    public void log(ServiceReference sr, int level, String message) {

        switch (level) {
        case LOG_DEBUG:
            if (delegate.isDebugEnabled()) {
                delegate.debug(createMessage(sr, message));
            }
            break;
        case LOG_ERROR:
            if (delegate.isErrorEnabled()) {
                delegate.error(createMessage(sr, message));
            }
            break;
        case LOG_INFO:
            if (delegate.isInfoEnabled()) {
                delegate.info(createMessage(sr, message));
            }
            break;
        case LOG_WARNING:
            if (delegate.isWarnEnabled()) {
                delegate.warn(createMessage(sr, message));
            }
            break;
        default:
            break;
        }
    }

    /**
     * Formats the log message to indicate the service sending it, if known.
     *
     * @param sr the ServiceReference sending the message.
     * @param message The message to log.
     * @return The formatted log message.
     */
    private String createMessage(ServiceReference sr, String message) {

        StringBuilder output = new StringBuilder();
        if (sr != null) {
            output.append('[').append(sr.toString()).append(']');
        } else {
            output.append(UNKNOWN);
        }
        output.append(message);

        return output.toString();
    }

	@Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {

        switch (level) {
        case LOG_DEBUG:
            if (delegate.isDebugEnabled()) {
                delegate.debug(createMessage(sr, message), exception);
            }
            break;
        case LOG_ERROR:
            if (delegate.isErrorEnabled()) {
                delegate.error(createMessage(sr, message), exception);
            }
            break;
        case LOG_INFO:
            if (delegate.isInfoEnabled()) {
                delegate.info(createMessage(sr, message), exception);
            }
            break;
        case LOG_WARNING:
            if (delegate.isWarnEnabled()) {
                delegate.warn(createMessage(sr, message), exception);
            }
            break;
        default:
            break;
        }
    }
}
