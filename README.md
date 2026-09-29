# Pip Road Buddy

A cute anime driving buddy that keeps you awake on the road. Mika or Ren rides along, asks you easy questions out loud, and listens for your answer. Answer in **English or Tagalog**. If you don't answer, a loud alarm goes off.

<img src="docs/icon.png" width="96" alt="Pip Road Buddy icon">

## What it does

- **Voice check-ins:** at random times, your buddy asks something easy and fun: quick math, "name a fruit", trivia, rhymes, memory games, "what color is the car in front of you?", or light chat.
- **Answer out loud:** in English or Tagalog ("fourteen", "labing-apat" and "katorse" all count). Tap works as a backup.
- **Alarm:** no answer twice in a row sets off a siren and vibration. Say "I'm awake" or tap to stop it.
- **Tiredness meter:** tracks reply speed, wrong answers, "yes, I'm sleepy" answers, time driving and time of day.
- **Breaks:** say "break" or "pahinga" to start a 15-minute break. You also get a reminder every 2 hours.
- **Works offline:** everything is bundled in the app.

## Install on Android

1. Open the **Releases** page of this repo and download `PipRoadBuddy.apk` from **Latest APK**.
2. Open it on your phone and allow "Install unknown apps" if Android asks.
3. On first drive, allow microphone access.

For voice answers with no signal, download Google's offline speech pack for **Filipino** (and English) in the Google app's voice settings.

## Try it in a browser

With GitHub Pages turned on (Settings → Pages → Deploy from branch → `main` / `docs`), open the Pages link in Chrome. Voice answers in the browser need internet.

## Build it yourself

The **Build APK** GitHub Action builds the APK on every push to `main`. To sign builds with your own key so they update over earlier installs, add these repository secrets:

- `PIP_KEYSTORE_B64`: your `pip.keystore` file, base64-encoded
- `PIP_KEYSTORE_PASS`: its password

Local build, with the Android SDK installed and `ANDROID_HOME` set:

```bash
bash build.sh   # output: build/PipRoadBuddy.apk
```

## Project layout

```
docs/          the app itself (HTML/CSS/JS, fonts, icon), also served by GitHub Pages
android/       native shell: WebView, text-to-speech, speech recognition, vibration
build.sh       builds and signs the APK
tools/align.py 4-byte aligns the APK
.github/       CI workflow that builds the APK
```

## Safety

Pip helps you notice drowsiness. It can't replace sleep. If your eyes feel heavy, pull over somewhere safe and rest.
