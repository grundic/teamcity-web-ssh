"use strict";

var WebSshCommon = {
    addThemesToSelect: function (selectId) {
        var sortedThemes = _.object(_.sortBy(_.pairs(webSshColorschemes), function (o) {
            return o[0];
        }));
        for (var theme in sortedThemes) {
            $j('<option value="' + theme + '">' + theme + '</option>').appendTo(selectId);
        }
    },

};
