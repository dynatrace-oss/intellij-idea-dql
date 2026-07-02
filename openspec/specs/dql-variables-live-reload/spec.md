# Spec: dql-variables-live-reload

## Purpose

Defines how changes to `dql-variables.json` files are reflected in already-open DQL queries without requiring the queries to be reopened, covering both invalidation of stale definitions and resolution of newly appearing ones.

## Requirements

### Requirement: Resolved variable definitions invalidate when the variables file changes

A DQL `$variable`'s resolved definition SHALL be recomputed whenever a `dql-variables.json` that governs the
query's file is created, modified, moved, or deleted. The cached definition held by a variable element SHALL NOT
outlive a change to the file it was resolved from, nor to a file that would newly become its closest definition.

#### Scenario: Editing a value updates the query

- **WHEN** a variable's value is changed in `dql-variables.json` while a DQL query using that variable is open
- **THEN** the query's resolved value and inferred data type reflect the new value without reopening the query

#### Scenario: Deleting a definition unresolves the query

- **WHEN** a variable's definition is removed from `dql-variables.json`
- **THEN** the DQL variable that resolved to it becomes unresolved and the corresponding inspection reports it as
  undefined

### Requirement: Newly created definitions resolve in already-open queries

A DQL `$variable` that currently resolves to no definition SHALL resolve to a definition that later appears in a
governing `dql-variables.json` — whether the file is newly created or the variable is newly added to an existing
file — without the query being reopened.

#### Scenario: Creating the variables file resolves previously unresolved variables

- **WHEN** a `dql-variables.json` is created in the query's directory defining a variable the open query uses
- **THEN** that variable resolves to the new definition and the "undefined variable" inspection clears

#### Scenario: Adding a missing definition resolves the variable

- **WHEN** a new property for a previously unresolved variable is added to an existing `dql-variables.json`
- **THEN** the query resolves the variable to the added definition

### Requirement: Reload applies across all affected queries

When a `dql-variables.json` changes, every DQL query file in that file's governed directory scope that references
an affected variable SHALL reflect the change, not only the currently focused editor.

#### Scenario: Multiple queries reflect a single edit

- **WHEN** two DQL files in the same directory subtree use a variable and its definition is edited once
- **THEN** re-analysis of both files resolves the variable to the updated definition
