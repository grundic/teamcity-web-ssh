"use strict";

var WebSshShell = {
    createShell: function (event, params) {
        var subSocket;
        var socket = atmosphere;
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

        };

        request.onReopen = function (response) {

        };

        // For demonstration of how you can customize the fallbackTransport using the onTransportFailure function
        request.onTransportFailure = function (errorMsg, request) {

        };

        request.onMessage = function (response) {
            var message = response.responseBody;
            term.write(atmosphere.util.parseJSON(message));
        };

        request.onClose = function (response) {
            term.destroy();
        };

        request.onError = function (response) {
        };

        request.onReconnect = function (request, response) {

        };

        subSocket = socket.subscribe(request);

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
            //socket.emit('data', data);
            subSocket.push(atmosphere.util.stringifyJSON({'stdin': data}));
        });

        term.on('title', function (title) {
            document.title = title;
        });

        term.open($j("#" + shellId)[0]);
        term.write('\x1b[31mWelcome to term.js!\x1b[m\r\n');
    }
};