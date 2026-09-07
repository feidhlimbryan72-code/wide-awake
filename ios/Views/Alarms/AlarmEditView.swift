import SwiftUI

/// Modal sheet for creating and editing alarms and configuring the disarm challenge.
public struct AlarmEditView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject var paywallManager = PaywallManager.shared

    @State public var alarm: Alarm
    public let onSave: (Alarm) -> Void

    @State private var selectedDate: Date

    public init(alarm: Alarm? = nil, onSave: @escaping (Alarm) -> Void) {
        let initialAlarm = alarm ?? Alarm()
        _alarm = State(initialValue: initialAlarm)
        self.onSave = onSave

        var components = DateComponents()
        components.hour = initialAlarm.hour
        components.minute = initialAlarm.minute
        _selectedDate = State(initialValue: Calendar.current.date(from: components) ?? Date())
    }

    public var body: some View {
        NavigationView {
            ZStack {
                Color.black.ignoresSafeArea()

                Form {
                    // Time Wheel Section
                    Section {
                        DatePicker("", selection: $selectedDate, displayedComponents: .hourAndMinute)
                            .datePickerStyle(.wheel)
                            .labelsHidden()
                            .frame(maxWidth: .infinity)
                            .colorScheme(.dark)
                    }
                    .listRowBackground(Color(white: 0.1))

                    // Basic Details
                    Section(header: Text("GENERAL").foregroundColor(.gray)) {
                        HStack {
                            Text("Label")
                            Spacer()
                            TextField("Alarm Name", text: $alarm.label)
                                .multilineTextAlignment(.trailing)
                                .foregroundColor(.orange)
                        }

                        // Repeat Days
                        NavigationLink(destination: RepeatDaysPickerView(selectedDays: $alarm.repeatDays)) {
                            HStack {
                                Text("Repeat")
                                Spacer()
                                Text(alarm.repeatSummary)
                                    .foregroundColor(.gray)
                            }
                        }

                        // Sound Selector
                        HStack {
                            Text("Sound")
                            Spacer()
                            Menu {
                                ForEach(AlarmSound.allSounds) { sound in
                                    Button(action: {
                                        if sound.isPro && !paywallManager.evaluateProRequirement(reason: "This alarm tone requires Pro.", required: true) {
                                            return
                                        }
                                        alarm.sound = sound
                                    }) {
                                        HStack {
                                            Text(sound.name)
                                            if sound.isPro {
                                                Image(systemName: "crown.fill")
                                            }
                                        }
                                    }
                                }
                            } label: {
                                HStack(spacing: 4) {
                                    Text(alarm.sound.name)
                                        .foregroundColor(.white)
                                    if alarm.sound.isPro {
                                        Image(systemName: "crown.fill")
                                            .foregroundColor(.yellow)
                                    }
                                }
                            }
                        }
                    }
                    .listRowBackground(Color(white: 0.1))

                    // CHOOSE DISARM CHALLENGE SECTION
                    Section(header: Text("CHOOSE DISARM CHALLENGE").foregroundColor(.orange)) {
                        Text("Select which challenge must be completed to silence the alarm:")
                            .font(.caption)
                            .foregroundColor(.gray)

                        ForEach(ChallengeType.allCases) { type in
                            Button(action: {
                                if type.isProRequired && !paywallManager.evaluateProRequirement(reason: "Unlocking \(type.rawValue) requires Pro.", required: true) {
                                    return
                                }
                                alarm.chosenChallenge.type = type
                                alarm.challengeChain = [alarm.chosenChallenge]
                            }) {
                                HStack(spacing: 12) {
                                    Image(systemName: type.iconName)
                                        .foregroundColor(alarm.chosenChallenge.type == type ? .orange : .gray)
                                        .frame(width: 28)

                                    VStack(alignment: .leading, spacing: 2) {
                                        HStack(spacing: 6) {
                                            Text(type.rawValue)
                                                .font(.system(size: 15, weight: .bold))
                                                .foregroundColor(.white)

                                            if type.isProRequired {
                                                Image(systemName: "crown.fill")
                                                    .font(.caption)
                                                    .foregroundColor(.yellow)
                                            }
                                        }

                                        Text(type.descriptionText)
                                            .font(.caption2)
                                            .foregroundColor(.gray)
                                            .lineLimit(2)
                                    }

                                    Spacer()

                                    if alarm.chosenChallenge.type == type {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundColor(.orange)
                                            .font(.system(size: 20))
                                    } else {
                                        Image(systemName: "circle")
                                            .foregroundColor(.gray.opacity(0.4))
                                            .font(.system(size: 20))
                                    }
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    .listRowBackground(Color(white: 0.1))

                    // CUSTOMIZE CHOSEN CHALLENGE PARAMETERS
                    Section(header: Text("CUSTOMIZE \(alarm.chosenChallenge.type.rawValue.uppercased()) SETTINGS").foregroundColor(.gray)) {
                        switch alarm.chosenChallenge.type {
                        case .math:
                            Picker("Difficulty", selection: $alarm.chosenChallenge.mathDifficulty) {
                                ForEach(MathDifficulty.allCases) { diff in
                                    Text(diff.rawValue).tag(diff)
                                }
                            }
                            .foregroundColor(.white)

                            Stepper("Problem Count: \(alarm.chosenChallenge.mathProblemCount)", value: $alarm.chosenChallenge.mathProblemCount, in: 1...10)
                                .foregroundColor(.white)

                        case .shake:
                            Stepper("Required Shakes: \(alarm.chosenChallenge.targetShakeCount)", value: $alarm.chosenChallenge.targetShakeCount, in: 20...500, step: 20)
                                .foregroundColor(.white)

                        case .barcode:
                            HStack {
                                Text("Anchor Label")
                                Spacer()
                                TextField("e.g. Bathroom Sink", text: Binding(
                                    get: { alarm.chosenChallenge.targetBarcodeLabel ?? "Bathroom Sink" },
                                    set: { alarm.chosenChallenge.targetBarcodeLabel = $0 }
                                ))
                                .multilineTextAlignment(.trailing)
                                .foregroundColor(.orange)
                            }

                            HStack {
                                Text("Target Barcode")
                                Spacer()
                                TextField("Barcode / QR Code", text: Binding(
                                    get: { alarm.chosenChallenge.targetBarcodePayload ?? "DEV_BARCODE" },
                                    set: { alarm.chosenChallenge.targetBarcodePayload = $0 }
                                ))
                                .multilineTextAlignment(.trailing)
                                .foregroundColor(.gray)
                            }

                        case .watchSteps:
                            Stepper("Step Milestone: \(alarm.chosenChallenge.targetStepCount) steps", value: $alarm.chosenChallenge.targetStepCount, in: 25...500, step: 25)
                                .foregroundColor(.white)

                        case .watchHeartRate:
                            Stepper("Target BPM: \(Int(alarm.chosenChallenge.targetBPM)) BPM", value: Binding(
                                get: { Int(alarm.chosenChallenge.targetBPM) },
                                set: { alarm.chosenChallenge.targetBPM = Double($0) }
                            ), in: 80...140, step: 5)
                                .foregroundColor(.white)

                        default:
                            Text("Standard challenge configuration active.")
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                    }
                    .listRowBackground(Color(white: 0.1))

                    // WAKE UP CHOICE OPTION
                    Section(header: Text("WAKE-UP DISARM OPTIONS").foregroundColor(.gray)) {
                        Toggle(isOn: $alarm.allowUserChoiceOnWake) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Allow Choosing Challenge On Wake-Up")
                                    .foregroundColor(.white)
                                Text("Shows a quick selector on the alarm screen so you can choose which challenge to solve.")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                        }
                        .tint(.orange)

                        Toggle(isOn: $alarm.isSnoozePenaltyEnabled) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("90-Second Snooze Penalty")
                                    .foregroundColor(.white)
                                Text("Ramps volume to 100% and triggers intense haptics if not solved quickly.")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                        }
                        .tint(.orange)
                    }
                    .listRowBackground(Color(white: 0.1))
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle("Configure Alarm")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(.gray)
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        let calendar = Calendar.current
                        alarm.hour = calendar.component(.hour, from: selectedDate)
                        alarm.minute = calendar.component(.minute, from: selectedDate)
                        alarm.challengeChain = [alarm.chosenChallenge]
                        onSave(alarm)
                        dismiss()
                    }
                    .foregroundColor(.orange)
                    .fontWeight(.bold)
                }
            }
        }
    }
}

private struct RepeatDaysPickerView: View {
    @Binding var selectedDays: Set<DayOfWeek>

    var body: some View {
        List {
            ForEach(DayOfWeek.allCases, id: \.self) { day in
                Button(action: {
                    if selectedDays.contains(day) {
                        selectedDays.remove(day)
                    } else {
                        selectedDays.insert(day)
                    }
                }) {
                    HStack {
                        Text(day.shortName)
                            .foregroundColor(.white)
                        Spacer()
                        if selectedDays.contains(day) {
                            Image(systemName: "checkmark")
                                .foregroundColor(.orange)
                        }
                    }
                }
                .listRowBackground(Color(white: 0.1))
            }
        }
        .scrollContentBackground(.hidden)
        .background(Color.black.ignoresSafeArea())
        .navigationTitle("Repeat")
    }
}
