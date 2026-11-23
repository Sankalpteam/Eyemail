# Firestore rules

`firestore.rules` in this folder is the only thing standing between one customer's mail and
another's. It is small on purpose.

## Deploy

Firebase console, Firestore Database, the Rules tab, paste the whole of `firestore.rules`,
press Publish. That is the only manual step.

If you prefer the command line:

    firebase deploy --only firestore:rules

## What it says, in one line each

1. A person may read and write only under their own uid, nothing else.
2. Mail in another person's account is refused even if the address is guessed, because the
   rule compares the signed in identity with the path.
3. A mail must carry one of the five folders the app knows, so nothing can be parked
   outside the app's view.
4. Everything not named is refused, so a new feature must be added here on purpose.

## What is not in Firestore

The profile, the storage counter and the username index live in the Realtime Database, and
the app only reads them. The backend writes them with a service account, which rules do not
apply to. There is no path in the app that needs to write them.

## After deploying

Open the app and use it normally for a minute: open a mail, star it, move it to Trash, empty
Trash, create a draft. If a rule is wrong, the app will say so on the first of those that
touches the missing permission, loudly, which is what we want.
