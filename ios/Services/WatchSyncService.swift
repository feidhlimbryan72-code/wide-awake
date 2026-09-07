import Foundation
import WatchConnectivity
import HealthKit
import Combine

/// Integrates HealthKit and Apple Watch Connectivity to verify physical waking thresholds.
@MainActor
public final class WatchSyncService: NSObject, ObservableObject, WCSessionDelegate {
    public static let shared = WatchSyncService()

    @Published public private(set) var liveHeartRate: Double = 0.0
    @Published public private(set) var liveStepCount: Int = 0
    @Published public private(set) var isWatchReachable: Bool = false
    @Published public private(set) var isHealthAuthorized: Bool = false

    private let healthStore = HKHealthStore()
    private var heartRateQuery: HKAnchoredObjectQuery?
    private var stepCountQuery: HKObserverQuery?

    override private init() {
        super.init()
        setupWatchConnectivity()
    }

    private func setupWatchConnectivity() {
        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }
    }

    /// Requests HealthKit read permissions for steps and heart rate.
    public func requestHealthAuthorization() async -> Bool {
        guard HKHealthStore.isHealthDataAvailable() else { return false }

        guard let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount),
              let heartRateType = HKQuantityType.quantityType(forIdentifier: .heartRate) else {
            return false
        }

        let readTypes: Set<HKObjectType> = [stepType, heartRateType]

        do {
            try await healthStore.requestAuthorization(toShare: [], read: readTypes)
            self.isHealthAuthorized = true
            return true
        } catch {
            print("[WatchSyncService] HealthKit authorization failed: \(error.localizedDescription)")
            self.isHealthAuthorized = false
            return false
        }
    }

    /// Starts real-time biometric query updates during active alarm challenge.
    public func startBiometricMonitoring() {
        guard HKHealthStore.isHealthDataAvailable() else { return }

        // Start live heart rate streaming query
        guard let heartRateType = HKQuantityType.quantityType(forIdentifier: .heartRate) else { return }
        let predicate = HKQuery.predicateForSamples(withStart: Date().addingTimeInterval(-180), end: nil, options: .strictStartDate)

        heartRateQuery = HKAnchoredObjectQuery(
            type: heartRateType,
            predicate: predicate,
            anchor: nil,
            limit: HKObjectQueryNoLimit
        ) { [weak self] _, samples, _, _, _ in
            self?.processHeartRateSamples(samples)
        }

        heartRateQuery?.updateHandler = { [weak self] _, samples, _, _, _ in
            self?.processHeartRateSamples(samples)
        }

        if let query = heartRateQuery {
            healthStore.execute(query)
        }
    }

    public func stopBiometricMonitoring() {
        if let query = heartRateQuery {
            healthStore.stop(query)
            heartRateQuery = nil
        }
        liveHeartRate = 0.0
        liveStepCount = 0
    }

    private func processHeartRateSamples(_ samples: [HKSample]?) {
        guard let quantitySamples = samples as? [HKQuantitySample], let last = quantitySamples.last else { return }
        let bpm = last.quantity.doubleValue(for: HKUnit.count().unitDivided(by: .minute()))
        Task { @MainActor [weak self] in
            self?.liveHeartRate = bpm
        }
    }

    // MARK: - WCSessionDelegate
    public nonisolated func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {
        Task { @MainActor in
            self.isWatchReachable = session.isReachable
        }
    }

    public nonisolated func sessionDidBecomeInactive(_ session: WCSession) {}
    public nonisolated func sessionDidDeactivate(_ session: WCSession) {
        session.activate()
    }

    public nonisolated func session(_ session: WCSession, didReceiveMessage message: [String : Any]) {
        Task { @MainActor in
            if let steps = message["steps"] as? Int {
                self.liveStepCount = steps
            }
            if let bpm = message["bpm"] as? Double {
                self.liveHeartRate = bpm
            }
        }
    }
}
