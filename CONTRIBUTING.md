# Contributing to GraphQl4Sparql

Thank you for your interest in contributing to GraphQl4Sparql! This document provides guidelines and instructions for contributing.

## How Can I Contribute?

### Reporting Bugs

Before creating a bug report:
- Check if the issue has already been reported in the [issue tracker](https://github.com/Scaseco/graphql4sparql/issues)
- Verify you're using the latest version
- Include as much detail as possible

**What to include:**
- A clear, descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Screenshots if applicable
- Your environment (OS, Java version, GraphQl4Sparql version)
- Relevant logs or error messages

### Suggesting Features

Feature requests are welcome! Please:
- Search existing issues to avoid duplicates
- Provide a clear description of the proposed feature
- Explain why this feature would be useful
- Include examples of how it would be used

### Pull Requests

1. Fork the repository
2. Create a branch for your changes (`git checkout -b feature/your-feature`)
3. Make your changes
4. Run tests if applicable
5. Commit with a clear message following our conventions
6. Push your branch and open a pull request

**Pull Request Guidelines:**
- Follow the existing code style
- Include tests for new functionality
- Update documentation as needed
- Keep commits focused and atomic
- Reference any related issues

## Development Setup

### Prerequisites
- Java 21 or later
- Maven 3.8+
- Git

### Building from Source

```bash
# Clone the repository
git clone https://github.com/Scaseco/graphql4sparql.git
cd graphql4sparql-parent

# Build the project
mvn clean install

# Run tests
mvn test
```

### Code Style

- Follow existing code style.
- Avoid consecutive blank lines.
- Use 4 spaces for indentation in Java code.
- There is no restriction on the max line length, but consider breaking excessively long ones.
- Add Javadoc for public APIs
- Use meaningful variable and method names
- When in doubt, use [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) as a baseline.

## Questions?

- Open an issue for discussion
- Check existing documentation

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).

