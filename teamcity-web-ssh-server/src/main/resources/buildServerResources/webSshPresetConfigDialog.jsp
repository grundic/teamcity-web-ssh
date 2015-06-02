<%@ page import="jetbrains.buildServer.serverSide.crypt.RSACipher" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="bean" type="ru.mail.teamcity.ssh.config.PresetBean" scope="request"/>


<div>
    <table class="sshHostFormTable">
        <tr id="nameContainer">
            <th><label for="name">Name: <l:star/></label></th>
            <td>
                <forms:textField name="name" style="width:25em;"
                                 value="${bean.name}"/>
                <span class="error" id="errorName"></span>
            </td>
        </tr>

        <%@ include file="webSshCredentialsConfig.jsp" %>
    </table>

    <span class="error" id="errorVarious"></span>

    <input type="hidden" id="id" name="id" value="${bean.id}"/>
    <input type="hidden" name="publicKey" id="publicKey"
           value="<c:out value='<%=RSACipher.getHexEncodedPublicKey()%>'/>"/>
</div>

<div class="popupSaveButtonsBlock">
    <forms:submit label="Save"/>
    <forms:cancel onclick="BS.WebSshConfiguration.CreateHostDialog.close()"/>
</div>
<div class="clr"></div>

