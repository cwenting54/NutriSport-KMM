import SwiftUI
import GoogleSignIn
import Firebase
import FirebaseCore
import FirebaseMessaging
import FirebaseAuth
import shared
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
                .onOpenURL { url in
                    print("Received URL in onOpenURL: \(url)")
                    
                    if GIDSignIn.sharedInstance.handle(url) { return }
                    
                    guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
                          let queryItems = components.queryItems else { return }
                    
                    let success = queryItems.first(where: { $0.name == "success" })?.value == "true"
                    let cancel = queryItems.first(where: { $0.name == "cancel" })?.value == "true"
                    let token = queryItems.first(where: { $0.name == "token" })?.value
                    
                    print(
                        """
                            ✅ Success: \(success)
                            ✅ Cancel: \(cancel)
                            ✅ Token: \(token ?? "null")
                        """
                    )
                    
                    PreferencesRepository().savePayPalData(
                        isSuccess: success ? KotlinBoolean(value: true) : nil,
                        error: cancel ? "Payment canceled." : nil,
                        token: token
                    )
//                    IntentHandlerHelper().navigateToPaymentCompleted(
//                       isSuccess: success ? KotlinBoolean(true) : nil,
//                       error: cancel ? "Payment canceled." : nil,
//                       token: token
//                     )
                }
        }
    }
}


class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        
        // MARK: - 安全設定 Firebase
        // 1. 取得 GoogleService-Info.plist 的路徑
        if let filePath = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
           let options = FirebaseOptions(contentsOfFile: filePath) {
            
            // 2. 從 Info.plist 讀取真正的 API Key
            if let secureKey = Bundle.main.object(forInfoDictionaryKey: "GoogleApiKey") as? String {
                options.apiKey = secureKey
                print("Firebase 使用安全 Key 設定成功")
            } else {
                print("警告：在 Info.plist 找不到 GoogleApiKey，將使用 GoogleService-Info.plist 內的原始值")
            }
            
            // 4. 使用修改後的 options 啟動
            FirebaseApp.configure(options: options)
            
        } else {
      
            print("錯誤：找不到 GoogleService-Info.plist")
            FirebaseApp.configure()
        }
        
        // MARK: - 原有的 NotifierManager 初始化
        NotifierManager.shared.initialize(configuration: NotificationPlatformConfigurationIos(
            showPushNotification: true,
            askNotificationPermissionOnStart: true,
            notificationSoundName: nil
        ))
        
        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}
