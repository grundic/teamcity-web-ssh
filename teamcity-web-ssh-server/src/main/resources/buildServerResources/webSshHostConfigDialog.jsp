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
        hostFields['privateKey'] = $j("#privateKey").val();
    }

    function createElementClone(elementId, newValue) {
        var cloneElementId = elementId + 'Cloned';
        var realElement = $j('#' + elementId);
        var clonedElement = $j('#' + cloneElementId);
        if (clonedElement.length == 0) {
            clonedElement = realElement.clone(true);
        }
        realElement.hide();
        clonedElement.insertAfter(realElement);
        if (newValue != undefined) {
            clonedElement.val(newValue);
        }
        clonedElement.attr('name', elementId + 'Cloned');
        clonedElement.attr('id', elementId + 'Cloned');
        clonedElement.prop('disabled', true);
        clonedElement.show();
        return clonedElement;
    }

    function setPresetFields(presetId) {
        $j.ajax({
            type: "GET",
            url: window['base_uri'] + '/webSshPresetResource.html',
            data: "id=" + presetId,
            success: function (response) {
                saveHostFields();
                createElementClone('login', response.login);
                createElementClone('privateKey', response.privateKey);
                createElementClone('password');
            }
        });
    }

    function setHostFields() {
        if (hostFields['login'] != undefined) {
            $j("#login").val(hostFields['login']);
        }
        if (hostFields['privateKey'] != undefined) {
            $j("#privateKey").val(hostFields['privateKey']);
        }

        var loginCloned = createElementClone('login');
        var privateKeyCloned = createElementClone('privateKey');
        var passwordCloned = createElementClone('password');
        loginCloned.hide();
        privateKeyCloned.hide();
        passwordCloned.hide();
        $j("#login").prop('disabled', false);
        $j("#privateKey").prop('disabled', false);
        $j("#password").prop('disabled', false);
        $j("#login").show();
        $j("#privateKey").show();
        $j("#password").show();
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

        // configure theme select
        WebSshCommon.addThemesToSelect('#theme');
        $j('#theme').val("${bean.theme}");
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

        <tr id="themesContainer">
            <th><label for="themes">Terminal theme:</label></th>
            <td>
                <forms:select name="theme" enableFilter="true" style="width:25em">
                    <option value="">-- Select theme --</option>
                </forms:select>
                <span class="error" id="errorTheme"></span>
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

