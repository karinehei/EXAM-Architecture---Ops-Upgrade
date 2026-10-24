/**
 * Serves the demo build under the GitHub Pages subpath so local preview
 * matches https://karinehei.github.io/EXAM-Architecture---Ops-Upgrade/
 *
 * Cross-platform: Node only (no shell mkdir/cp assumptions).
 */
const fs = require('fs');
const path = require('path');
const http = require('http');

const SUBPATH = '/EXAM-Architecture---Ops-Upgrade';
const PORT = Number(process.env.PORT || 4173);
const distRoot = path.join(__dirname, '..', 'dist', 'exam-frontend');

function resolveDistDir() {
  const direct = path.join(distRoot, 'index.html');
  if (fs.existsSync(direct)) {
    return distRoot;
  }
  const browser = path.join(distRoot, 'browser', 'index.html');
  if (fs.existsSync(browser)) {
    return path.join(distRoot, 'browser');
  }
  console.error(
    'Demo build not found. Run `npm run build:demo` first.\n' +
      `Looked for index.html under ${distRoot}`
  );
  process.exit(1);
}

const assetRoot = resolveDistDir();

function contentType(filePath) {
  const ext = path.extname(filePath).toLowerCase();
  switch (ext) {
    case '.html':
      return 'text/html; charset=utf-8';
    case '.js':
      return 'application/javascript; charset=utf-8';
    case '.css':
      return 'text/css; charset=utf-8';
    case '.json':
      return 'application/json; charset=utf-8';
    case '.ico':
      return 'image/x-icon';
    case '.svg':
      return 'image/svg+xml';
    case '.png':
      return 'image/png';
    case '.woff2':
      return 'font/woff2';
    default:
      return 'application/octet-stream';
  }
}

function send(res, status, body, type) {
  res.writeHead(status, {
    'Content-Type': type,
    'Cache-Control': 'no-store'
  });
  res.end(body);
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url || '/', `http://127.0.0.1:${PORT}`);
  let pathname = decodeURIComponent(url.pathname);

  if (pathname === '/' || pathname === SUBPATH) {
    res.writeHead(302, { Location: `${SUBPATH}/` });
    res.end();
    return;
  }

  if (!pathname.startsWith(`${SUBPATH}/`) && pathname !== SUBPATH) {
    send(
      res,
      404,
      `Not found. Open http://127.0.0.1:${PORT}${SUBPATH}/\n`,
      'text/plain; charset=utf-8'
    );
    return;
  }

  let rel = pathname.slice(SUBPATH.length);
  if (rel === '' || rel === '/') {
    rel = '/index.html';
  }

  const safeRel = path.normalize(rel).replace(/^(\.\.[/\\])+/, '');
  const filePath = path.join(assetRoot, safeRel);

  if (!filePath.startsWith(assetRoot)) {
    send(res, 403, 'Forbidden\n', 'text/plain; charset=utf-8');
    return;
  }

  fs.readFile(filePath, (err, data) => {
    if (err) {
      // SPA fallback for non-hash path mistakes
      const indexPath = path.join(assetRoot, 'index.html');
      fs.readFile(indexPath, (indexErr, indexData) => {
        if (indexErr) {
          send(res, 404, 'Not found\n', 'text/plain; charset=utf-8');
          return;
        }
        send(res, 200, indexData, 'text/html; charset=utf-8');
      });
      return;
    }
    send(res, 200, data, contentType(filePath));
  });
});

server.listen(PORT, '127.0.0.1', () => {
  console.log(`Demo preview: http://127.0.0.1:${PORT}${SUBPATH}/`);
  console.log(`Serving assets from: ${assetRoot}`);
});
