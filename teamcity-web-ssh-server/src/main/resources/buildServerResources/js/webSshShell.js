"use strict";

var WebSshShell = {
    terminals: {},

    createShell: function (event, query) {
        var subSocket;
        var socket = atmosphere;
        var transport = 'websocket';
        var controllerUrl = '/webSsh.html';
        var shellId = 'terminal';
        var term;

        var request = {
            url: base_uri + controllerUrl + '?' + query,
            contentType: "application/json",
            logLevel: 'debug',
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
                convertEol: true
            });

            term.on('data', function (data) {
                subSocket.push(atmosphere.util.stringifyJSON({'stdin': data, 'uuid': this.uuid}));
            });

            term.on('title', function (title) {
                document.title = title;
            });

            term.open($(shellId));
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
    }
};