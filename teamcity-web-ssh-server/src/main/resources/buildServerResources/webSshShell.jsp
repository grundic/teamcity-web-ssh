<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include.jsp" %>

<bs:page>
<jsp:attribute name="head_include">
    <bs:linkScript>
        ${teamcityPluginResourcesPath}js/webSshShell.js
        ${teamcityPluginResourcesPath}lib/term.js
    </bs:linkScript>

    <script type="text/javascript">
    (function (event) {
        $j(document).ready(function (event) {
            $j(document).unbind("keydown");
            WebSshShell.createShell(event, "${queryString}");
        });
    })();
    </script>
    </jsp:attribute>

    <jsp:attribute name="body_include">
        <div id="command" tabindex="1" style="background-color: crimson">
            <div id="terminal"></div>
        </div>
    </jsp:attribute>
</bs:page>