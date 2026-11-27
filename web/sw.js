/* Eyemail service worker.
   Three jobs, in this order of importance:
   1. show the notification when the server pushes one, and open the mail on tap
   2. keep a copy of the shell, so the app opens without network
   3. stay out of the way of everything else: the mail itself always comes from the
      network, never from a cache, because a stale mailbox is worse than no mailbox */

const VERSION = "eyemail-v2";
const SHELL = [
  "./app.html",
  "./index.html",
  "./firebase-config.js",
  "./manifest.webmanifest",
  "../branding/app-icons/eyemail-192.png",
  "../branding/app-icons/eyemail-32.png",
  "../branding/app-icons/eyemail-icon.svg",
  "./favicon.ico",
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches
      .open(VERSION)
      // One missing file must not fail the whole install.
      .then((cache) => Promise.allSettled(SHELL.map((url) => cache.add(url))))
      .then(() => self.skipWaiting()),
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== VERSION).map((key) => caches.delete(key))))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener("fetch", (event) => {
  const request = event.request;
  if (request.method !== "GET") return;

  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return; // firebase, the backend, fonts: never cached

  // Pages: try the network, fall back to the copy we kept, so a dead connection still opens.
  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(VERSION).then((cache) => cache.put(request, copy));
          return response;
        })
        .catch(async () => {
          const cached = await caches.match(request);
          return cached || (await caches.match("./app.html")) || Response.error();
        }),
    );
    return;
  }

  // Our own files: serve the copy first, refresh it in the background.
  event.respondWith(
    caches.match(request).then((cached) => {
      const network = fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(VERSION).then((cache) => cache.put(request, copy));
          return response;
        })
        .catch(() => cached);
      return cached || network;
    }),
  );
});

self.addEventListener("push", (event) => {
  let payload = { title: "Eyemail", body: "New mail", url: "./app.html" };
  try {
    if (event.data) payload = Object.assign(payload, event.data.json());
  } catch (_) {
    if (event.data) payload.body = event.data.text();
  }

  event.waitUntil(
    self.registration.showNotification(payload.title, {
      body: payload.body,
      icon: "../branding/app-icons/eyemail-192.png",
      badge: "../branding/app-icons/eyemail-32.png",
      data: { url: payload.url },
      tag: "eyemail-mail",
      renotify: true,
    }),
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  const target = (event.notification.data && event.notification.data.url) || "./app.html";

  event.waitUntil(
    self.clients.matchAll({ type: "window", includeUncontrolled: true }).then((list) => {
      for (const client of list) {
        if (client.url.includes("app.html") && "focus" in client) return client.focus();
      }
      return self.clients.openWindow(target);
    }),
  );
});
