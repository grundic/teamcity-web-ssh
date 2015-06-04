<%@ page import="jetbrains.buildServer.serverSide.crypt.RSACipher" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="bean" type="ru.mail.teamcity.ssh.config.HostBean" scope="request"/>
<jsp:useBean id="presets" type="java.util.List<ru.mail.teamcity.ssh.config.PresetBean>" scope="request"/>

<script language="JavaScript">

    var hostFields = {};

    function saveHostFields() {
        hostFields['login'] = $j("#login").val();
    }

    function setPresetFields(presetId) {
        $j.ajax({
            type: "GET",
            url: window['base_uri'] + '/webSshPresetResource.html',
            data: "id=" + presetId,
            success: function (response) {
                saveHostFields();
                $j("#login").val(response.login);
                $j("#login").prop('disabled', true);
                $j("#password").prop('disabled', true);
            }
        });
    }

    function setHostFields() {
        console.log(hostFields);
        if (hostFields['login'] != undefined) {
            $j("#login").val(hostFields['login']);
        }
        $j("#login").prop('disabled', false);
        $j("#password").prop('disabled', false);
    }

    function setPresetOrHostFields(presetId) {
        if (presetId != "") {
            setPresetFields(presetId);
        } else {
            setHostFields();
        }
    }

    $j(document).ready(function () {
        setPresetOrHostFields($j('#presetId').val());

        $j('#presetId').change(function () {
            setPresetOrHostFields($j(this).val());
        });
    });

</script>

<div>
    <table class="sshHostFormTable">

        <tr id="presetContainer">
            <th><label for="host">Preset: <l:star/></label></th>
            <td>
                <forms:select name="presetId" enableFilter="true" style="width:25em">
                    <option value="">-- Select preset --</option>
                    <c:forEach var="preset" items="${presets}">
                        <c:set var="selected" value="${preset.id eq bean.presetId}"/>
                        <forms:option value="${preset.id}" selected="${selected}">
                            <c:out value="${preset.name}"/>
                        </forms:option>
                    </c:forEach>
                </forms:select>
            </td>
        </tr>

        <tr id="hostContainer">
            <th><label for="host">Host: <l:star/></label></th>
            <td>
                <forms:textField name="host" style="width:25em;"
                                 value="${bean.host}"/>
                <span class="error" id="errorHost"></span>
            </td>
        </tr>

        <tr id="portContainer">
            <th><label for="port">Port: <l:star/></label></th>
            <td>
                <forms:textField name="port" style="width:8em;" maxlength="6" value="${bean.port}"/>
                <span class="error" id="errorPort"></span>
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

