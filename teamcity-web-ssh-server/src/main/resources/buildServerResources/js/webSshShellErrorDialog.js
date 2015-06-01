WebSshShell.ErrorDialog = OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    getTitle: function () {
        return $('errorFormTitle');
    },

    getContainer: function () {
        return $('errorFormDialog');
    },

    formElement: function () {
        return $('errorForm');
    },

    getBody: function () {
        return $j("#errorFormDialog").find("div.modalDialogBody").get(0);
    },

    show: function (title, content) {
        this.getTitle().innerHTML = title;
        this.getBody().innerHTML = content;
        this.showCentered();
    }
}));