import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
    var notificationDelegate = MyNotificationDelegate()

    init() {
        UNUserNotificationCenter.current().delegate = notificationDelegate
        BackgroundTaskManager.shared.register()
        BackgroundTaskManager.shared.scheduleAppRefresh()
        #if DEBUG
            MainViewControllerKt.debugBuild()
        #endif
    }
}

func filesDir() -> String {
    let filesDir = if #available(iOS 16.0, *) {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path(percentEncoded: true)
    } else {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path
    }
    return filesDir
}


func getSourceData(username: String, password: String, quarter: String, loadPfp: KotlinBoolean) -> String {
    let cchar = get_source_data(username, password, filesDir().removingPercentEncoding!.replacingOccurrences(of: " ", with: " "), quarter, loadPfp.boolValue)
    return String(cString: cchar!)
}

func openLink(url: String) {
    guard let url = URL(string: url) else { return }
    UIApplication.shared.open(url)
}
