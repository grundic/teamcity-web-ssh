"use strict";

var WebSshShell = {
    terminals: {},

    createShell: function (event, hostId, hostTheme) {
        var subSocket;
        var socket = atmosphere;
        var transport = 'websocket';
        var controllerUrl = '/webSsh.html';
        var shellId = 'terminal';
        var term;

        var request = {
            url: base_uri + controllerUrl + '?id=' + hostId,
            contentType: "application/json",
            logLevel: 'info',
            transport: transport,
            trackMessageLength: true,
            reconnectOnServerError: false,
            reconnect: false
        };

        request.onOpen = function (response) {
            term = new Terminal({
                cols: 80,
                rows: 24,
                //screenKeys: true,
                //useStyle: true
                screenKeys: false,
                useStyle: true,
                cursorBlink: true,
                convertEol: true,
                colors: webSshColorschemes[hostTheme]
            });

            term.on('data', function (data) {
                subSocket.push(atmosphere.util.stringifyJSON({'stdin': data, 'uuid': this.uuid}));
            });

            term.on('title', function (title) {
                document.title = title;
            });

            term.open($(shellId));

            $j('#terminal div:first-child').resizable({
                ghost: true,
                stop: function (event, ui) {
                    WebSshShell.resizeTerminal(
                        term,
                        subSocket,
                        this.uuid,
                        ui.originalSize.width,
                        ui.size.width,
                        ui.originalSize.height,
                        ui.size.height
                    );
                }
            });

            // stretch terminal to window width
            WebSshShell.resizeTerminal(
                term,
                subSocket,
                this.uuid,
                $j('#terminal div:first-child').width(),
                $j('#terminal').width(),
                $j('#terminal div:first-child').height(),
                $j('#terminal div:first-child').height()
            );

            WebSshShell.handleThemeChange(term);
            $j('#themeContainer').show();
        };

        request.onClientTimeout = function (r) {
            console.log("'onClientTimeout' called!");
        };

        request.onReopen = function (response) {
            console.log("'onReopen' called!");
        };

        // For demonstration of how you can customize the fallbackTransport using the onTransportFailure function
        request.onTransportFailure = function (errorMsg, request) {
            console.log("'onTransportFailure' called!");
        };

        request.onMessage = function (response) {
            var message = response.responseBody;
            var jsonMessage = atmosphere.util.parseJSON(message);
            if (jsonMessage != null && jsonMessage['error'] != null){
                WebSshShell.ErrorDialog.show(
                    jsonMessage.error.title,
                    '<p class="error">' + jsonMessage.error.content + '</p>'
                );

                subSocket.disconnect();
                return;
            }

            term.write(jsonMessage);
        };

        request.onClose = function (response) {
            if (term != null) {
                term.destroy();
            }
        };

        request.onError = function (response) {
            console.log("'onError' called!");
        };

        request.onReconnect = function (request, response) {
            console.log("'onReconnect' called!");
        };

        subSocket = socket.subscribe(request);
    },

    resizeTerminal: function (term, subSocket, uuid, oldWidth, newWidth, oldHeight, newHeight) {
        var x = (newWidth / oldWidth) * term.cols | 0;
        var y = (newHeight / oldHeight) * term.rows | 0;

        term.resize(x, y);
        var resizeData = atmosphere.util.stringifyJSON({'x': x, 'y': y});
        subSocket.push(atmosphere.util.stringifyJSON({'resize': resizeData, 'uuid': uuid}));
    },


    handleThemeChange: function (term) {
        $j("#theme").change(function () {
            var colors = webSshColorschemes[$j(this).val()];
            term.colors = colors.slice(0, -2).concat(Terminal._colors.slice(16, -2), colors.slice(-2));
            $j(".terminal")[0].style.backgroundColor = term.colors[256];
            $j(".terminal")[0].style.color = term.colors[257];

            term.resize(term.cols, term.rows); //refresh
        });
    }
};