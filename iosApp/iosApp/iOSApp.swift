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
        MainViewControllerKt.debugBuild()
    }
}
