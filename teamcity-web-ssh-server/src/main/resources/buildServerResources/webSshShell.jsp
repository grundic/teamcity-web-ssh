<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include.jsp" %>

<bs:page>
<jsp:attribute name="head_include">
    <bs:linkScript>
        ${teamcityPluginResourcesPath}js/webSshShell.js
        ${teamcityPluginResourcesPath}js/colors.js
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
            WebSshShell.createShell(event, "${queryString}");
        });
    })();
    </script>
    </jsp:attribute>

    <jsp:attribute name="body_include">
        <div id="command" tabindex="1">
            <div id="terminal"></div>
        </div>

        <div id="error" class="error"></div>
    </jsp:attribute>
</bs:page>