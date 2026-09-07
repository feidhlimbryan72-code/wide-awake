const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const FILE = path.join(__dirname, 'web', 'index.html');

const server = http.createServer((req, res) => {
    fs.readFile(FILE, (err, data) => {
        if (err) {
            res.writeHead(500);
            res.end('Error loading simulator');
            return;
        }
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.end(data);
    });
});

server.listen(PORT, () => {
    console.log(`Wide Awake simulator running at http://localhost:${PORT}`);
});
