<tr id="loginContainer">
    <th><label for="login">Login: <l:star/></label></th>
    <td>
        <forms:textField
                name="login"
                style="width:25em;"
                value="${bean.login}"
                readonly="true"
                onfocus="this.removeAttribute('readonly');"
                noAutoComplete="true"/>
        <span class="error" id="errorLogin"></span>
    </td>
</tr>

<tr id="passwordContainer">
    <th><label for="password">Password: <l:star/></label></th>
    <td>
        <forms:passwordField
                name="password"
                encryptedPassword="${bean.encryptedPassword}"
                maxlength="80"
                style="width: 25em;"/>
        <span class="error" id="errorPassword"></span>
    </td>
</tr>

<tr id="privateKeyContainer">
    <th><label for="privateKey">Private key:</label></th>
    <td>
                <textarea
                        name="privateKey"
                        id="privateKey"
                        style="width:25em;"
                        readonly="true"
                        onfocus="this.removeAttribute('readonly');"
                        rows="7">${bean.privateKey}</textarea>
        <span class="error" id="errorPrivateKey"></span>
    </td>
</tr>