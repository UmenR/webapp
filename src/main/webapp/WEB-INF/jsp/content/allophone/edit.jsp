<content:title>
    <fmt:message key="edit.allophone" />
</content:title>

<content:section cssId="allophoneEditPage">
    <h4><content:gettitle /></h4>
    <div class="card-panel">
        <form:form modelAttribute="allophone">
            <tag:formErrors modelAttribute="allophone" />

            <div class="row">
                <form:hidden path="locale" value="${contributor.locale}" />
                <form:hidden path="revisionNumber" value="${allophone.revisionNumber}" />
                
                <div class="input-field col s12">
                    <form:label path="valueIpa" cssErrorClass="error"><fmt:message key='value' /> (IPA)</form:label>
                    <form:input path="valueIpa" cssErrorClass="error" />
                </div>
                
                <div class="input-field col s12">
                    <form:label path="valueSampa" cssErrorClass="error"><fmt:message key='value' /> (X-SAMPA)</form:label>
                    <form:input path="valueSampa" cssErrorClass="error" />
                </div>
            </div>

            <button id="submitButton" class="btn waves-effect waves-light" type="submit">
                <fmt:message key="edit" /> <i class="material-icons right">send</i>
            </button>
        </form:form>
    </div>
    
    <div class="divider"></div>
    
    <h5><fmt:message key="revisions" /></h5>
    <table class="bordered highlight">
        <thead>
            <th><fmt:message key="revision" /></th>
            <th><fmt:message key="time" /></th>
            <th><fmt:message key="contributor" /></th>
        </thead>
        <tbody>
            <c:forEach var="contentCreationEvent" items="${contentCreationEvents}" varStatus="status">
                <tr>
                    <td>${fn:length(contentCreationEvents) - status.index}</td>
                    <td><fmt:formatDate value="${contentCreationEvent.calendar.time}" type="both" timeStyle="short" /></td>
                    <td>
                        <a href="<spring:url value='/content/community/contributors' />" target="_blank">
                            <div class="chip">
                                <img src="<spring:url value='${contentCreationEvent.contributor.imageUrl}' />" alt="${contentCreationEvent.contributor.firstName}" /> 
                                <c:out value="${contentCreationEvent.contributor.firstName}" />&nbsp;<c:out value="${contentCreationEvent.contributor.lastName}" />
                            </div>
                        </a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</content:section>

<content:aside>
    <h5 class="center"><fmt:message key="preview" /></h5>
    
    <div class="previewContainer valignwrapper">
        <img src="<spring:url value='/img/device-pixel-c.png' />" alt="<fmt:message key="preview" />" />
        <div id="previewContentContainer">
            <div id="previewContent" class="previewContentGrapheme">

            </div>
        </div>
    </div>
    <script>
        $(function() {
            initializePreview();
            
            $('#valueIpa').on("change", function() {
                console.debug('#valueIpa on change');
                initializePreview();
            });
            
            function initializePreview() {
                console.debug('initializePreview');
                var value = $('#valueIpa').val();
                if (value != '') {
                    $('#previewContent').html('/' + value + '/');
                }
            };
        });
    </script>
</content:aside>