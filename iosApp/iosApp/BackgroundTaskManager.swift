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

private let backgroundTaskIdentifier = "com.chrissytopher.source.Source.background_sync"

class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()

    private init() { }
}

extension BackgroundTaskManager {
    func register() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: backgroundTaskIdentifier, using: .main, launchHandler: handleTask(_:))
        print("Registered Background Task!")
    }
    
    func handleTask(_ task: BGTask) {
        scheduleAppRefresh()

        print("Running background task \(task.identifier)")

        let filesDir = if #available(iOS 16.0, *) {
            FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path(percentEncoded: true)
        } else {
            FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.path
        }
        MainViewControllerKt.runBackgroundSync(sendNotification: sendNotification(title:body:), filesDir: filesDir)
        
        
        task.setTaskCompleted(success: true)
    }
    
    func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: backgroundTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60)

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

