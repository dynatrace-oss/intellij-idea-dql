### Requirement: Top-level entry command is a sticky line
When a DQL file is open in the editor and the user scrolls past the first pipeline command (the entry command), that command's line SHALL be pinned as a sticky line at the top of the viewport.

#### Scenario: Sticky line shows entry command for fetch query
- **WHEN** the editor contains a DQL query starting with `fetch`
- **THEN** the line containing `fetch` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for timeseries query
- **WHEN** the editor contains a DQL query starting with `timeseries`
- **THEN** the line containing `timeseries` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for data query
- **WHEN** the editor contains a DQL query starting with `data`
- **THEN** the line containing `data` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for describe query
- **WHEN** the editor contains a DQL query starting with `describe`
- **THEN** the line containing `describe` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for fieldsSnapshot query
- **WHEN** the editor contains a DQL query starting with `fieldsSnapshot`
- **THEN** the line containing `fieldsSnapshot` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for load query
- **WHEN** the editor contains a DQL query starting with `load`
- **THEN** the line containing `load` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for smartscapeEdges query
- **WHEN** the editor contains a DQL query starting with `smartscapeEdges`
- **THEN** the line containing `smartscapeEdges` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for smartscapeNodes query
- **WHEN** the editor contains a DQL query starting with `smartscapeNodes`
- **THEN** the line containing `smartscapeNodes` is shown as a sticky line when the viewport is scrolled below it

#### Scenario: Sticky line shows entry command for metrics query
- **WHEN** the editor contains a DQL query starting with `metrics`
- **THEN** the line containing `metrics` is shown as a sticky line when the viewport is scrolled below it

### Requirement: Subquery scope shows its own entry command as a sticky line
When the cursor is inside a subquery block, the editor SHALL display a sticky line for the subquery's own entry command. The outer operator command (`append`, `join`, `lookup`, etc.) is NOT a sticky line.

#### Scenario: append subquery shows inner entry command as sticky line
- **WHEN** the editor contains `| append [ fetch ... ]` and the cursor is inside the `[ ... ]` block
- **THEN** the sticky lines include the inner `fetch` command line but NOT the `append` command line

#### Scenario: join subquery shows inner entry command as sticky line
- **WHEN** the editor contains `| join [ data ... ]` and the cursor is inside the `[ ... ]` block
- **THEN** the sticky lines include the inner `data` command line but NOT the `join` command line

#### Scenario: lookup subquery shows inner entry command as sticky line
- **WHEN** the editor contains `| lookup [ fetch ... ]` and the cursor is inside the `[ ... ]` block
- **THEN** the sticky lines include the inner `fetch` command line but NOT the `lookup` command line

### Requirement: Nested subqueries accumulate one sticky line per scope level
When the cursor is inside a subquery that is itself nested inside another subquery, the editor SHALL display one sticky line per enclosing `DQLQuery` scope, ordered from outermost to innermost entry command.

#### Scenario: Two levels of nesting show one sticky line per scope
- **WHEN** the editor contains a query with an `append` subquery that itself contains a `join` subquery, and the cursor is inside the innermost `join` block
- **THEN** the sticky lines show the top-level entry command, the `append` subquery's entry command, and the `join` subquery's entry command — one per scope level, in that order

### Requirement: Non-entry commands do not generate sticky lines
Pipeline commands that are not the first command of their query (e.g., `filter`, `summarize`, `sort`, `limit`) SHALL NOT produce sticky lines.

#### Scenario: filter command does not appear as a sticky line
- **WHEN** the editor contains `fetch ... | filter foo == "bar" | sort timestamp`
- **THEN** scrolling past `filter` or `sort` does not add either as a sticky line

### Requirement: Incomplete queries do not cause errors
If the PSI tree is in an incomplete or invalid state (e.g., a subquery bracket is not closed, or the entry command is missing), the sticky lines provider SHALL return an empty result without throwing an exception.

#### Scenario: Empty subquery bracket produces no sticky lines for that scope
- **WHEN** the editor contains `| append [` with no content inside the bracket
- **THEN** no sticky line is shown for the incomplete subquery scope and the editor remains stable

