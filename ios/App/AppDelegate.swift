import UIKit
import UserNotifications

/// Handles notification responses and system lifecycle events.
public final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    public func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self

        Task { @MainActor in
            _ = await AlarmSchedulerService.shared.requestAuthorization()
        }

        return true
    }

    // MARK: - UNUserNotificationCenterDelegate
    public func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // App is already in foreground: trigger active alarm screen directly
        let userInfo = notification.request.content.userInfo
        if let alarmIdString = userInfo["alarm_id"] as? String,
           let alarmId = UUID(uuidString: alarmIdString) {
            Task { @MainActor in
                if let alarm = AlarmSchedulerService.shared.alarms.first(where: { $0.id == alarmId }) {
                    AlarmSchedulerService.shared.triggeredAlarm = alarm
                }
            }
        }
        completionHandler([.banner, .sound, .badge])
    }

    public func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        if let alarmIdString = userInfo["alarm_id"] as? String,
           let alarmId = UUID(uuidString: alarmIdString) {
            Task { @MainActor in
                if let alarm = AlarmSchedulerService.shared.alarms.first(where: { $0.id == alarmId }) {
                    AlarmSchedulerService.shared.triggeredAlarm = alarm
                }
            }
        }
        completionHandler()
    }
}
