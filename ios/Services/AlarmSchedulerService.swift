import Foundation
import UserNotifications
import Combine

/// Manages notification scheduling and persistence of alarms across app launches.
@MainActor
public final class AlarmSchedulerService: NSObject, ObservableObject {
    public static let shared = AlarmSchedulerService()

    @Published public private(set) var alarms: [Alarm] = []
    @Published public var triggeredAlarm: Alarm?
    @Published public var isNotificationPermissionGranted: Bool = false

    private let userDefaultsKey = "com.wideawake.alarms_store"
    private let notificationCenter = UNUserNotificationCenter.current()

    override private init() {
        super.init()
        loadAlarms()
    }

    /// Requests user authorization for notifications, requesting critical alert options if supported.
    public func requestAuthorization() async -> Bool {
        var options: UNAuthorizationOptions = [.alert, .sound, .badge]
        if #available(iOS 12.0, *) {
            options.insert(.criticalAlert)
        }

        do {
            let granted = try await notificationCenter.requestAuthorization(options: options)
            self.isNotificationPermissionGranted = granted
            return granted
        } catch {
            print("[AlarmSchedulerService] Authorization error: \(error.localizedDescription)")
            self.isNotificationPermissionGranted = false
            return false
        }
    }

    /// Adds a new alarm or updates an existing alarm, then schedules it.
    public func saveAlarm(_ alarm: Alarm) {
        if let index = alarms.firstIndex(where: { $0.id == alarm.id }) {
            alarms[index] = alarm
        } else {
            alarms.append(alarm)
        }
        persistAlarms()
        rescheduleAlarm(alarm)
    }

    /// Deletes an alarm and cancels its pending notification requests.
    public func deleteAlarm(id: UUID) {
        alarms.removeAll { $0.id == id }
        persistAlarms()
        cancelNotification(for: id)
    }

    /// Toggles the enabled state of an alarm.
    public func toggleAlarm(id: UUID, isEnabled: Bool) {
        guard let index = alarms.firstIndex(where: { $0.id == id }) else { return }
        alarms[index].isEnabled = isEnabled
        persistAlarms()

        if isEnabled {
            rescheduleAlarm(alarms[index])
        } else {
            cancelNotification(for: id)
        }
    }

    /// Schedules notification requests for the target alarm.
    public func rescheduleAlarm(_ alarm: Alarm) {
        cancelNotification(for: alarm.id)

        guard alarm.isEnabled else { return }

        let content = UNMutableNotificationContent()
        content.title = "WAKE UP: \(alarm.label.uppercased())"
        content.body = "Alarm is ringing! Complete your \(alarm.challengeChain.first?.type.rawValue ?? "Challenge") to silence."
        content.sound = UNNotificationSound.defaultCritical
        content.userInfo = ["alarm_id": alarm.id.uuidString]
        content.categoryIdentifier = "ALARM_CATEGORY"

        let targetDate = alarm.nextTriggerDate()
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day, .hour, .minute, .second], from: targetDate)

        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: !alarm.repeatDays.isEmpty)
        let request = UNNotificationRequest(identifier: alarm.id.uuidString, content: content, trigger: trigger)

        notificationCenter.add(request) { error in
            if let error = error {
                print("[AlarmSchedulerService] Failed to schedule notification: \(error.localizedDescription)")
            } else {
                print("[AlarmSchedulerService] Scheduled alarm \(alarm.id) for \(targetDate)")
            }
        }
    }

    public func cancelNotification(for id: UUID) {
        notificationCenter.removePendingNotificationRequests(withIdentifiers: [id.uuidString])
    }

    private func persistAlarms() {
        do {
            let data = try JSONEncoder().encode(alarms)
            UserDefaults.standard.set(data, forKey: userDefaultsKey)
        } catch {
            print("[AlarmSchedulerService] Failed to encode alarms: \(error.localizedDescription)")
        }
    }

    private func loadAlarms() {
        guard let data = UserDefaults.standard.data(forKey: userDefaultsKey) else {
            // Seed default 7:00 AM sample alarm
            self.alarms = [
                Alarm(
                    hour: 7,
                    minute: 0,
                    label: "Morning Awakening",
                    isEnabled: true,
                    repeatDays: [.monday, .tuesday, .wednesday, .thursday, .friday],
                    sound: .sirenAirRaid,
                    challengeChain: [ChallengeConfig(type: .math)]
                )
            ]
            return
        }

        do {
            self.alarms = try JSONDecoder().decode([Alarm].self, from: data)
        } catch {
            print("[AlarmSchedulerService] Failed to decode alarms: \(error.localizedDescription)")
            self.alarms = []
        }
    }
}
