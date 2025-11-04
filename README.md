# Memory Map

Android 用のシンプルな位置メモアプリです。地図上で場所を選択してメモと日時を保存し、同時にそのメモ内容をを登録できます。

## 概要

- 地図検索（住所／施設名）
- 地図タップで場所選択 → メモ入力 → 日時選択 → アプリ保存
- 選択時にカレンダーへ自動でメモの内容を登録
- 保存した地点はマーカーとして地図に表示されます

## 必要な環境

- Android Studio（推奨）
- JDK 11 以上
- Android SDK（プロジェクトが要求する API レベル）
- 実機またはエミュレータ



## ビルドと実行

1. このリポジトリをクローンまたはダウンロード

```bash
# Windows (cmd)
git clone https://github.com/ri462/Memory-Map.git
cd "Memory Map"
```

2. Android Studio でプロジェクトを開き、Gradle 同期を実行してから実行（Run）してください。

3. コマンドライン（Gradle wrapper）でビルドする場合:

```bash
# Windows
gradlew.bat assembleDebug
# Unix / macOS
./gradlew assembleDebug
```

## 使い方

1. 検索ボックスに場所の名前や住所を入力して検索。該当場所が見つかれば地図が移動します。
2. 地図をタップして場所を選択。
3. 画面右下の「保存」ボタンを押すとメモ入力ダイアログが出ます。
4. メモを入力して「日時選択へ」を押すと日付と時間のピッカーが開きます。
5. 日時を選ぶとアプリ内に地点が保存され、同時にカレンダーにメモの内容が自動登録されます。
   
## 保存形式

- アプリ内では SharedPreferences の StringSet に JSON（Gson）で保存します。
- カレンダーには `CalendarContract.Events` を使ってイベントを挿入します。


