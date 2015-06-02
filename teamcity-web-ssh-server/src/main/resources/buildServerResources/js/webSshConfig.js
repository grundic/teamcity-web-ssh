BS.WebSshConfigurationListener = OO.extend(BS.ErrorsAwareListener, {

    onEmptyHostError: function (elem) {
        $("errorHost").innerHTML = elem.firstChild.nodeValue;
        this.getForm().highlightErrorField($("host"));
    },

    onBadHostValueError: function (elem) {
        $("errorHost").innerHTML = elem.firstChild.nodeValue;
        this.getForm().highlightErrorField($("host"));
    },

    onBadPortValueError: function (elem) {
        $("errorPort").innerHTML = elem.firstChild.nodeValue;
        this.getForm().highlightErrorField($("port"));
    },

    onEmptyLoginError: function (elem) {
        $("errorLogin").innerHTML = elem.firstChild.nodeValue;
        this.getForm().highlightErrorField($("login"));
    },

    onEmptyPasswordError: function (elem) {
        $("errorPassword").innerHTML = elem.firstChild.nodeValue;
        this.getForm().highlightErrorField($("password"));
    },

    onEmptyKeyFilePathError: function (elem) {
        $("errorKeyFilePath").innerHTML = elem.firstChild.nodeValue;
        this.getForm().highlightErrorField($("keyFilePath"));
    },

    onIoExceptionError: function (elem) {
        $("errorVarious").innerHTML = elem.firstChild.nodeValue;
    },

    onJaxbExceptionError: function (elem) {
        $("errorVarious").innerHTML = elem.firstChild.nodeValue;
    },

    onCompleteSave: function (form, responseXML, err) {
        BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
        if (!err) {
            //BS.XMLResponse.processRedirect(responseXML);
            document.location.reload();
        }
    }
});


BS.WebSshConfiguration = {
    _containerElement: null,
    _formElement: null,
    _dialogElement: null,

    showDialog: function (e, formId, controllerAjaxUrl, params) {
        BS.WebSshConfiguration.CreateHostDialog.showProgress(e);

        this._formElement = $(formId);
        this._dialogElement = $(formId + "Dialog");
        this._containerElement = $(formId + "MainRefresh");

        BS.ajaxUpdater(this._containerElement, base_uri + controllerAjaxUrl + "?" + params,
            {
                method: "get",
                evalScripts: true,
                onComplete: function () {

                    BS.WebSshConfiguration.CreateHostDialog.hideProgress();

                    BS.WebSshConfiguration.CreateHostDialog.baseParams = function () {
                        return params;
                    };
                    ((function () {
                        var dialog = $(this.getContainer());
                        var pos = BS.Util.computeCenter(dialog);
                        this.showAt(pos[0], 100);
                    }).bind(BS.WebSshConfiguration.CreateHostDialog))();
                }
            });

        return false;
    },

    CreateHostForm: OO.extend(BS.AbstractPasswordForm, {
        formElement: function () {
            return BS.WebSshConfiguration._formElement;
        },

        getDialog: function () {
            return BS.WebSshConfiguration.CreateHostDialog;
        },

        beforeShow: function () {
            this.clearErrors();
            BS.Util.reenableForm(this.formElement());
        },

        saveForm: function () {
            var that = this;
            BS.PasswordFormSaver.save(this, this.formElement().action, OO.extend(BS.WebSshConfigurationListener, {
                getForm: function () {
                    return that;
                },

                onCompleteSave: function (form, responseXML, err) {
                    BS.WebSshConfigurationListener.onCompleteSave(form, responseXML, err);

                    if (err) {
                        BS.Util.reenableForm(that.formElement());
                        return;
                    }

                    that.getDialog().close();
                }
            }));
            return false;
        }
    }),

    CreateHostDialog: OO.extend(BS.DialogWithProgress, {
        getContainer: function () {
            return BS.WebSshConfiguration._dialogElement;
        },

        getForm: function () {
            return BS.WebSshConfiguration.CreateHostForm;
        },

        beforeShow: function () {
            BS.WebSshConfiguration._dialogElement.style.width = "35em";
            this.getForm().beforeShow();
        },

        submit: function () {
            this.getForm().saveForm();
            return false;
        },

        close: function () {
            this.doClose();
            BS.WebSshConfiguration._containerElement.descendants().each(function (elem) {
                Event.stopObserving(elem);
            });
            BS.WebSshConfiguration._containerElement.update();

            var children = BS.WebSshConfiguration._containerElement.childNodes;
            for (var i = 0; i < children.length; ++i) {
                BS.WebSshConfiguration._containerElement.removeChild(children[i]);
            }
        }
    }),

    DeleteHostDialog: OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('webSshHostDeleteFormDialog');
        },

        showDialog: function (hostId, hostName) {
            $('webSshHostDeleteId').value = hostId;
            $('webSshHostDeleteName').innerHTML = hostName;
            this.showCentered();
        },

        submit: function (url) {
            var that = this;
            BS.ajaxRequest(base_uri + url + "?delete=true&id=" + $('webSshHostDeleteId').value, {
                method: 'post',
                onComplete: function (transport) {
                    document.location.reload();
                    that.close();
                }
            });
            return false;
        }
    }),

    DeletePresetDialog: OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('webSshPresetDeleteForm');
        },

        showDialog: function (hostId, hostName) {
            $('webSshPresetDeleteId').value = hostId;
            $('webSshPresetDeleteName').innerHTML = hostName;
            this.showCentered();
        },

        submit: function (url) {
            var that = this;
            BS.ajaxRequest(base_uri + url + "?delete=true&id=" + $('webSshPresetDeleteId').value, {
                method: 'post',
                onComplete: function (transport) {
                    document.location.reload();
                    that.close();
                }
            });
            return false;
        }
    })
};