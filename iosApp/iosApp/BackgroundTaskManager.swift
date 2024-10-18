//
//  BackgroundTasks.swift
//  iosApp
//
//  Created by Christopher Huntwork on 10/16/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import BackgroundTasks
import NotificationCenter
import ComposeApp

private let backgroundTaskIdentifier = "IOS_SOURCE_BACKGROUND_SYNC"

class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()

    private init() { }
}

extension BackgroundTaskManager {
    func register() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: backgroundTaskIdentifier, using: .main, launchHandler: handleTask(_:))
    }
    
    func handleTask(_ task: BGTask) {
        scheduleAppRefresh()

        print("Running background task \(task.identifier)")

        let filesDir = if #available(iOS 16.0, *) {
            FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path(percentEncoded: true)
        } else {
            FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path
        }
        MainViewControllerKt.runBackgroundSync(sendNotification: {title, body in
            let content = UNMutableNotificationContent()
            content.title = title
            content.body = body
            let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: nil)
            UNUserNotificationCenter.current().add(request) { error in
                if let error = error {
                    print("notification error: \(error.localizedDescription)")
                }
            }
        }, filesDir: filesDir)
        
        
        task.setTaskCompleted(success: true)
    }
    
    func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: backgroundTaskIdentifier)
        request.

        var message = "Scheduled"
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch BGTaskScheduler.Error.notPermitted {
            message = "BGTaskScheduler.shared.submit notPermitted"
        } catch BGTaskScheduler.Error.tooManyPendingTaskRequests {
            message = "BGTaskScheduler.shared.submit tooManyPendingTaskRequests"
        } catch BGTaskScheduler.Error.unavailable {
            message = "BGTaskScheduler.shared.submit unavailable"
        } catch {
            message = "BGTaskScheduler.shared.submit \(error.localizedDescription)"
        }

        print(message)
    }
}

