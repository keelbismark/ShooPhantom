# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-02-21

### Added
- 🧪 Comprehensive unit tests for WardManager, FuelManager, and DegradationManager
- 🔄 GitHub Actions CI/CD pipeline with automated testing and building
- 📊 JaCoCo code coverage reporting
- 🎨 Checkstyle configuration for code quality enforcement
- 📝 CONTRIBUTING.md with contribution guidelines
- 📦 Maven plugins: jacoco-maven-plugin, maven-checkstyle-plugin, maven-surefire-plugin
- 🔐 Updated README with developer documentation section
- 📚 Comprehensive project documentation

### Changed
- 📈 Version bumped from 1.0 to 2.0
- 🏗️ Improved code structure for better testability
- 📖 Enhanced documentation with badges and detailed sections

### Fixed
- 🐛 Code quality issues identified by Checkstyle

### Technical Debt
- Additional unit tests needed for EffectManager, listeners, and tasks
- Integration tests for ward creation flow
- Performance tests for large-scale deployments

## [1.0.0] - Initial Release

### Added
- 🛡️ Three-tier protection system against phantoms
- ⚗️ Fuel system using phantom membranes
- 🔧 Structure degradation mechanics (copper oxidation, mast burnout)
- 🔥 Tier 3 gradient power scaling based on active masts
- ✨ Visual effects and sounds for all tiers
- 🗄️ Database support (SQLite and MySQL)
- 📦 Automatic fuel injection via hoppers
- 🎮 Complete command system with admin tools
- 📜 Detailed configuration options
- 📖 Comprehensive README documentation

### Features
- Tier 1 (Node): 48 block radius, 60 min/fuel, spawn prevention
- Tier 2 (Station): 80 block radius, 45 min/fuel, target cancellation + copper degradation
- Tier 3 (Tesla Tower): 80-128 block radius, 30 min/fuel, damage + knockback + fire + mast degradation
- Shoo Sigil activation item with multiple acquisition methods
- Permission system for all features
- Cross-world database persistence
