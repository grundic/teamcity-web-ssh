"use strict";

var WebSshShell = {
    createShell: function (event, params) {
        var subSocket;
        var transport = 'websocket';
        var controllerUrl = '/webSsh.html';
        var shellId = 'terminal';

        var request = {
            url: base_uri + controllerUrl + '?' + params,
            contentType: "application/json",
            logLevel: 'debug',
            transport: transport,
            trackMessageLength: true,
            reconnectInterval: 5000
        };

        request.onOpen = function (response) {

        };

        request.onClientTimeout = function (r) {
            console.log("=== onClientTimeout ===");
        };

        request.onReopen = function (response) {
            console.log("=== onReopen ===");
        };

        // For demonstration of how you can customize the fallbackTransport using the onTransportFailure function
        request.onTransportFailure = function (errorMsg, request) {
            console.log("=== onTransportFailure ===");
        };

        request.onMessage = function (response) {
            console.log("<<onMessage>>");
            var message = response.responseBody;
            console.log(response.messages);
            console.log("Message from server: '" + message + "'");
            console.log("Escaped message: '" + escape(message) + "'");
            //term.write(response.messages[0]);

            for (var i = 0; i < response.messages.length; ++i) {
                term.write(response.messages[i]);

            }
        };

        request.onClose = function (response) {
            term.destroy();
        };

        request.onError = function (response) {
            console.log("=== onError ===");
        };

        request.onReconnect = function (request, response) {
            console.log("=== onReconnect ===");
        };

        subSocket = atmosphere.subscribe(request);

        console.log("Preparing to create ssh terminal");
        var term = new Terminal({
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
            //console.log(data);
            //term.write(data);
            subSocket.push(atmosphere.util.stringifyJSON({'stdin': data}));
        });

        term.on('title', function (title) {
            document.title = title;
        });

        term.open($j("#" + shellId)[0]);
        term.write('\x1b[31mWelcome to term.js!\x1b[m\r\n');
    }
};