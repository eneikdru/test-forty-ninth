<script>
  import { onMount, onDestroy } from 'svelte';

  export let trackingEndpoint = '/api/v1/analytics/search-events';
  export let nowFn = () => performance.now();
  export let fetchFn = null;

  let searchQuery = '';
  let activeSearchQuery = '';
  let searchResults = [];
  let isSearching = false;
  let searchCompleted = false;
  let searchCompletedAt = null;
  let hasClickedDocument = false;
  let isAbandoned = false;
  let trackedEvents = [];

  // Default sample dataset
  const sampleDocuments = [
    { id: '123e4567-e89b-12d3-a456-426614174000', title: 'Standard Protocol for Influenza Outbreak Investigation', category: 'protocol', type: 'PDF' },
    { id: '223e4567-e89b-12d3-a456-426614174001', title: 'Q3 Epidemiological Surveillance Report 2023', category: 'report', type: 'PDF' },
    { id: '323e4567-e89b-12d3-a456-426614174002', title: 'Respiratory Virus Outbreak Dataset', category: 'dataset', type: 'Spreadsheet' },
    { id: '423e4567-e89b-12d3-a456-426614174003', title: 'Ebola Response Guidelines v2.1', category: 'guideline', type: 'Text' }
  ];

  const recentSearches = [
    'Q3 Financial Reports 2023',
    'Project Alpha Specification v2',
    'Employee Onboarding Guidelines'
  ];

  function getEffectiveFetch() {
    if (fetchFn) return fetchFn;
    if (typeof window !== 'undefined' && window.fetch) return window.fetch.bind(window);
    return () => Promise.resolve();
  }

  function transmitTelemetry(payload) {
    trackedEvents = [...trackedEvents, payload];

    if (fetchFn) {
      fetchFn(trackingEndpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      }).catch(() => {});
      return;
    }

    if (typeof navigator !== 'undefined' && typeof navigator.sendBeacon === 'function') {
      try {
        const blob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
        const sent = navigator.sendBeacon(trackingEndpoint, blob);
        if (sent) return;
      } catch (err) {
        // Fallback to fetch
      }
    }

    if (typeof window !== 'undefined' && window.fetch) {
      window.fetch(trackingEndpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        keepalive: true
      }).catch(() => {});
    }
  }

  export function executeSearch(query) {
    if (!query || !query.trim()) return;
    searchQuery = query.trim();
    activeSearchQuery = searchQuery;
    isSearching = true;

    // Filter documents
    const qLower = searchQuery.toLowerCase();
    searchResults = sampleDocuments.filter(doc =>
      doc.title.toLowerCase().includes(qLower) ||
      doc.category.toLowerCase().includes(qLower)
    );

    searchCompleted = true;
    hasClickedDocument = false;
    isAbandoned = false;
    searchCompletedAt = nowFn();
    isSearching = false;
  }

  export function handleDocumentClick(doc) {
    if (!searchCompleted || hasClickedDocument) return;

    const clickedTime = nowFn();
    const elapsedTimeMs = Math.max(0, Math.round(clickedTime - searchCompletedAt));
    hasClickedDocument = true;

    const eventPayload = {
      eventType: 'SEARCH_CLICK',
      query: activeSearchQuery,
      documentId: doc.id,
      documentTitle: doc.title,
      elapsedTimeMs: elapsedTimeMs,
      resultCount: searchResults.length,
      timestamp: new Date().toISOString()
    };

    transmitTelemetry(eventPayload);
  }

  export function handleAbandonment() {
    if (searchCompleted && !hasClickedDocument && !isAbandoned) {
      isAbandoned = true;

      const eventPayload = {
        eventType: 'SEARCH_ABANDONED',
        query: activeSearchQuery,
        resultCount: searchResults.length,
        executionTimeMs: 0,
        status: 'FAILED',
        timestamp: new Date().toISOString()
      };

      transmitTelemetry(eventPayload);
    }
  }

  function onKeyDown(e) {
    if (e.key === 'Enter') {
      executeSearch(searchQuery);
    }
  }

  const unloadHandler = () => {
    handleAbandonment();
  };

  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', unloadHandler);
    window.addEventListener('pagehide', unloadHandler);
  }

  onDestroy(() => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('beforeunload', unloadHandler);
      window.removeEventListener('pagehide', unloadHandler);
    }
    handleAbandonment();
  });
</script>

<header class="pt-12 pb-sm px-margin-mobile bg-surface sticky top-0 z-40 border-b border-surface-variant flex flex-col gap-sm relative">
  <div class="telemetry-marker top-4 right-4"></div>
  <div class="telemetry-label top-6 right-2">[Event: PageLoad_Search]</div>

  <div class="flex items-center justify-between gap-sm">
    <div class="flex items-center gap-sm">
      <button
        type="button"
        class="text-on-surface-variant hover:bg-surface-container-low p-2 rounded-full transition-colors relative"
        aria-label="Back"
        on:click={handleAbandonment}
      >
        <span class="material-symbols-outlined" data-icon="arrow_back">arrow_back</span>
        <div class="telemetry-marker top-0 left-0"></div>
      </button>
      <h1 class="font-headline-sm text-headline-sm text-primary font-bold tracking-tight">DocuStream</h1>
    </div>

    {#if searchCompleted && !hasClickedDocument}
      <button
        type="button"
        class="px-xs py-1 text-xs bg-error-container text-on-error-container rounded hover:opacity-90 font-mono-data"
        on:click={handleAbandonment}
        data-testid="abandon-btn"
      >
        Abandon Search
      </button>
    {/if}
  </div>

  <div class="relative w-full group">
    <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-on-surface-variant" data-icon="search">search</span>
    <input
      type="text"
      bind:value={searchQuery}
      on:keydown={onKeyDown}
      placeholder="Search epidemiological materials..."
      class="w-full h-12 pl-10 pr-10 bg-surface-container-low border border-outline-variant rounded-lg font-body-md text-body-md text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all placeholder:text-on-surface-variant"
      data-testid="search-input"
    />
    <button
      type="button"
      class="material-symbols-outlined absolute right-sm top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-primary"
      data-icon="tune"
      aria-label="Submit search"
      on:click={() => executeSearch(searchQuery)}
      data-testid="search-submit"
    >
      tune
    </button>
    <div class="telemetry-marker -top-2 left-1/2"></div>
    <div class="telemetry-label -top-5 left-1/2 -translate-x-1/2">[Event: SearchFocus]</div>
  </div>
</header>

<main class="flex-1 overflow-y-auto pb-24 pt-lg px-margin-mobile relative">
  {#if searchCompleted}
    <section class="mb-xl relative" data-testid="search-results-section">
      <div class="flex items-center justify-between mb-sm">
        <h2 class="font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider">
          Search Results ({searchResults.length})
        </h2>
        <span class="text-xs font-mono-data text-primary">
          Query: "{activeSearchQuery}"
        </span>
      </div>

      {#if searchResults.length > 0}
        <div class="flex flex-col gap-xs" data-testid="results-list">
          {#each searchResults as doc (doc.id)}
            <button
              type="button"
              class="flex flex-col gap-xs p-sm bg-surface-container-low border border-surface-container-high rounded-lg hover:border-primary hover:bg-surface-container transition-all text-left w-full group relative"
              on:click={() => handleDocumentClick(doc)}
              data-testid="document-item"
              data-doc-id={doc.id}
            >
              <div class="flex items-center justify-between w-full">
                <span class="font-body-md font-semibold text-on-surface group-hover:text-primary">
                  {doc.title}
                </span>
                <span class="text-xs font-mono-data px-2 py-0.5 rounded bg-primary-fixed text-on-primary-fixed">
                  {doc.type}
                </span>
              </div>
              <div class="flex items-center gap-sm text-xs font-mono-data text-on-surface-variant">
                <span>Category: {doc.category}</span>
                <span>•</span>
                <span>ID: {doc.id.substring(0, 8)}...</span>
              </div>
            </button>
          {/each}
        </div>
      {:else}
        <div class="p-lg text-center bg-surface-container-low rounded-lg text-on-surface-variant font-body-md" data-testid="no-results">
          No materials found matching "{activeSearchQuery}".
        </div>
      {/if}
    </section>
  {/if}

  <!-- Recent Searches Section -->
  <section class="mb-xl relative">
    <div class="telemetry-marker top-0 left-0 border-tertiary"></div>
    <div class="telemetry-label -top-3 left-3 text-tertiary border-tertiary">[Metric: View_Recent]</div>
    <h2 class="font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider mb-sm">Recent Searches</h2>
    <div class="flex flex-col">
      {#each recentSearches as item}
        <button
          type="button"
          class="flex items-center gap-sm py-sm border-b border-surface-container-high hover:bg-surface-container-low transition-colors w-full text-left relative group"
          on:click={() => { searchQuery = item; executeSearch(item); }}
        >
          <span class="material-symbols-outlined text-outline-variant" data-icon="history">history</span>
          <span class="font-body-md text-body-md text-on-surface flex-1 truncate">{item}</span>
          <span class="material-symbols-outlined text-outline-variant opacity-0 group-hover:opacity-100 transition-opacity" data-icon="north_west">north_west</span>
        </button>
      {/each}
    </div>
  </section>

  <!-- Categories Section -->
  <section class="relative">
    <div class="telemetry-label -top-3 right-3 text-tertiary border-tertiary">[Metric: Tap_Category]</div>
    <h2 class="font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider mb-sm">Browse by Type</h2>
    <div class="grid grid-cols-2 gap-xs">
      <button
        type="button"
        class="bg-surface-container-low border border-surface-container-high rounded-lg p-sm flex flex-col items-start gap-xs hover:bg-surface-container transition-colors relative text-left"
        on:click={() => { searchQuery = 'protocol'; executeSearch('protocol'); }}
      >
        <div class="p-xs bg-error-container rounded-md">
          <span class="material-symbols-outlined text-on-error-container text-sm" data-icon="picture_as_pdf">picture_as_pdf</span>
        </div>
        <span class="font-body-md text-body-md font-semibold text-on-surface">Protocols</span>
        <span class="font-mono-data text-mono-data text-on-surface-variant">142 docs</span>
      </button>

      <button
        type="button"
        class="bg-surface-container-low border border-surface-container-high rounded-lg p-sm flex flex-col items-start gap-xs hover:bg-surface-container transition-colors relative text-left"
        on:click={() => { searchQuery = 'guideline'; executeSearch('guideline'); }}
      >
        <div class="p-xs bg-tertiary-fixed rounded-md">
          <span class="material-symbols-outlined text-on-tertiary-fixed text-sm" data-icon="description">description</span>
        </div>
        <span class="font-body-md text-body-md font-semibold text-on-surface">Guidelines</span>
        <span class="font-mono-data text-mono-data text-on-surface-variant">89 docs</span>
        <div class="telemetry-marker bottom-2 right-2 border-primary"></div>
      </button>

      <button
        type="button"
        class="col-span-2 bg-surface-container-low border border-surface-container-high rounded-lg p-sm flex items-center gap-sm hover:bg-surface-container transition-colors relative text-left"
        on:click={() => { searchQuery = 'dataset'; executeSearch('dataset'); }}
      >
        <div class="p-xs bg-secondary-fixed rounded-md">
          <span class="material-symbols-outlined text-on-secondary-fixed text-sm" data-icon="table_view">table_view</span>
        </div>
        <div class="flex flex-col items-start flex-1">
          <span class="font-body-md text-body-md font-semibold text-on-surface">Datasets & Reports</span>
          <span class="font-mono-data text-mono-data text-on-surface-variant">Financials & Data</span>
        </div>
        <span class="material-symbols-outlined text-outline-variant" data-icon="chevron_right">chevron_right</span>
      </button>
    </div>
  </section>

  <!-- Telemetry Debug Panel (for verification) -->
  {#if trackedEvents.length > 0}
    <section class="mt-xl p-sm bg-surface-container border border-outline-variant rounded-lg" data-testid="telemetry-log">
      <h3 class="font-label-caps text-xs text-primary uppercase font-bold mb-xs">Telemetry Transmitted ({trackedEvents.length})</h3>
      <div class="flex flex-col gap-xs font-mono-data text-xs">
        {#each trackedEvents as evt, idx}
          <div class="p-xs bg-surface rounded border border-surface-container-high">
            <span class="font-bold text-primary">[{evt.eventType}]</span>
            <span>Query: "{evt.query}"</span>
            {#if evt.elapsedTimeMs !== undefined}
              <span class="text-tertiary">Elapsed: {evt.elapsedTimeMs}ms</span>
            {/if}
            {#if evt.documentId}
              <span class="text-on-surface-variant">Doc: {evt.documentId.substring(0,8)}</span>
            {/if}
          </div>
        {/each}
      </div>
    </section>
  {/if}
</main>
