<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="ext" tagdir="/WEB-INF/tags/ext" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>

<jsp:useBean id="hosts" scope="request"
             type="java.util.List<ru.mail.teamcity.ssh.config.HostBean>"/>

<jsp:useBean id="presets" scope="request"
             type="java.util.List<ru.mail.teamcity.ssh.config.PresetBean>"/>

<jsp:useBean id="connections" scope="request"
             type="java.util.Collection<ru.mail.teamcity.ssh.shell.SshConnectionInfo>"/>

<jsp:useBean id="errors" scope="request"
             type="jetbrains.buildServer.controllers.ActionErrors"/>


<c:set var="hostDialogAjaxUrl"><c:url value="/webSshHostConfigController.html"/></c:set>
<c:set var="hostFormId"><c:url value="webSshHostForm"/></c:set>
<c:set var="presetDialogAjaxUrl"><c:url value="/webSshPresetConfigController.html"/></c:set>
<c:set var="presetFormId"><c:url value="webSshPresetForm"/></c:set>


<div class="section noMargin">
    <h2 class="noBorder">Ssh hosts configuration</h2>

    <div class="sidebarNote">
        On this page you can add personal ssh connections and later connect to configured host right from your browser.
        Add individual host clicking on "Add new host button". If you have similar credentials for multiple hosts, you
        can create preset clicking on "Add new preset button" and then use that preset in any host.
        Plugin also adds link on build agent page to connect to specific agent. If you have configured host for
        particular
        build agent, you could connect to it from agent's page.
    </div>

    <%-- Hosts block --%>
    <div>
        <c:choose>
            <c:when test="${not empty hosts}">
                <l:tableWithHighlighting className="webSshHosts" highlightImmediately="true">
                    <tr class="header">
                        <th colspan="4">Configured hosts</th>
                    </tr>
                    <c:forEach var="host" items="${hosts}">
                        <tr>
                            <td class="highlight">
                                    <span <c:if test="${not empty host.preset}">style="font-style: italic"</c:if>>
                                    ${host.delegate.login}@${host.host}:${host.port}
                                    </span>
                            </td>
                            <td class="highlight edit">
                                <a href="webSshShell.html?id=${host.id}" target="_blank">
                                    connect
                                </a>
                            </td>
                            <td class="highlight edit">
                                <a href="#"
                                   onclick="return BS.WebSshConfiguration.showDialog(event, '${hostFormId}', '${hostDialogAjaxUrl}', 'id=${host.id}'); return false">
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
                    onclick="return BS.WebSshConfiguration.showDialog(event, '${hostFormId}', '${hostDialogAjaxUrl}', ''); return false">
                Add new host
            </forms:addButton>
        </p>
    </div>

    <%-- Presets block --%>
    <div>
        <c:choose>
            <c:when test="${not empty presets}">
                <l:tableWithHighlighting className="webSshHosts" highlightImmediately="true">
                    <tr class="header">
                        <th colspan="3">Configured host presets</th>
                    </tr>
                    <c:forEach var="preset" items="${presets}">
                        <tr>
                            <td class="highlight">
                                    ${preset.name}
                            </td>

                            <td class="highlight edit">
                                <a href="#"
                                   onclick="return BS.WebSshConfiguration.showDialog(event, '${presetFormId}', '${presetDialogAjaxUrl}', 'id=${preset.id}'); return false">
                                    edit
                                </a>
                            </td>
                            <td class="highlight edit">
                                <c:choose>
                                    <c:when test="${preset.hosts.size() == 0}">
                                        <a href="#"
                                           onclick="BS.WebSshConfiguration.DeletePresetDialog.showDialog('${preset.id}', '${util:forJS(preset.name, true, true)}'); return false">
                                            delete
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color:#7F7F7F"
                                              title="There are ${preset.hosts.size()} usage(s) of this preset">delete</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </l:tableWithHighlighting>
            </c:when>
            <c:otherwise>
                <p>No host presets are configured yet.</p>
            </c:otherwise>
        </c:choose>

        <p>
            <forms:addButton
                    onclick="return BS.WebSshConfiguration.showDialog(event, '${presetFormId}', '${presetDialogAjaxUrl}', ''); return false">
                Add new preset
            </forms:addButton>
        </p>
    </div>

    <%-- Connections block --%>
    <c:choose>
        <c:when test="${not empty connections}">
            <div id="connections">
                <l:tableWithHighlighting className="webSshHosts" highlightImmediately="true">
                    <tr class="header">
                        <th colspan="4">Established connections</th>
                    </tr>
                    <c:forEach var="connection" items="${connections}">
                        <tr>
                            <td>${connection.channel.session.userName}@${connection.channel.session.host}:${connection.channel.session.port}</td>
                        </tr>
                    </c:forEach>

                </l:tableWithHighlighting>
            </div>
        </c:when>
        <c:otherwise>
            <p>There is no established connections.</p>
        </c:otherwise>
    </c:choose>

    <%-- Host modal dialogs --%>
    <bs:modalDialog
            formId="${hostFormId}"
            title="Host configuration"
            action="${hostDialogAjaxUrl}"
            closeCommand="BS.WebSshConfiguration.CreateHostDialog.close()"
            saveCommand="BS.WebSshConfiguration.CreateHostDialog.submit()"
            >
        <bs:refreshable containerId="webSshHostFormMainRefresh" pageUrl="${hostDialogAjaxUrl}"/>
    </bs:modalDialog>

    <bs:modalDialog formId="webSshHostDeleteForm"
                    title="Delete host"
                    action="${hostDialogAjaxUrl}"
                    closeCommand="BS.WebSshConfiguration.DeleteHostDialog.close()"
                    saveCommand="BS.WebSshConfiguration.DeleteHostDialog.submit('${hostDialogAjaxUrl}')">
        Are you sure you want to delete "<span id="webSshHostDeleteName"></span>"?
        <input type="hidden" name="webSshHostDeleteId" id="webSshHostDeleteId"/>

        <div class="popupSaveButtonsBlock">
            <forms:submit label="Delete"/>
            <forms:cancel onclick="BS.WebSshConfiguration.DeleteHostDialog.close(); return false" label="Cancel"/>
        </div>
    </bs:modalDialog>

    <%-- Preset modal dialogs --%>
    <bs:modalDialog
            formId="${presetFormId}"
            title="Preset configuration"
            action="${presetDialogAjaxUrl}"
            closeCommand="BS.WebSshConfiguration.CreateHostDialog.close()"
            saveCommand="BS.WebSshConfiguration.CreateHostDialog.submit()"
            >
        <bs:refreshable containerId="webSshPresetFormMainRefresh" pageUrl="${presetDialogAjaxUrl}"/>
    </bs:modalDialog>

    <bs:modalDialog formId="webSshPresetDeleteForm"
                    title="Delete preset"
                    action="${presetDialogAjaxUrl}"
                    closeCommand="BS.WebSshConfiguration.DeletePresetDialog.close()"
                    saveCommand="BS.WebSshConfiguration.DeletePresetDialog.submit('${presetDialogAjaxUrl}')">
        Are you sure you want to delete "<span id="webSshPresetDeleteName"></span>"?
        <input type="hidden" name="webSshPresetDeleteId" id="webSshPresetDeleteId"/>

        <div class="popupSaveButtonsBlock">
            <forms:submit label="Delete"/>
            <forms:cancel onclick="BS.WebSshConfiguration.DeletePresetDialog.close(); return false" label="Cancel"/>
        </div>
    </bs:modalDialog>
</div>

<%-- Errors block --%>
<c:choose>
    <c:when test="${not empty errors}">
        <div class="error">
            <c:forEach var="error" items="${errors.errors}">
                <p><c:out value="${error.id}: ${error.message}"/></p>
            </c:forEach>
        </div>
    </c:when>
</c:choose>