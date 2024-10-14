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
        }, filesDir: filesDir)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}
