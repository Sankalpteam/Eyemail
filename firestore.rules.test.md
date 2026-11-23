# Proving the rules, path by path

There is no emulator here, so these are the cases I checked by reading the rules against
the real paths the app and the backend use. Each line says what the rules must answer.

| Path | Who | Must answer | Why |
|---|---|---|---|
| `users/<me>/emails/<id>` read | me | allow | my own mail |
| `users/<me>/emails/<id>` read | another signed in person | refuse | the whole point |
| `users/<other>/emails/<id>` read | me | refuse | guessing an address must not work |
| `users` list | anyone | refuse | no listing the customer base |
| `users/<me>/emails` create with folder `inbox` | me | allow | the app moves mail |
| `users/<me>/emails` create with folder `archive` | me | refuse | only the five known folders |
| `users/<me>/emails/<id>` update | me | allow | read, star, label, move |
| `users/<me>/emails/<id>` delete | me | allow | permanent delete |
| `users/<me>/emails/<id>` delete | another person | refuse | not their mail |
| `users/<me>/profile/<doc>` read | me | allow | read only |
| `users/<me>/profile/<doc>` write | me | refuse | a phone must not set its own plan |
| `users/<me>/anything-else` | me | refuse | default closed |

The profile, the storage counter and the username index are in the Realtime Database, not
Firestore, and the app never writes them: the backend does, with the service account, which
the rules do not apply to. So nothing in the app needs write access to them.
