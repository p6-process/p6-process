# p6-process

P6 process engine.

[![License](https://img.shields.io/github/license/p6-process/p6-process?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/p6-process/p6-process/master/master?logo=github&style=for-the-badge)](https://github.com/p6-process/p6-process/actions?query=workflow%3Amaster)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/p6-process/p6-process?sort=semver&logo=github&style=for-the-badge)](https://github.com/p6-process/p6-process/releases/latest)


### Build and tests

```shell script
mvn clean package
samo maven docker-build
mvn failsafe:integration-test failsafe:verify
```
#### Generate DDL

```shell script
mvn process-classes -Pddl
```