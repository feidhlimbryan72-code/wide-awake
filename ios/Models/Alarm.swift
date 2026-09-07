import Foundation

/// Represents the days of the week an alarm can repeat on.
public enum DayOfWeek: Int, CaseIterable, Codable, Sendable, Comparable {
    case sunday = 1
    case monday = 2
    case tuesday = 3
    case wednesday = 4
    case thursday = 5
    case friday = 6
    case saturday = 7

    public static func < (lhs: DayOfWeek, rhs: DayOfWeek) -> Bool {
        lhs.rawValue < rhs.rawValue
    }

    public var shortName: String {
        switch self {
        case .sunday: return "Sun"
        case .monday: return "Mon"
        case .tuesday: return "Tue"
        case .wednesday: return "Wed"
        case .thursday: return "Thu"
        case .friday: return "Fri"
        case .saturday: return "Sat"
        }
    }
}

public struct AlarmSound: Identifiable, Codable, Sendable, Hashable {
    public var id: String
    public var name: String
    public var resourceName: String?
    public var isPro: Bool

    public init(id: String, name: String, resourceName: String? = nil, isPro: Bool = false) {
        self.id = id
        self.name = name
        self.resourceName = resourceName
        self.isPro = isPro
    }

    public static let sirenAirRaid = AlarmSound(id: "siren_air_raid", name: "Air Raid Siren", resourceName: "siren_air_raid", isPro: false)
    public static let nuclearKlaxon = AlarmSound(id: "nuclear_klaxon", name: "Nuclear Reactor Klaxon", resourceName: "nuclear_klaxon", isPro: false)
    public static let militaryReveille = AlarmSound(id: "military_reveille", name: "Aggressive Reveille", resourceName: "military_reveille", isPro: true)
    public static let syntheticShock = AlarmSound(id: "synthetic_shock", name: "Synthetic Piercing Tone", resourceName: nil, isPro: true)

    public static let allSounds: [AlarmSound] = [
        .sirenAirRaid,
        .nuclearKlaxon,
        .militaryReveille,
        .syntheticShock
    ]
}

public struct Alarm: Identifiable, Codable, Sendable, Hashable {
    public var id: UUID
    public var hour: Int
    public var minute: Int
    public var label: String
    public var isEnabled: Bool
    public var repeatDays: Set<DayOfWeek>
    public var sound: AlarmSound
    public var volume: Float
    public var snoozeDurationMinutes: Int
    public var isSnoozePenaltyEnabled: Bool
    public var chosenChallenge: ChallengeConfig
    public var allowUserChoiceOnWake: Bool
    public var challengeChain: [ChallengeConfig]

    public init(
        id: UUID = UUID(),
        hour: Int = 7,
        minute: Int = 0,
        label: String = "Wake Up",
        isEnabled: Bool = true,
        repeatDays: Set<DayOfWeek> = [],
        sound: AlarmSound = .sirenAirRaid,
        volume: Float = 0.85,
        snoozeDurationMinutes: Int = 5,
        isSnoozePenaltyEnabled: Bool = true,
        chosenChallenge: ChallengeConfig = ChallengeConfig(type: .math),
        allowUserChoiceOnWake: Bool = true,
        challengeChain: [ChallengeConfig]? = nil
    ) {
        self.id = id
        self.hour = hour
        self.minute = minute
        self.label = label
        self.isEnabled = isEnabled
        self.repeatDays = repeatDays
        self.sound = sound
        self.volume = volume
        self.snoozeDurationMinutes = snoozeDurationMinutes
        self.isSnoozePenaltyEnabled = isSnoozePenaltyEnabled
        self.chosenChallenge = chosenChallenge
        self.allowUserChoiceOnWake = allowUserChoiceOnWake
        self.challengeChain = challengeChain ?? [chosenChallenge]
    }

    /// Formatted digital time string, e.g. "07:00 AM".
    public var timeFormatted: String {
        var components = DateComponents()
        components.hour = hour
        components.minute = minute
        let date = Calendar.current.date(from: components) ?? Date()
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }

    /// Summary description of repetition schedule.
    public var repeatSummary: String {
        if repeatDays.isEmpty {
            return "Never"
        }
        if repeatDays.count == 7 {
            return "Every day"
        }
        if repeatDays == [.monday, .tuesday, .wednesday, .thursday, .friday] {
            return "Weekdays"
        }
        if repeatDays == [.saturday, .sunday] {
            return "Weekends"
        }
        return repeatDays.sorted().map { $0.shortName }.joined(separator: ", ")
    }

    /// True if any configured asset or challenge triggers a Pro entitlement check.
    public var requiresPro: Bool {
        sound.isPro || chosenChallenge.type.isProRequired || challengeChain.contains { $0.type.isProRequired }
    }

    /// Calculates the next exact firing date based on current calendar time and repeat days.
    public func nextTriggerDate(after date: Date = Date()) -> Date {
        let calendar = Calendar.current
        var targetComponents = calendar.dateComponents([.year, .month, .day], from: date)
        targetComponents.hour = hour
        targetComponents.minute = minute
        targetComponents.second = 0

        guard let candidateToday = calendar.date(from: targetComponents) else {
            return date.addingTimeInterval(3600)
        }

        // If one-off alarm (no repeat days)
        if repeatDays.isEmpty {
            if candidateToday > date {
                return candidateToday
            } else {
                return calendar.date(byAdding: .day, value: 1, to: candidateToday) ?? candidateToday
            }
        }

        // Recurring alarm
        var upcomingDates: [Date] = []
        for day in repeatDays {
            var matchingComponents = DateComponents()
            matchingComponents.weekday = day.rawValue
            matchingComponents.hour = hour
            matchingComponents.minute = minute
            matchingComponents.second = 0

            if let nextDate = calendar.nextDate(after: date, matching: matchingComponents, matchingPolicy: .nextTime) {
                upcomingDates.append(nextDate)
            }
        }

        return upcomingDates.min() ?? candidateToday
    }
}
