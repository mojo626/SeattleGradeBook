import SwiftUI
import ComposeApp
import Combine
import AVKit

@main
struct iOSApp: App {
    static var avPlayer: AVPlayer? = nil
    @StateObject var viewModel: ViewModelWrapper
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(viewModel)
        }
    }
    
    var notificationDelegate = MyNotificationDelegate()

    init() {
        //percent encoding migrations
        if #available(iOS 16.0, *) {
            try? FileManager.default.createDirectory(atPath: filesDir(), withIntermediateDirectories: true)
            moveFile(sourcePath: FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path(percentEncoded: true)+"/seattle_gradebook.preferences_pb", destinationPath: filesDir()+"/seattle_gradebook.preferences_pb")
            moveFile(sourcePath: FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path(percentEncoded: true)+"/pfp.jpeg", destinationPath: filesDir()+"/pfp.jpeg")
        }
        _viewModel = StateObject(wrappedValue: ViewModelWrapper())
        
        UNUserNotificationCenter.current().delegate = notificationDelegate
        BackgroundTaskManager.shared.register()
        BackgroundTaskManager.shared.scheduleAppRefresh()
        #if DEBUG
            MainViewControllerKt.debugBuild()
        #endif
    }
}

func filesDir() -> String {
    return FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path
    let filesDir = if #available(iOS 16.0, *) {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path(percentEncoded: true)
    } else {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path
    }
    return filesDir
}

func moveFile(sourcePath: String, destinationPath: String) {
    let fileManager = FileManager.default

    guard fileManager.fileExists(atPath: sourcePath) else {
        return
    }
    
    print("item exists, moving \(sourcePath) to \(destinationPath)")
    do {
        try? fileManager.removeItem(atPath: destinationPath)
        try fileManager.moveItem(atPath: sourcePath, toPath: destinationPath)
    } catch {
        print("\(error)")
    }
}

func openLink(url: String) {
    guard let url = URL(string: url) else { return }
    UIApplication.shared.open(url)
}

@MainActor
class ViewModelWrapper: ObservableObject {
    let viewModel = IosAppViewModel(sendNotification: sendNotification(title:body:), filesDir: filesDir())
    @Published var initialized = false
    @Published var displayNameBinding = ""
    @Published var loggedIn: Bool? = nil
    @Published var currentClassBinding: Class? = nil
    @Published var implementPluey = false
    @Published var platformNavigation = true
    @Published var gpaTypeSelectionBinding = 0 {
        didSet {
            self.viewModel.gpaTypeSelectionState.setValue(gpaTypeSelectionBinding)
        }
    }
    private var cancellables = Set<AnyCancellable>()
    init() {
        Task {
            try? await viewModel.displayNameState
                .collect(collector:  FlowCollector<String> { displayName in
                    await MainActor.run {
                        self.displayNameBinding = displayName
                    }
                })
        }
        Task {
            try? await viewModel.classForGradePage
                .collect(collector:  FlowCollector<Class?> { currentClass in
                    await MainActor.run {
                        self.currentClassBinding = currentClass
                    }
                })
        }
        Task {
            try? await viewModel.initializedFlows.collect(collector: FlowCollector<KotlinBoolean> { initializedFlows in
                await MainActor.run {
                    self.initialized = initializedFlows.boolValue
                }
                if (initializedFlows.boolValue) {
                    Task {
                        try? await self.viewModel._username
                            .collect(collector:  FlowCollector<String?> { username in
                                await MainActor.run {
                                    if self.loggedIn == nil {
                                        if username == nil {
                                            self.loggedIn = false
                                        } else {
                                            self.loggedIn = true
                                        }
                                    }
                                }
                            })
                    }
                    Task {
                        try? await self.viewModel._implementPluey
                            .collect(collector:  FlowCollector<KotlinBoolean> { implementPluey in
                                await MainActor.run {
                                    self.implementPluey = implementPluey.boolValue
                                }
                            })
                    }
                    Task {
                        try? await self.viewModel._platformNavigation
                            .collect(collector:  FlowCollector<KotlinBoolean> { platformNavigation in
                                await MainActor.run {
                                    self.platformNavigation = platformNavigation.boolValue
                                }
                            })
                    }
                }
            })
        }
    }
}

class FlowCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    var collector: (T) async -> ()
    init(_ collector: @escaping (T) async -> ()) {
        self.collector = collector
    }
    
    func emit(value: Any?) async throws {
        if let valueTyped = value as? T {
            await self.collector(valueTyped)
        }
    }
}
