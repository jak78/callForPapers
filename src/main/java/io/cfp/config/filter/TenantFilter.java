/*
 * Copyright (c) 2016 BreizhCamp
 * [http://breizhcamp.org]
 *
 * This file is part of CFP.io.
 *
 * CFP.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.cfp.config.filter;

import io.cfp.entity.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class TenantFilter extends OncePerRequestFilter {

    public static final String REFERER = "Referer";

    public static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final URL url = new URL(request.getRequestURL().toString());
        String host = url.getHost();

        String eventId = "demo";
        String referer = request.getHeader(REFERER);
        String header = request.getHeader(TENANT_HEADER);
        final String path = url.getPath();
        int i;
        if (path.startsWith("/events/") && (i = path.indexOf('/', 8)) > 0) {
            eventId = path.substring(8, i);
        } else if (referer != null && referer.endsWith(".cfp.io")) {
            eventId = host.substring(0, host.indexOf('.'));
        } else if (header != null) {
            eventId = header;
        } else if (host.endsWith(".cfp.io")) {
            eventId = host.substring(0, host.indexOf('.'));
        } else {
            logger.warn("Can't determine current event from requested host '"+host+" from "+request.getRequestURL()+". Fallback to 'demo'");
        }
        MDC.put("event.id", eventId);
        Event.setCurrent(eventId.toLowerCase());
        try {
            filterChain.doFilter(request, response);
        } finally {
            Event.unsetCurrent();
            MDC.remove("event.id");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);
}
