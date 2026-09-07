import SwiftUI
import AVFoundation

/// Camera-based disarm view forcing user to walk to a registered barcode/QR anchor (e.g. bathroom).
public struct BarcodeScannerView: View {
    public let config: ChallengeConfig
    public let onCompleted: () -> Void

    @State private var scannedCode: String?
    @State private var isCameraAuthorized: Bool = false
    @State private var cameraSession: AVCaptureSession?
    @State private var errorMessage: String?

    public init(config: ChallengeConfig, onCompleted: @escaping () -> Void) {
        self.config = config
        self.onCompleted = onCompleted
    }

    public var body: some View {
        VStack(spacing: 24) {
            Text("SCAN LOCATION ANCHOR")
                .font(.system(size: 16, weight: .bold, design: .monospaced))
                .foregroundColor(.green)
                .tracking(2)

            Text(config.targetBarcodeLabel ?? "Registered Anchor Barcode")
                .font(.system(size: 20, weight: .semibold, design: .rounded))
                .foregroundColor(.white)

            // Viewfinder Mock / Capture Box
            ZStack {
                RoundedRectangle(cornerRadius: 18)
                    .fill(Color(white: 0.08))
                    .frame(width: 280, height: 280)
                    .overlay(
                        RoundedRectangle(cornerRadius: 18)
                            .stroke(Color.green.opacity(0.6), lineWidth: 3)
                    )

                // Laser scanline animation
                VStack {
                    Rectangle()
                        .fill(Color.green)
                        .frame(height: 2)
                        .shadow(color: .green, radius: 8)
                    Spacer()
                }
                .frame(width: 260, height: 260)

                Image(systemName: "viewfinder")
                    .font(.system(size: 80, weight: .ultraLight))
                    .foregroundColor(.green.opacity(0.4))

                if let scanned = scannedCode {
                    VStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 48))
                            .foregroundColor(.green)
                        Text("VERIFIED: \(scanned)")
                            .font(.system(size: 14, weight: .bold, design: .monospaced))
                            .foregroundColor(.white)
                    }
                }
            }

            Text("Go to your registered anchor (bathroom/kitchen) and point camera at the barcode.")
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)

            // Simulator / Development manual disarm button
            #if targetEnvironment(simulator) || DEBUG
            Button(action: {
                scannedCode = config.targetBarcodePayload ?? "DEV_MOCK_BARCODE"
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                    onCompleted()
                }
            }) {
                Text("Simulate Valid Barcode Scan")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.black)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 10)
                    .background(Color.green)
                    .cornerRadius(10)
            }
            #endif
        }
        .onAppear {
            checkCameraPermission()
        }
    }

    private func checkCameraPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            isCameraAuthorized = true
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                DispatchQueue.main.async {
                    self.isCameraAuthorized = granted
                }
            }
        default:
            isCameraAuthorized = false
            errorMessage = "Camera access denied. Enable in Settings."
        }
    }
}
