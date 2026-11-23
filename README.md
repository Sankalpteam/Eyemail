# Eyemail

Business mail on `@eyemail.in`. Free on the free plan: 1 GB storage, unlimited addresses on
the domain, 50 mails a day to outside addresses, and spam kept out.

This repository holds three things that work together.

| Folder | What it is | Where it runs |
|---|---|---|
| `web/` | the site: landing page, sign in, sign up, reset, and the mailbox | GitHub Pages, which also serves eyemail.in |
| `native/` | the Android app, Kotlin and Jetpack Compose | built by GitHub Actions, installed from the artifact |
| `branding/` | the icon, in every size, made by one script | read by both of the above |

The server is a separate repository, `Sankalpteam/Eyemail-Backend`: a single FastAPI service
that sends mail through Brevo, writes inbound mail into Firestore, and holds the account
endpoints. It runs on Render.

## How the three fit

```
Cloudflare Email Routing  ->  worker.js  ->  POST /webhook/inbound  ->  Firestore
browser or app            ->  POST /api/send-email  ->  Brevo  ->  the world
browser or app            ->  Firestore directly, for reading and moving mail
```

A person signs in with Firebase Auth. Their uid is their address, so `yashwanth@eyemail.in`
is both the login and the mailbox. The app and the web both read mail straight from
Firestore, which is why the security rules in `firestore.rules` matter.

## Run the web locally

Any static server works, because there is no build step:

    cd web
    python3 -m http.server 5500

Then open http://localhost:5500. The pages call the live backend; nothing else is needed.

## Build the app

The GitHub Actions workflow builds on every push that touches `native/`, and uploads
`eyemail-debug-apk` as an artifact. To build by hand, open `native/` in Android Studio.

The app needs three values at build time, read from `native/local.properties` or from the
environment, so nothing secret is ever committed:

| Name | What it is |
|---|---|
| `BACKEND_URL` | `https://eyemail-backend.onrender.com` |
| `FIREBASE_API_KEY` | the public web key from `web/firebase-config.js` |
| `FIRESTORE_PROJECT` | `eyemail-c3398` |

In CI these come from repository secrets of the same names. The app shows the commit it was
built from in Settings, About, Build, so an install can always be identified.

## The server

Environment variables, set on Render, never in the repository:

| Name | What it is |
|---|---|
| `FIREBASE_SERVICE_ACCOUNT_JSON` | the whole service account file, not only the private key |
| `FIREBASE_DATABASE_URL` | the Realtime Database URL |
| `BREVO_API_KEY` | outbound mail |
| `INTERNAL_API_SECRET` | the key Indo Account sends with `/api/internal/send-mail` |
| `WEBHOOK_SECRET` | the secret Cloudflare sends with `/webhook/inbound` |
| `ALLOWED_ORIGINS` | comma separated list of sites allowed to call the API |
| `DAILY_EXTERNAL_LIMIT` | outside mails per day, per account |

## Security

`firestore.rules` is the only thing between one customer's mail and another's. Read
`FIRESTORE_RULES.md` before changing it, and deploy it in the Firebase console under
Firestore Database, Rules. `firestore.rules.test.md` lists the cases it must answer.

## House rules

- The look must match on the web and in the app: same blue, same spacing, same words.
- Never commit `serviceAccountKey.json`, `local.properties`, or any key. The public web key
  is the one exception, and it belongs in `web/firebase-config.js`.
- The free plan wording is part of the product: a limit is a free plan limit, and the reason
  is spam and abuse, never that mail is short.
- A change to mail behaviour is a change in `main.py` or `worker.js`. A change to how a
  screen looks is a change in `web/` and in `native/`, both, or it is half done.
