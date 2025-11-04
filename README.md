# Memory Map

Android 用のシンプルな位置メモアプリです。地図上で場所を選択してメモと日時を保存し、同時に Google カレンダーへイベントを登録できます。

## 概要

- 地図検索（住所／施設名）
- 地図タップで場所選択 → メモ入力 → 日時選択 → アプリ保存
- 選択時に Google カレンダーへ自動でイベント登録
- 保存した地点はマーカーとして地図に表示されます

## 必要な環境

- Android Studio（推奨）
- JDK 11 以上
- Android SDK（プロジェクトが要求する API レベル）
- 実機またはエミュレータ

## 事前準備（API キー等）

- Google Maps を利用しているため、Google Maps Android API の API キーが必要です。
  - `AndroidManifest.xml` や `google_maps_api.xml`（プロジェクト内の既定ファイル）に API キーを設定してください。

## 権限

アプリは以下の権限を使用します。実行時に許可を求められます。

- ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION: 地図・位置情報（必須ではないが利便性向上）
- READ_CALENDAR / WRITE_CALENDAR: カレンダーの読み書き（Google カレンダーへイベントを追加するため）

注意: カレンダーのアカウントが複数ある場合、Google のアカウント（ACCOUNT_TYPE = `com.google`）のカレンダーIDを優先して使用します。

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

## 使い方（簡単）

1. 検索ボックスに場所の名前や住所を入力して検索（`search`）。該当場所が見つかれば地図が移動します。
2. 地図をタップして場所を選択。
3. 画面右下の「保存」ボタンを押すとメモ入力ダイアログが出ます。
4. メモを入力して「日時選択へ」を押すと日付と時間のピッカーが開きます。
5. 日時を選ぶとアプリ内に地点が保存され、同時に Google カレンダーにイベントが自動登録されます（1時間のイベント）。

## 保存形式

- アプリ内では SharedPreferences の StringSet に JSON（Gson）で保存します。
- カレンダーには `CalendarContract.Events` を使ってイベントを挿入します。

## よくある問題と対処法

- Geocoder で住所が取得できない／検索結果が空
  - ネットワーク接続を確認してください。Geocoder は端末のサービスに依存します。
  - 入力を具体的に（市区町村＋番地など）してみてください。

- Google Maps が表示されない
  - API キーの設定を確認してください（制限やパッケージ名・SHA1 が正しいか）。

- カレンダーに追加できない（権限エラー）
  - アプリにカレンダーの読み書き権限を付与してください。
  - 一部端末やアカウント設定によっては書き込み不可のカレンダーがあるため、アカウント設定を確認してください。

## 貢献

プルリクエスト歓迎。軽微な変更（README 修正、UI 微改善、バグ修正等）は直接 PR を出してください。

作業フロー例:

```bash
# 新しいブランチを切る
git checkout -b feature/your-change
# 変更 -> commit -> push
git add .
git commit -m "説明"
git push origin feature/your-change
# GitHub 上で PR を作成
```

## ライセンス

必要に応じてライセンスを追加してください（例: MIT）。

---

不明点や README に追記してほしい具体項目があれば教えてください。README の文言は日本語で読みやすく整えていますが、追加のスクリーンショットや使い方の図を入れることもできます。
