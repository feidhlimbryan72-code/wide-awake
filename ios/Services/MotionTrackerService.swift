import Foundation
import CoreMotion
import Combine

/// Tracks high-variance physical shaking movements using CoreMotion.
/// Filters out casual vibration or micro-movements to ensure genuine physical alertness.
@MainActor
public final class MotionTrackerService: ObservableObject {
    @Published public private(set) var currentShakeCount: Int = 0
    @Published public private(set) var targetShakeCount: Int = 100
    @Published public private(set) var progress: Double = 0.0
    @Published public private(set) var isCompleted: Bool = false

    private let motionManager = CMMotionManager()
    private var lastAccelerationMagnitude: Double = 1.0
    private var lastDirectionPositive: Bool = true
    private let accelerationThreshold: Double = 2.2 // Minimum g-force magnitude to qualify as vigorous shake

    public init() {}

    /// Starts tracking shakes toward the specified target count.
    public func startTracking(targetCount: Int) {
        stopTracking()

        self.targetShakeCount = max(10, targetCount)
        self.currentShakeCount = 0
        self.progress = 0.0
        self.isCompleted = false

        guard motionManager.isAccelerometerAvailable else {
            print("[MotionTrackerService] Accelerometer unavailable on this device.")
            return
        }

        motionManager.accelerometerUpdateInterval = 0.05 // 20 Hz sampling
        motionManager.startAccelerometerUpdates(to: .main) { [weak self] data, error in
            guard let self = self, let acceleration = data?.acceleration else { return }

            let magnitude = sqrt(pow(acceleration.x, 2) + pow(acceleration.y, 2) + pow(acceleration.z, 2))

            // Check if magnitude exceeds threshold and reverses directional vector
            if magnitude > self.accelerationThreshold {
                let currentDirectionPositive = acceleration.y > 0
                if currentDirectionPositive != self.lastDirectionPositive {
                    self.currentShakeCount += 1
                    self.lastDirectionPositive = currentDirectionPositive
                    self.progress = min(1.0, Double(self.currentShakeCount) / Double(self.targetShakeCount))

                    if self.currentShakeCount >= self.targetShakeCount {
                        self.isCompleted = true
                        self.stopTracking()
                    }
                }
            }
            self.lastAccelerationMagnitude = magnitude
        }
    }

    /// Stops accelerometer updates and releases sensor resources.
    public func stopTracking() {
        if motionManager.isAccelerometerActive {
            motionManager.stopAccelerometerUpdates()
        }
    }
}
