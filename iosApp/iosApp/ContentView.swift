import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let filesDir = if #available(iOS 16.0, *) {
            FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path(percentEncoded: true)
        } else {
            FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path
        }
        return MainViewControllerKt.MainViewController(getSourceData: {username,password in
            let cchar = get_source_data(username, password, filesDir.removingPercentEncoding!.replacingOccurrences(of: " ", with: "\\ "))
            return String(cString: cchar!)
        }, filesDir: filesDir, sendNotification: {title, body in
            let content = UNMutableNotificationContent()
            content.title = title
            content.body = body
            let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: nil)
            UNUserNotificationCenter.current().add(request) { error in
                if let error = error {
                    print("notification error: \(error.localizedDescription)")
                }
            }
        })
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}

class MyNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.list, .banner, .sound])
    }
}
