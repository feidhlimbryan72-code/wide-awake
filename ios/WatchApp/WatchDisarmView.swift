import SwiftUI

public struct WatchDisarmView: View {
    @ObservedObject var workoutManager = WatchWorkoutManager.shared

    public init() {}

    public var body: some View {
        VStack(spacing: 8) {
            HStack {
                Image(systemName: "alarm.fill")
                    .foregroundColor(.orange)
                Text("WIDE AWAKE")
                    .font(.system(size: 11, weight: .black, design: .monospaced))
                    .foregroundColor(.orange)
            }

            VStack(spacing: 4) {
                HStack {
                    Image(systemName: "figure.walk")
                        .foregroundColor(.yellow)
                    Text("\(workoutManager.currentSteps) steps")
                        .font(.system(size: 16, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                }

                HStack {
                    Image(systemName: "heart.fill")
                        .foregroundColor(.red)
                    Text("\(Int(workoutManager.currentHeartRate)) BPM")
                        .font(.system(size: 16, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                }
            }
            .padding(.vertical, 4)

            Button(action: {
                if workoutManager.isRunning {
                    workoutManager.stopWakeWorkout()
                } else {
                    workoutManager.startWakeWorkout()
                }
            }) {
                Text(workoutManager.isRunning ? "TRACKING ACTIVE" : "START DISARM TRACK")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 6)
                    .background(workoutManager.isRunning ? Color.green : Color.orange)
                    .cornerRadius(8)
            }
        }
        .padding()
    }
}
