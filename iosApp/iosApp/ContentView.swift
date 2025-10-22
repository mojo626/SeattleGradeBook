import UIKit
import SwiftUI
import ComposeApp
import AVKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let filesDir = filesDir()
        return MainViewControllerKt.MainViewController(filesDir: filesDir, sendNotification: sendNotification(title:body:), openLink: openLink, implementPluey: implementPluey(reverse:))
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct NavigationView: UIViewControllerRepresentable {
    @EnvironmentObject var viewModel: ViewModelWrapper
    var navigationPage: NavScreen
    var navigateTo: (NavScreen) -> ()
    var navigationStackCleared: (NavScreen) -> ()
    var navigateBack: () -> ()
    let paddingState: any Kotlinx_coroutines_coreMutableStateFlow
    func makeUIViewController(context: Context) -> UIViewController {
        let filesDir = filesDir()
        return MainViewControllerKt.NavigationViewController(viewModel: viewModel.viewModel, navigationPage: navigationPage, filesDir: filesDir, openLink: openLink, implementPluey: implementPluey(reverse:), navigateTo: navigateTo, navigateBack: navigateBack, navigationStackCleared: navigationStackCleared, paddingState: paddingState)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@available(iOS 16.0, *)
struct ComposePaddedView: View {
    @EnvironmentObject var viewModel: ViewModelWrapper
    var route: NavScreen
    var navigationPath: Binding<NavigationPath>
    var navigationStackCleared: (NavScreen) -> ()
    let paddingState = MainViewControllerKt.doNewPaddingState()
    
    var body: some View {
        ZStack {
            GeometryReader { geometry in
                Color.clear
                    .task(id: geometry.size) {
                        let insets = geometry.safeAreaInsets

                        paddingState.setValue(MainViewControllerKt.paddingValues(top: Float(insets.top), bottom: Float(insets.bottom), start: Float(insets.leading), end: Float(insets.trailing)))
                    }
                NavigationView(navigationPage: route, navigateTo: { newRoute in navigationPath.wrappedValue.append(newRoute) }, navigationStackCleared: navigationStackCleared, navigateBack: { navigationPath.wrappedValue.removeLast() }, paddingState: paddingState)
                    .ignoresSafeArea(.all)
            }
        }
    }
}

@available(iOS 17.0, *)
struct NavigationHolderView: View {
    @EnvironmentObject var viewModel: ViewModelWrapper
    var initialScreen: NavScreen
    @State var navigationPath = NavigationPath()
    private var gpaTypeSelection: Binding<Int> {
        Binding(
            get: { viewModel.gpaTypeSelectionBinding },
            set: { viewModel.gpaTypeSelectionBinding = $0 }
        )
    }
    var body: some View {
        NavigationStack(path: $navigationPath) {
            let inner = ComposePaddedView(route: initialScreen, navigationPath: $navigationPath, navigationStackCleared: { route in
                if route == NavScreen.onboarding {
                    viewModel.loggedIn = false
                }
            })
                .navigationDestination(for: NavScreen.self, destination: { route in
                    ComposePaddedView(route: route, navigationPath: $navigationPath, navigationStackCleared: { route in
                        if route == NavScreen.onboarding {
                            viewModel.loggedIn = false
                        }
                    })
                        .onDisappear {
                            if (route == NavScreen.grades && viewModel.currentClassBinding?.reported_grade == "P") {
                                implementPluey(reverse: KotlinBoolean(bool: true))
                            }
                        }
                        .toolbar {
                            ToolbarItem(placement: .title) {
                                let title = if route == NavScreen.grades {
                                    viewModel.currentClassBinding?.name ?? ""
                                } else {
                                    route.title()
                                }
                                let text = Text(title)
                                if #available(iOS 26.0, *) {
                                    text
                                        .padding(.horizontal)
                                        .padding(.vertical, 4.0)
                                        .glassEffect()
                                } else {
                                    text
                                }
                            }
                            if route == NavScreen.gpa {
                                ToolbarItem(placement: .topBarTrailing) {
                                    Picker(selection: gpaTypeSelection) {
                                        Text("Weighted").tag(0)
                                        Text("Unweighted").tag(1)
                                    } label: {}
                                    .pickerStyle(.segmented)
                                }
                            }
                            
                        }
                })
            if initialScreen == NavScreen.home {
                inner
                    .toolbar {
                        ToolbarItem(placement: .title) {
                            let text = Text(viewModel.displayNameBinding)
                            if #available(iOS 26.0, *) {
                                text
                                    .padding(.horizontal)
                                    .padding(.vertical, 4.0)
                                    .glassEffect()
                            } else {
                                text
                            }
                        }
                        ToolbarItem(placement: .topBarTrailing) {
                            Button {
                                navigationPath.append(NavScreen.settings)
                            } label: {
                                Image(systemName: "gearshape")
                            }
                        }
                        if let image = UIImage(contentsOfFile: "\(filesDir())/pfp.jpeg") {
                            ToolbarItem(placement: .topBarLeading) {
                                let image = Image(uiImage: image)
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                    .clipShape(Circle())
                                if #available(iOS 26.0, *) {
                                    image
                                } else {
                                    image
                                        .frame(width: 48, height: 48)
                                }
                            }
                        }
                    }
                    .toolbarTitleDisplayMode(.inline)
            } else {
                inner
            }
        }
        .onAppear {
            if #available(iOS 26.0, *) {
                
            } else {
                let appearance = UINavigationBarAppearance()
                appearance.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterial)
                UINavigationBar.appearance().scrollEdgeAppearance = appearance
            }
        }
    }
}

extension NavScreen {
    func title() -> String {
        return switch (self) {
        case .calculator: "Grade Calculator"
        case .more: "More"
        case .settings: "Settings"
        case .colors: "Colors & Themes"
        case .gpa: "GPA"
        default: "This is fake"
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var viewModel: ViewModelWrapper
    @State var initiallyLoggedIn = false
    
    var body: some View {
        if viewModel.initialized && viewModel.loggedIn != nil {
            if #available(iOS 18.0, *) {
                if viewModel.loggedIn == true {
                    TabView {
                        Tab("Grades", systemImage: "graduationcap") {
                            let view = NavigationHolderView(initialScreen: NavScreen.home)
                            if #available(iOS 26.0, *) {
                                view
                            } else {
                                view
                                    .toolbarBackground(.visible, for: .tabBar, .bottomBar, .navigationBar)
                            }
                        }
                        Tab("More", systemImage: "lightbulb") {
                            let view = NavigationHolderView(initialScreen: NavScreen.more)
                            if #available(iOS 26.0, *) {
                                view
                            } else {
                                view
                                    .toolbarBackground(.visible, for: .tabBar, .bottomBar, .navigationBar)
                            }
                        }
                    }
                } else {
                    @State var fakeNavigationPath = NavigationPath()
                    ComposePaddedView(route: NavScreen.onboarding, navigationPath: $fakeNavigationPath, navigationStackCleared: { route in
                        if route == NavScreen.home {
                            viewModel.loggedIn = true
                        }
                    })
                }
            } else {
                ComposeView()
                    .ignoresSafeArea(.all)
            }
        } else {
            
        }
    }
}

class MyNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.list, .banner, .sound])
    }
}

func sendNotification(title: String, body: String) {
    let content = UNMutableNotificationContent()
    content.title = title
    content.body = body
    let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: nil)
    UNUserNotificationCenter.current().add(request) { error in
        if let error = error {
            print("notification error: \(error.localizedDescription)")
        }
    }
}

@MainActor
func implementPluey(reverse: KotlinBoolean) {
    do {
    try AVAudioSession.sharedInstance().setCategory(
        .playback,
        mode: .default,
        options: []
    )
    try AVAudioSession.sharedInstance().setActive(true)
    } catch {
        print("Failed to set up audio session: \(error.localizedDescription)")
    }
    let plueyItem = if (reverse.boolValue) {
        AVPlayerItem(url: Bundle.main.url(forResource: "reverse_pluey", withExtension: "mp3")!)
    } else {
        AVPlayerItem(url: Bundle.main.url(forResource: "pluey", withExtension: "m4a")!)
    }
    iOSApp.avPlayer = AVPlayer(playerItem: plueyItem)
    iOSApp.avPlayer?.play()
}
