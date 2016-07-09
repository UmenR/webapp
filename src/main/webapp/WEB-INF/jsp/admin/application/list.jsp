<content:title>
    <fmt:message key="applications" />
</content:title>

<content:section cssId="applicationListPage">
    <div class="section row">
        <c:if test="${empty applications}">
            <p>
                <fmt:message key="to.add.new.content.click.the.button.below" />
            </p>
        </c:if>
        
        <c:if test="${not empty applications}">
            <table class="bordered highlight">
                <thead>
                    <th><fmt:message key="package.name" /></th>
                    <th><fmt:message key="literacy.skills" /></th>
                    <th><fmt:message key="numeracy.skills" /></th>
                </thead>
                <tbody>
                    <c:forEach var="application" items="${applications}">
                        <tr>
                            <td><a href="<spring:url value='/admin/application/edit/${application.id}' />">${application.packageName}</a></td>
                            <td>${application.literacySkills}</td>
                            <td>${application.numeracySkills}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>
    </div>
    
    <div class="fixed-action-btn" style="bottom: 2em; right: 2em;">
        <a href="<spring:url value='/admin/application/create' />" class="btn-floating btn-large red tooltipped" data-position="left" data-delay="50" data-tooltip="<fmt:message key="add.application" />"><i class="material-icons">add</i></a>
    </div>
</content:section>