import SwiftUI

/// Main dashboard displaying configured alarms and quick status indicators.
public struct AlarmListView: View {
    @ObservedObject var scheduler = AlarmSchedulerService.shared
    @ObservedObject var paywallManager = PaywallManager.shared

    @State private var editingAlarm: Alarm?
    @State private var showingCreateSheet: Bool = false
    @State private var testingAlarm: Alarm?

    public init() {}

    public var body: some View {
        NavigationView {
            ZStack {
                Color.black.ignoresSafeArea()

                VStack(spacing: 0) {
                    // Pro Status / Upgrade Pill
                    if !paywallManager.subscriptionState.isPro {
                        Button(action: {
                            paywallManager.activePaywallReason = "Unlock unlimited alarms and advanced physical disarms."
                            paywallManager.shouldPresentPaywall = true
                        }) {
                            HStack {
                                Image(systemName: "crown.fill")
                                    .foregroundColor(.yellow)
                                Text("UPGRADE TO PRO — 3 DAYS FREE")
                                    .font(.system(size: 12, weight: .black, design: .monospaced))
                                    .foregroundColor(.white)
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .foregroundColor(.gray)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                            .background(Color(white: 0.12))
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.yellow.opacity(0.4), lineWidth: 1)
                            )
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 12)
                    }

                    // Alarms List
                    if scheduler.alarms.isEmpty {
                        VStack(spacing: 16) {
                            Spacer()
                            Image(systemName: "alarm.fill")
                                .font(.system(size: 56))
                                .foregroundColor(.gray.opacity(0.4))
                            Text("No Alarms Configured")
                                .font(.headline)
                                .foregroundColor(.gray)
                            Text("Tap the + button above to create your first accountability alarm.")
                                .font(.subheadline)
                                .foregroundColor(.gray.opacity(0.8))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 32)
                            Spacer()
                        }
                    } else {
                        List {
                            ForEach(scheduler.alarms) { alarm in
                                AlarmRowView(
                                    alarm: alarm,
                                    onToggle: { isEnabled in
                                        handleToggle(alarm: alarm, isEnabled: isEnabled)
                                    },
                                    onTestRing: {
                                        testingAlarm = alarm
                                    }
                                )
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    editingAlarm = alarm
                                }
                                .listRowBackground(Color(white: 0.08))
                                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                            }
                            .onDelete { indices in
                                for index in indices {
                                    let id = scheduler.alarms[index].id
                                    scheduler.deleteAlarm(id: id)
                                }
                            }
                        }
                        .listStyle(.plain)
                        .scrollContentBackground(.hidden)
                    }
                }
            }
            .navigationTitle("Wide Awake")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        let activeCount = scheduler.alarms.filter { $0.isEnabled }.count
                        if paywallManager.canActivateAlarm(currentActiveCount: activeCount) {
                            showingCreateSheet = true
                        }
                    }) {
                        Image(systemName: "plus")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.orange)
                    }
                }
            }
            .sheet(isPresented: $showingCreateSheet) {
                AlarmEditView { newAlarm in
                    scheduler.saveAlarm(newAlarm)
                }
            }
            .sheet(item: $editingAlarm) { alarm in
                AlarmEditView(alarm: alarm) { updatedAlarm in
                    scheduler.saveAlarm(updatedAlarm)
                }
            }
            .fullScreenCover(item: $testingAlarm) { alarm in
                ActiveAlarmView(alarm: alarm) {
                    testingAlarm = nil
                }
            }
            .sheet(isPresented: $paywallManager.shouldPresentPaywall) {
                PaywallView()
            }
        }
    }

    private func handleToggle(alarm: Alarm, isEnabled: Bool) {
        if isEnabled {
            let activeCount = scheduler.alarms.filter { $0.isEnabled }.count
            if !paywallManager.canActivateAlarm(currentActiveCount: activeCount) {
                return
            }
        }
        scheduler.toggleAlarm(id: alarm.id, isEnabled: isEnabled)
    }
}

private struct AlarmRowView: View {
    let alarm: Alarm
    let onToggle: (Bool) -> Void
    let onTestRing: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .firstTextBaseline, spacing: 6) {
                    Text(alarm.timeFormatted)
                        .font(.system(size: 38, weight: .medium, design: .rounded))
                        .foregroundColor(alarm.isEnabled ? .white : .gray)

                    if alarm.requiresPro {
                        Image(systemName: "crown.fill")
                            .font(.system(size: 12))
                            .foregroundColor(.yellow)
                    }
                }

                HStack(spacing: 8) {
                    Text(alarm.label)
                        .font(.subheadline)
                        .foregroundColor(alarm.isEnabled ? .orange : .gray)

                    Text("•")
                        .foregroundColor(.gray)

                    Text(alarm.repeatSummary)
                        .font(.caption)
                        .foregroundColor(.gray)
                }

                // Challenge badge
                HStack(spacing: 6) {
                    HStack(spacing: 4) {
                        Image(systemName: alarm.chosenChallenge.type.iconName)
                        Text(alarm.chosenChallenge.type.rawValue)
                    }
                    .font(.system(size: 11, weight: .bold))
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Color.orange.opacity(0.15))
                    .foregroundColor(.orange)
                    .cornerRadius(6)

                    if alarm.allowUserChoiceOnWake {
                        Text("CHOICE ON WAKE")
                            .font(.system(size: 9, weight: .black, design: .monospaced))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(white: 0.16))
                            .foregroundColor(.gray)
                            .cornerRadius(4)
                    }
                }
                .padding(.top, 4)
            }

            Spacer()

            VStack(spacing: 12) {
                Toggle("", isOn: Binding(
                    get: { alarm.isEnabled },
                    set: { onToggle($0) }
                ))
                .labelsHidden()
                .tint(.orange)

                // Test ring trigger button
                Button(action: onTestRing) {
                    Text("TEST")
                        .font(.system(size: 10, weight: .black, design: .monospaced))
                        .foregroundColor(.yellow)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.yellow.opacity(0.15))
                        .cornerRadius(6)
                }
            }
        }
        .padding(.vertical, 8)
    }
}
