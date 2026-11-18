# branding/

One place for the Eyemail mark. Both surfaces read from here, so they cannot drift apart.

| File | Used by |
|---|---|
| `app-icons/eyemail-icon.svg` | the drawing, for a designer to edit |
| `app-icons/eyemail-512.png` | web manifest, Play Store |
| `app-icons/eyemail-192.png` | web manifest, Android home screen |
| `app-icons/eyemail-180.png` | iOS home screen (apple-touch-icon) |
| `app-icons/eyemail-96.png`, `-48.png`, `-32.png` | favicon sizes |
| `app-icons/eyemail-1024-square.png` | Play Store, square, no rounding |
| `make-icons.py` | regenerates all of the above, and the Android launcher icons |

The web pages and `manifest.webmanifest` point at `branding/app-icons/...` directly, so there
are no copies of the icon anywhere else in the repository.

The Android launcher icons are the one exception: the build system only reads `res/mipmap-*`,
so `make-icons.py` writes those from the same drawing. They are generated, not edited by hand.

    python3 branding/make-icons.py

Run it from the repository root after any change to the drawing.
