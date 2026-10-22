# KMP Multi-Module Guidelines (AGP 9+)

## Project Structure
- `:shared` - Cross-platform UI and business logic in `shared/src/commonMain/kotlin/`.
- `:androidApp` - Android entry point.
- `:desktopApp` - Desktop entry point.
- `:server` - Server entry point.

