// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "WideAwake",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "WideAwake",
            targets: ["WideAwake"]
        ),
    ],
    dependencies: [
        // Superwall SDK for Swift
        .package(url: "https://github.com/superwall/Superwall-iOS", from: "3.0.0")
    ],
    targets: [
        .target(
            name: "WideAwake",
            dependencies: [
                .product(name: "SuperwallKit", package: "Superwall-iOS")
            ],
            path: "."
        ),
    ]
)
