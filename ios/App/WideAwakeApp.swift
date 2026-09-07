import SwiftUI

@main
struct WideAwakeApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var scheduler = AlarmSchedulerService.shared
    @StateObject private var paywallManager = PaywallManager.shared

    var body: some Scene {
        WindowGroup {
            AlarmListView()
                .preferredColorScheme(.dark)
                .fullScreenCover(item: $scheduler.triggeredAlarm) { alarm in
                    ActiveAlarmView(alarm: alarm) {
                        scheduler.triggeredAlarm = nil
                    }
                }
                .sheet(isPresented: $paywallManager.shouldPresentPaywall) {
                    PaywallView()
                }
                .onAppear {
                    // Configure Superwall SDK
                    PaywallManager.shared.configure(apiKey: "pk_live_mock_wakeorpay_superwall")
                }
        }
    }
}
