# NUTRISPORT

基於 **Kotlin Multiplatform (KMM)** 建構的跨平台電商應用程式。
本專案採用 **Feature-First Modularization** (功能優先模組化) 策略，並深度整合 **Firebase Cloud Services**，實現跨平台的即時資料同步與雲端媒體管理。

## Modular Architecture (模組化架構詳解)

本專案將系統拆解為以下關鍵模組，確保每一層級職責單一：

### 1. 核心功能模組 (Feature Layer) - `:feature`
業務邏輯的核心，每個功能皆為獨立單元。
* **`:feature:auth`**: 負責使用者認證。
* **`:feature:home`**: 複合式首頁 UI (含商品總覽、熱銷商品及促銷商品)。
* **`:feature:details`**: 商品詳情與規格選擇。
* **`:feature:cart`**: 購物車管理，透過 Firebase 監聽實現多裝置購物車同步。
* **`:feature:categories`**: 商品分類。
* **`:feature:profile`**: 會員資料。
* **`:feature:order_list`**: 會員訂單紀錄。
* **`:feature:favorite_list`**: 會員收藏商品清單。
* **`:feature:admin_panel`**: 管理員後台 (商品管理)。

### 2. 雲端與數據層 (Cloud & Data Layer)
負責資料的持久化、雲端同步與依賴注入。

* **`:FIREBASE` (Cloud Infrastructure)**:
    * **Realtime Database**: 負責使用者購物車 (Cart) 的即時同步，確保使用者在不同裝置間能看到一致的購物內容。
    * **Cloud Storage**: 處理非結構化媒體資料。包含商品圖片託管與快取策略。
    * **Email Automation**: 整合後端自動化流程，負責監聽訂單事件並觸發 HTML 格式的訂單確認信 (Order Confirmation Email) 發送。
* **`:data`** (Repository Implementation):
    * 實作層。負責將 Firebase 的 Snapshot 轉換為 Domain Model，提供乾淨的資料流供業務層使用。
* **`:di`** (Dependency Injection):
    * 統一管理 Koin Modules。

### 3. 共享與核心層 (Shared & Core Layer)
* **`:shared`**: 包含 model和Design System (`Colors.kt`, `Fonts.kt`)。
* **`:navigation`**: 獨立導航模組，統一管理路由。

### 4. 應用程式入口 (App Layer)
* **`:composeApp`** / **`:iosApp`**: 各平台啟動入口。

---

## Tech Stack (技術堆疊)

* **UI Framework**: Jetpack Compose Multiplatform
* **Architecture**: Clean Architecture + MVVM/MVI
* **Cloud Services (Firebase)**:
    * **Database**: Realtime Database (NoSQL)
    * **Storage**: Cloud Storage
    * **Serverless**: Cloud Functions / Extensions (Email Trigger)
* **DI**: Koin
* **Async**: Coroutines & Flow
* **Network**: Ktor Client

---

1.  **Reactive Data Synchronization (響應式資料同步)**：
    利用 **Kotlin Flow** 結合 **Firebase Realtime Database**，實現「單一裝置修改，全平台即時更新」。

2.  **Automated Order Processing (自動化訂單處理)**：
    當使用者完成結帳 (`Checkout`)：
    * **Automated Email Notification (自動化郵件通知)**：系統監聽訂單成立事件，自動觸發並寄送 HTML 格式的訂單確認信 (Order Confirmation Email) 至使用者信箱，提供完整的訂單明細。

3.  **Cross-Platform Feature Parity (跨平台一致性)**：
    透過 `expect/actual` 機制與 KMM 架構，確保 Android 與 iOS 雙平台擁有完全一致的**支付流程 (PayPal)** 與**業務邏輯**，消除平台差異帶來的維護成本。
