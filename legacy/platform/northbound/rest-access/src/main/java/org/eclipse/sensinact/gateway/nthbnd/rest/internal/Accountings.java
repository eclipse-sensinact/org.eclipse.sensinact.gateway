/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * This class is the REST interface between each others classes that perform a task and jersey
 */
public class Accountings extends HttpServlet {
    public static final String ACCOUNTING = "accountings";

    /**
     * @throws IOException
     * @inheritDoc
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<AccountingEntry> accountings = (List<AccountingEntry>) super.getServletContext().getAttribute(ACCOUNTING);

        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>");
        builder.append("<html>");
        builder.append("<head>");
        builder.append("<title>sensiNact Accounting</title>");
        builder.append("<style>table {border-collapse: collapse;width: 100%;} th, td {text-align: left;padding: 8px;} tr:nth-child(even){background-color: #f2f2f2}</style>");
        builder.append("</head>");
        builder.append("<body>");
        builder.append("<table>");
        builder.append("<tr><th>Timestamp</th><th>Client</th><th>Uri</th><th>Method</th><th>Status</th></tr>");

        Iterator<AccountingEntry> iterator = accountings.iterator();
        while (iterator.hasNext()) {
            AccountingEntry entry = iterator.next();
            builder.append("<tr><td>");
            builder.append(entry.timestamp);
            builder.append("</td>");
            builder.append("<td>");
            builder.append(entry.client);
            builder.append("</td>");
            builder.append("<td>");
            builder.append(entry.uri);
            builder.append("</td>");
            builder.append("<td>");
            builder.append(entry.method);
            builder.append("</td>");
            builder.append("<td>");
            builder.append(entry.status);
            builder.append("</td></tr>");
        }
        builder.append("<table>");
        builder.append("</body>");
        builder.append("</html>");
        response.getWriter().println(builder.toString());
        response.flushBuffer();
    }
}
