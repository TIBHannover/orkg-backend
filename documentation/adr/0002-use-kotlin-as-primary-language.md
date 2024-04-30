# 2. Use Kotlin as primary language

Date: 2022-02-01

## Status

Accepted

## Context

This ADR records the current practice, that was decided years ago already.
Deciding on the programming language is one of the most impactful and long-lasting decisions for a project and architecture, so it should be recorded.

## Decision

Kotlin will be used as primary language for this application.

Other languages can be used for specific tasks (such as scripts), or if using Kotlin is not possible or very inconvenient.
Those cases should be exceptions, not the rule.

## Consequences

We benefit of Kotlins advantages over Java, without sacrificing anything due to the great interoperability.

In the future, we may profit from other developments in the Kotlin ecosystem, such as code sharing between platforms.
