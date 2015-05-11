<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include.jsp" %>

<bs:externalPage/>

<bs:linkScript>
    ${teamcityPluginResourcesPath}js/webSshShell.js
    ${teamcityPluginResourcesPath}lib/term.js
</bs:linkScript>


<script type="text/javascript">
    (function (event) {
        WebSshShell.createShell(event, 'id=${id}');

//        $j(document).keypress(function (e) {
//            var keyCode = (e.keyCode) ? e.keyCode : e.charCode;
//            console.log(keyCode);
//            console.log(String.fromCharCode(keyCode));
//        });
//
//        $j(document).keydown(function (e) {
//            var keyCode = (e.keyCode) ? e.keyCode : e.charCode;
//            console.log(keyCode);
//            console.log(String.fromCharCode(keyCode));
//        });
    })();
</script>


<div id="terminal"></div>
