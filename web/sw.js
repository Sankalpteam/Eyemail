/* Eyemail service worker.
   Two jobs: show a notification when the server pushes one, and open the mail when
   the person taps it. Nothing is cached yet, so the shell always comes from the network. */

self.addEventListener("install", () => self.skipWaiting());
self.addEventListener("activate", (event) => event.waitUntil(self.clients.claim()));

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
