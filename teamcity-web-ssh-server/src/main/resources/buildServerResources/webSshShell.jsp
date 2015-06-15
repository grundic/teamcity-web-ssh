<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include.jsp" %>

<bs:page>
<jsp:attribute name="head_include">
    <bs:linkScript>
        ${teamcityPluginResourcesPath}js/webSshCommon.js
        ${teamcityPluginResourcesPath}js/webSshShell.js
        ${teamcityPluginResourcesPath}js/webSshColorSchemes.js
        ${teamcityPluginResourcesPath}js/webSshShellErrorDialog.js
        ${teamcityPluginResourcesPath}lib/term.js
        ${teamcityPluginResourcesPath}lib/jquery-ui.js
    </bs:linkScript>

    <bs:linkCSS>
        ${teamcityPluginResourcesPath}css/jquery-ui.css
        ${teamcityPluginResourcesPath}css/jquery-ui.structure.css
    </bs:linkCSS>


    <script type="text/javascript">
    (function (event) {
        $j(document).ready(function (event) {
            $j(document).unbind("keydown");

            WebSshCommon.addThemesToSelect('#theme');

            <c:if test="${not empty bean}">
            WebSshShell.createShell(event, "${bean.id}", "${bean.theme}");
            $j('#theme').val("${bean.theme}");
            </c:if>
        });
    })();
    </script>
    </jsp:attribute>

    <jsp:attribute name="body_include">
        <div id="command" tabindex="1">
            <div id="terminal"></div>
            <div id="themeContainer" style="display: none">
                <label for="theme">Select theme for terminal:</label>
                <select id="theme">
                    <option value="">-- Select theme --</option>
                </select>
            </div>
        </div>


        <c:choose>
            <c:when test="${not empty errors}">
                <div class="error">
                    <c:forEach var="error" items="${errors.errors}">
                        <p><c:out value="${error.id}: ${error.message}"/></p>
                    </c:forEach>
                </div>
            </c:when>
        </c:choose>
    </jsp:attribute>
</bs:page>