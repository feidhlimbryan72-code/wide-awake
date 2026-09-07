import Foundation
import HealthKit
import WatchConnectivity

/// Manages HKWorkoutSession on Apple Watch to monitor steps and live heart rate during alarms.
public final class WatchWorkoutManager: NSObject, ObservableObject, HKWorkoutSessionDelegate, HKLiveWorkoutBuilderDelegate, WCSessionDelegate {
    public static let shared = WatchWorkoutManager()

    @Published public private(set) var currentHeartRate: Double = 0.0
    @Published public private(set) var currentSteps: Int = 0
    @Published public private(set) var isRunning: Bool = false

    private let healthStore = HKHealthStore()
    private var session: HKWorkoutSession?
    private var builder: HKLiveWorkoutBuilder?

    override private init() {
        super.init()
        if WCSession.isSupported() {
            WCSession.default.delegate = self
            WCSession.default.activate()
        }
    }

    public func startWakeWorkout() {
        let typesToShare: Set = [HKQuantityType.workoutType()]
        let typesToRead: Set = [
            HKQuantityType.quantityType(forIdentifier: .heartRate)!,
            HKQuantityType.quantityType(forIdentifier: .stepCount)!
        ]

        healthStore.requestAuthorization(toShare: typesToShare, read: typesToRead) { [weak self] success, _ in
            guard success else { return }
            self?.beginSession()
        }
    }

    private func beginSession() {
        let configuration = HKWorkoutConfiguration()
        configuration.activityType = .other
        configuration.locationType = .indoor

        do {
            session = try HKWorkoutSession(healthStore: healthStore, configuration: configuration)
            builder = session?.associatedWorkoutBuilder()
            builder?.dataSource = HKLiveWorkoutDataSource(healthStore: healthStore, workoutConfiguration: configuration)

            session?.delegate = self
            builder?.delegate = self

            session?.startActivity(with: Date())
            builder?.beginCollection(withStart: Date()) { success, _ in
                DispatchQueue.main.async {
                    self.isRunning = success
                }
            }
        } catch {
            print("Failed to start watch workout: \(error)")
        }
    }

    public func stopWakeWorkout() {
        session?.end()
        builder?.endCollection(withEnd: Date()) { _, _ in
            self.builder?.finishWorkout { _, _ in
                DispatchQueue.main.async {
                    self.isRunning = false
                }
            }
        }
    }

    // MARK: - HKLiveWorkoutBuilderDelegate
    public func workoutBuilder(_ workoutBuilder: HKLiveWorkoutBuilder, didCollectDataOf collectedTypes: Set<HKSampleType>) {
        for type in collectedTypes {
            guard let quantityType = type as? HKQuantityType else { continue }
            let statistics = workoutBuilder.statistics(for: quantityType)

            if quantityType == HKQuantityType.quantityType(forIdentifier: .heartRate) {
                let heartRateUnit = HKUnit.count().unitDivided(by: .minute())
                let value = statistics?.mostRecentQuantity()?.doubleValue(for: heartRateUnit) ?? 0.0
                DispatchQueue.main.async {
                    self.currentHeartRate = value
                    self.streamToPhone(bpm: value, steps: self.currentSteps)
                }
            } else if quantityType == HKQuantityType.quantityType(forIdentifier: .stepCount) {
                let stepUnit = HKUnit.count()
                let value = statistics?.sumQuantity()?.doubleValue(for: stepUnit) ?? 0.0
                DispatchQueue.main.async {
                    self.currentSteps = Int(value)
                    self.streamToPhone(bpm: self.currentHeartRate, steps: Int(value))
                }
            }
        }
    }

    public func workoutBuilderDidCollectEvent(_ workoutBuilder: HKLiveWorkoutBuilder) {}

    // MARK: - HKWorkoutSessionDelegate
    public func workoutSession(_ workoutSession: HKWorkoutSession, didChangeTo toState: HKWorkoutSessionState, from fromState: HKWorkoutSessionState, date: Date) {}
    public func workoutSession(_ workoutSession: HKWorkoutSession, didFailWithError error: Error) {}

    // MARK: - WCSessionDelegate
    public func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {}

    private func streamToPhone(bpm: Double, steps: Int) {
        if WCSession.default.isReachable {
            WCSession.default.sendMessage(["bpm": bpm, "steps": steps], replyHandler: nil)
        }
    }
}
