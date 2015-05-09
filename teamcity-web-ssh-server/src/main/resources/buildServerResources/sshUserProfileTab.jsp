<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="ext" tagdir="/WEB-INF/tags/ext" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>

<jsp:useBean id="hosts" scope="request"
             type="java.util.List<ru.mail.teamcity.ssh.config.HostBean>"/>

<c:set var="controllerAjaxUrl"><c:url value="/sshUserProfile.html"/></c:set>
<c:set var="formId"><c:url value="webSshHostForm"/></c:set>


<div class="section noMargin">
    <h2 class="noBorder">Ssh hosts configuration</h2>

    <div class="sidebarNote">
        This is going to be a description.
        A lot of lines here with couple of useful information. In case user is interested
        he/she always can read more here.
    </div>

    <c:choose>
        <c:when test="${not empty hosts}">
            <l:tableWithHighlighting className="webSshHosts" highlightImmediately="true">
                <tr class="header">
                    <th colspan="3">Configured hosts</th>
                </tr>
                <c:forEach var="host" items="${hosts}">
                    <%--<c:set var="onclick">BS.AgentPush.updateHost(event, '<bs:forJs>${pr.id}</bs:forJs>');</c:set>--%>
                    <tr>
                        <td class="highlight" title="<DESCRIPTION>" onclick="">
                                ${host.host}:${host.port}
                                <%--<c:if test="${not empty pr.description}">--%>
                                <%--<span style="color: #707070">(${pr.description})</span>--%>
                                <%--</c:if>--%>
                        </td>
                        <td class="highlight edit">
                            <a href="#"
                               onclick="return BS.WebSshConfiguration.showDialog(event, '${formId}', '${controllerAjaxUrl}', 'id=${host.id}'); return false">
                                edit
                            </a>
                        </td>
                        <td class="highlight edit">
                            <a href="#"
                               onclick="BS.WebSshConfiguration.DeleteHostDialog.showDialog('${host.id}', '${util:forJS(host.host, true, true)}'); return false">
                                delete
                            </a>
                        </td>
                    </tr>
                </c:forEach>
            </l:tableWithHighlighting>
        </c:when>
        <c:otherwise>
            <p>No hosts are configured yet.</p>
        </c:otherwise>
    </c:choose>

    <p>
        <forms:addButton
                onclick="return BS.WebSshConfiguration.showDialog(event, '${formId}', '${controllerAjaxUrl}', ''); return false">
            Add new host
        </forms:addButton>
    </p>

    <bs:modalDialog
            formId="${formId}"
            title="Host configuration"
            action="${controllerAjaxUrl}"
            closeCommand="BS.WebSshConfiguration.CreateHostDialog.close()"
            saveCommand="BS.WebSshConfiguration.CreateHostDialog.submit()"
            >
        <bs:refreshable containerId="webSshHostFormMainRefresh" pageUrl="${controllerAjaxUrl}"/>
    </bs:modalDialog>

    <bs:modalDialog formId="webSshHostDeleteForm"
                    title="Delete host"
                    action="${controllerAjaxUrl}"
                    closeCommand="BS.WebSshConfiguration.DeleteHostDialog.close()"
                    saveCommand="BS.WebSshConfiguration.DeleteHostDialog.submit('${controllerAjaxUrl}')">
        Are you sure you want to delete "<span id="webSshHostDeleteName"></span>"?
        <input type="hidden" name="webSshHostDeleteId" id="webSshHostDeleteId"/>

        <div class="popupSaveButtonsBlock">
            <forms:submit label="Delete"/>
            <forms:cancel onclick="BS.WebSshConfiguration.DeleteHostDialog.close(); return false" label="Cancel"/>
        </div>
    </bs:modalDialog>
</div>
