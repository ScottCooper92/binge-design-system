# Pull to refresh

`BingePullToRefresh` lets a phone user pull down to re-read a screen. It is phone only; a TV has no pull. This page says
where to use it and how a view model reports the reload, so the apps do it the same way.

## Where it applies

Use it on a screen that lists or summarises server data that others can change while the user is away:

- dashboards and hubs;
- request and issue lists;
- detail pages with live status;
- device, user and task lists.

Do not use it on:

- a **form or edit screen**: a refresh would clobber what is being edited;
- a **save-as-made page**, which saves as the user changes it;
- a **drag-to-reorder list**, where the pull competes with the drag;
- a **loading or error state**: it is not scrollable, and Retry is the way out;
- a screen that **already updates live**: an event feed, or a poll while something runs.

A **pager**: wrap each page, not the pager, and set `indicatorTopInset` so the spinner clears a chip header or a transparent
top bar (`BingeScreenScaffold` passes its top padding for this).

A **paged list** already reports `loadState.refresh is Loading` and needs nothing more: pass that as `isRefreshing`, and show
this spinner instead of an in-list refresh indicator for the same read, never both.

## Reporting the reload

Most screens reload by setting `Loading`, which swaps the content for a skeleton, and many reloads (a bumped trigger, a
re-collected flow) expose no in-progress signal. A pull would then flash the skeleton, or finish at once. Two rules fix that:

1. **Keep the last content while a reload runs.** The reload is a refresh of the `Ready` state, not a new `Loading`.
2. **Report the reload with `RefreshTracker`.** It owns no content and no trigger: wrap whatever the view model already calls.

```kotlin
class ThingsViewModel(private val repository: ThingsRepository) : ViewModel() {
    private val refreshTracker = RefreshTracker()

    val uiState: StateFlow<ThingsUiState> =
        combine(things, refreshTracker.isRefreshing) { result, refreshing ->
            when (result) {
                is Result.Loaded -> ThingsUiState.Ready(result.things, isRefreshing = refreshing)
                is Result.Failed -> ThingsUiState.Error(result.error)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, ThingsUiState.Loading)

    fun refresh() = viewModelScope.launch { refreshTracker.track { repository.reload() } }
}
```

`isRefreshing` is a field of the data variant, so the screen keeps its one state stream. The screen passes it on:

```kotlin
BingePullToRefresh(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh) { ThingsList(state.things) }
```

`track` reports the end however the reload ends: it returns, it throws (the exception passes through unchanged) or it is
cancelled. Reloads that overlap, such as a pull while a trigger-driven reload is running, keep `isRefreshing` true until the
last of them ends. A failed reload leaves the last content in place; show the failure as the screen's usual message, not as an
`Error` that replaces the list.
