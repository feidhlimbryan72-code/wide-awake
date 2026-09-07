import Foundation
import AVFoundation
import UIKit

/// High-urgency audio playback engine engineered to wake heavy sleepers.
/// Enforces background audio session configuration, ducking overrides, volume escalation,
/// and haptic feedback shockwaves.
@MainActor
public final class AudioPlayerService: NSObject, ObservableObject, AVAudioPlayerDelegate {
    public static let shared = AudioPlayerService()

    @Published public private(set) var isPlaying: Bool = false
    @Published public private(set) var currentVolume: Float = 0.85
    @Published public private(set) var isPenaltyActive: Bool = false
    @Published public private(set) var penaltySecondsRemaining: Int = 90

    private var audioPlayer: AVAudioPlayer?
    private var penaltyTimer: Timer?
    private var hapticTimer: Timer?
    private var hapticGenerator: UIImpactFeedbackGenerator?

    private var initialVolume: Float = 0.85
    private var targetAlarm: Alarm?

    override private init() {
        super.init()
    }

    /// Configures the hardware audio session for high-priority emergency playback.
    private func setupAudioSession() {
        do {
            let session = AVAudioSession.sharedInstance()
            // Set playback category with duckOthers to override mute switch and suppress other media
            try session.setCategory(.playback, mode: .default, options: [.duckOthers])
            try session.setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
            print("[AudioPlayerService] Error configuring AVAudioSession: \(error.localizedDescription)")
        }
    }

    /// Starts looping alarm audio for the provided alarm configuration.
    public func startAlarm(for alarm: Alarm) {
        stopAlarm()
        setupAudioSession()

        self.targetAlarm = alarm
        self.initialVolume = alarm.volume
        self.currentVolume = alarm.volume
        self.isPenaltyActive = false
        self.penaltySecondsRemaining = 90

        // Attempt to load resource file; fallback to synthesized alert tone
        if let resourceName = alarm.sound.resourceName,
           let soundURL = Bundle.main.url(forResource: resourceName, withExtension: "mp3") ??
                          Bundle.main.url(forResource: resourceName, withExtension: "wav") {
            do {
                audioPlayer = try AVAudioPlayer(contentsOf: soundURL)
            } catch {
                print("[AudioPlayerService] Failed to load sound asset \(resourceName), fallback to synthetic.")
                audioPlayer = try? generateSyntheticSiren()
            }
        } else {
            audioPlayer = try? generateSyntheticSiren()
        }

        guard let player = audioPlayer else {
            print("[AudioPlayerService] Critical Error: Unable to initialize audio player.")
            return
        }

        player.delegate = self
        player.numberOfLoops = -1 // Infinite loop until challenge completion
        player.volume = alarm.volume
        player.prepareToPlay()
        player.play()

        isPlaying = true

        if alarm.isSnoozePenaltyEnabled {
            startPenaltyTimer()
        }
    }

    /// Initiates the 90-second countdown leading to the aggressive snooze penalty escalation.
    private func startPenaltyTimer() {
        penaltyTimer?.invalidate()
        penaltySecondsRemaining = 90

        penaltyTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            Task { @MainActor [weak self] in
                guard let self = self, self.isPlaying else { return }

                if self.penaltySecondsRemaining > 0 {
                    self.penaltySecondsRemaining -= 1

                    // Linear ramp volume from initial to 1.0 over 90 seconds
                    let progress = Float(90 - self.penaltySecondsRemaining) / 90.0
                    let rampedVolume = self.initialVolume + ((1.0 - self.initialVolume) * progress)
                    self.currentVolume = min(1.0, rampedVolume)
                    self.audioPlayer?.volume = self.currentVolume
                } else if !self.isPenaltyActive {
                    // Penalty threshold reached: maximum volume and aggressive haptics
                    self.isPenaltyActive = true
                    self.currentVolume = 1.0
                    self.audioPlayer?.volume = 1.0
                    self.startAggressiveHaptics()
                }
            }
        }
    }

    /// Triggers continuous haptic shockwaves when the 90-second penalty activates.
    private func startAggressiveHaptics() {
        hapticGenerator = UIImpactFeedbackGenerator(style: .heavy)
        hapticGenerator?.prepare()

        hapticTimer?.invalidate()
        hapticTimer = Timer.scheduledTimer(withTimeInterval: 0.6, repeats: true) { [weak self] _ in
            Task { @MainActor [weak self] in
                guard let self = self, self.isPlaying, self.isPenaltyActive else { return }
                self.hapticGenerator?.impactOccurred(intensity: 1.0)
            }
        }
    }

    /// Silences audio playback and clears penalties upon verified challenge disarm.
    public func stopAlarm() {
        penaltyTimer?.invalidate()
        penaltyTimer = nil
        hapticTimer?.invalidate()
        hapticTimer = nil
        hapticGenerator = nil

        audioPlayer?.stop()
        audioPlayer = nil

        isPlaying = false
        isPenaltyActive = false
        targetAlarm = nil

        do {
            try AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
        } catch {
            print("[AudioPlayerService] Error deactivating audio session: \(error.localizedDescription)")
        }
    }

    /// Synthesizes an emergency piercing alert tone (2400Hz - 3200Hz dual frequency).
    private func generateSyntheticSiren() throws -> AVAudioPlayer {
        let sampleRate: Double = 44100.0
        let duration: Double = 1.5
        let numSamples = Int(sampleRate * duration)

        let settings: [String: Any] = [
            AVFormatIDKey: kAudioFormatLinearPCM,
            AVSampleRateKey: sampleRate,
            AVNumberOfChannelsKey: 1,
            AVLinearPCMBitDepthKey: 16,
            AVLinearPCMIsBigEndianKey: false,
            AVLinearPCMIsFloatKey: false
        ]

        var pcmData = Data()
        for i in 0..<numSamples {
            let time = Double(i) / sampleRate
            let freq: Double = (sin(2.0 * .pi * 4.0 * time) > 0) ? 2400.0 : 3200.0
            let value = sin(2.0 * .pi * freq * time)
            var sample = Int16(value * 32767.0 * 0.9)
            withUnsafeBytes(of: &sample) { pcmData.append(contentsOf: $0) }
        }

        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent("wakeorpay_synthetic.wav")
        let audioFile = try AVAudioFile(forWriting: tempURL, settings: settings)
        guard let format = AVAudioFormat(settings: settings),
              let pcmBuffer = AVAudioPCMBuffer(pcmFormat: format, frameCapacity: AVAudioFrameCount(numSamples)) else {
            throw NSError(domain: "AudioPlayerService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to allocate audio buffer"])
        }

        pcmBuffer.frameLength = AVAudioFrameCount(numSamples)
        let channelData = pcmBuffer.int16ChannelData![0]
        pcmData.withUnsafeBytes { rawBuffer in
            let int16Pointer = rawBuffer.bindMemory(to: Int16.self)
            for i in 0..<numSamples {
                channelData[i] = int16Pointer[i]
            }
        }

        try audioFile.write(from: pcmBuffer)
        return try AVAudioPlayer(contentsOf: tempURL)
    }

    // MARK: - AVAudioPlayerDelegate
    public nonisolated func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        // Ignored: loops indefinitely
    }
}
